package de.julielab.semedico.search;

import static de.julielab.semedico.IndexFieldNames.DATE;
import static de.julielab.semedico.IndexFieldNames.FACETS;
import static de.julielab.semedico.IndexFieldNames.FACET_TERMS;
import static de.julielab.semedico.IndexFieldNames.TEXT;
import static de.julielab.semedico.IndexFieldNames.TITLE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ApplicationStateManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.FacettedSearchResult;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.SearchSessionState;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.core.SortCriterium;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.IDocumentCacheService;
import de.julielab.semedico.core.services.IDocumentService;
import de.julielab.semedico.core.services.IFacetService;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.query.IQueryTranslationService;

public class SolrSearchService implements IFacetedSearchService {

	private IQueryTranslationService queryTranslationService;
	private IKwicService kwicService;
	private IDocumentCacheService documentCacheService;
	private IDocumentService documentService;

	private SolrServer solr;

	int maxDocumentHits;
	private final ApplicationStateManager applicationStateManager;
	private final ILabelCacheService labelCacheService;
	private final IFacetService facetService;
	
	private final Set<String> alreadyQueriedTermIndicator;

	public SolrSearchService(
			SolrServer solr,
			IQueryTranslationService queryTranslationService,
			IDocumentCacheService documentCacheService,
			IDocumentService documentService,
			IKwicService kwicService,
			ApplicationStateManager applicationStateManager,
			ILabelCacheService labelCacheService,
			IFacetService facetService,
			@Symbol(SemedicoSymbolConstants.SEARCH_MAX_NUMBER_DOC_HITS) int maxDocumentHits) {
		super();
		this.applicationStateManager = applicationStateManager;
		this.labelCacheService = labelCacheService;
		this.facetService = facetService;
		this.maxDocumentHits = maxDocumentHits;
		this.solr = solr;
		this.queryTranslationService = queryTranslationService;
		this.documentCacheService = documentCacheService;
		this.documentService = documentService;
		this.kwicService = kwicService;
		this.alreadyQueriedTermIndicator = new HashSet<String>();
	}

	@Override
	public FacettedSearchResult search(Multimap<String, IFacetTerm> queryTerms,
			String rawQuery, SortCriterium sortCriterium, boolean filterReviews)
			throws IOException {

		alreadyQueriedTermIndicator.clear();
		
		// Get the Solr-compatible query for the current query state (boolean
		// tree of term identifiers and key words).
		String solrQueryString = queryTranslationService.createQueryFromTerms(
				queryTerms, rawQuery);

		// Get the state objects.
		SearchSessionState searchSessionState = applicationStateManager
				.get(SearchSessionState.class);
		SolrQuery query = getSolrQuery();
		UserInterfaceState uiState = searchSessionState.getUiState();

		// Adjust the user session's Solr query object to retrieve all
		// information needed. This includes facet counts, which is why we need
		// to know the IDs of currently (i.e. immediately after the current
		// search process) displayed terms in the facet boxes.
		Map<FacetConfiguration, Set<IFacetTerm>> displayedTermIds = uiState
				.getAllTermsOnCurrentFacetLevelsInSelectedFacetGroup();
		adjustQuery(query, solrQueryString, sortCriterium, filterReviews,
				queryTerms.size(), displayedTermIds);

		// Do the actual Solr search.
		QueryResponse queryResponse = performSearch(query, 0, maxDocumentHits);

		// Extract the facet counts from Solr's response and store them to the
		// user's interface state object.
		FacetHit facetHit = uiState.getFacetHit();
		storeHitFacetTermLabels(queryResponse, facetHit);
		// Store the total counts for each facet (not individual facet/term
		// counts but the counts of all hit terms of each facet).
		storeTotalFacetCounts(queryResponse, facetHit);

		Collection<DocumentHit> documentHits = createDocumentHitsForPositions(queryResponse);
		return new FacettedSearchResult(facetHit, documentHits,
				(int) queryResponse.getResults().getNumFound());
	}

	/**
	 * Configures the <code>SolrQuery</code> object <code>query</code> to
	 * reflect the current search state determined by the user, e.g. by query
	 * input, term selection etc.
	 * 
	 * @param query
	 *            The <code>SolrQuery</code> to configure.
	 * @param queryString
	 *            The user's original query string.
	 * @param sortCriterium
	 * @param reviewFilter
	 * @param maxNumberOfHighlightedSnippets
	 * @param displayedTerms
	 */
	private void adjustQuery(SolrQuery query, String queryString,
			SortCriterium sortCriterium, boolean reviewFilter,
			int maxNumberOfHighlightedSnippets,
			Map<FacetConfiguration, Set<IFacetTerm>> displayedTerms) {

		query.setQuery(queryString);

		// Facets
		query.setFacet(true);
		// Collect term counts over all fields which contain facet terms.
		// query.add("facet.field", FACET_TERMS);
		query.add("facet.field", FACETS);
		adjustQueryForFacetCountsInSelectedFacetGroup(query, displayedTerms);

		// Set hightlighting.
		query.setHighlight(true);
		// Text snippets causing a hit should be highlighted in bold
		query.setHighlightSimplePre("<b>");
		query.setHighlightSimplePost("</b>");
		// hl.fl = HighLighting FieldList - a list of fields for which hits
		// should be returned in a highlighted manner.
		query.add("hl.fl", TEXT + "," + TITLE);
		// TODO magic number: 3
		query.set("hl.snippets", Math.min(3, maxNumberOfHighlightedSnippets));

		// Sorting
		switch (sortCriterium) {
		case DATE:
			query.setSortField("date", ORDER.desc);
			break;
		case DATE_AND_RELEVANCE:
			query.set("sort", DATE + " desc,score desc");
			break;
		case RELEVANCE:
			query.setSortField("score", ORDER.desc);
		}
	}

	/**
	 * @param query
	 * @param displayedTerms
	 */
	private void adjustQueryForFacetCountsInSelectedFacetGroup(SolrQuery query,
			Map<FacetConfiguration, Set<IFacetTerm>> displayedTerms) {
		FacetGroup<FacetConfiguration> selectedFacetGroup = applicationStateManager
				.get(SearchSessionState.class).getUiState()
				.getSelectedFacetGroup();
		for (FacetConfiguration facetConfiguration : selectedFacetGroup) {
			adjustQueryForFacetCountsInFacet(query, facetConfiguration,
					displayedTerms.get(facetConfiguration));
		}
	}

	/**
	 * Adds faceting paremeters to the <code>SolrQuery</code> <code>query</code>
	 * .
	 * <p>
	 * For facets whose facetConfiguration is in a hierarchical state, facet
	 * queries are built which ask Solr for the particular terms given in
	 * <code>displayedTermsInFacet</code>. This collection must then contain all
	 * terms which are currently viewable by the user in the corresponding
	 * facet.<br>
	 * The returned facet counts will later be used by the <code>FacetBox</code>
	 * component to render exactly these terms (more precisely, their
	 * {@link TermLabel}s) to where the user has currently drilled down (which
	 * may be the roots of the facet if the user hasn't drilled down at all).
	 * </p>
	 * <p>
	 * For facetConfigurations in flat state, the whole field which is the
	 * source of the facet in <code>facetConfiguration</code> is used for
	 * faceting. This results in a list of top N ranked terms in this field,
	 * ordered decreasing by frequency.<br>
	 * For flat facets, <code>displayedTermsInFacet</code> may be
	 * <code>null</code>.
	 * </p>
	 * 
	 * @param query
	 *            The <code>SolrQuery</code> to add facet parameters to.
	 * @param facetConfiguration
	 *            The <code>FacetConfiguration</code> for the actual
	 *            <code>Facet</code> to generate term counts for.
	 * @param displayedTermsInFacet
	 *            For <code>FacetConfiguration</code>s in hierarchical state,
	 *            this collection must contain all terms whose
	 *            <code>TermLabels</code> will be rendered to the user and for
	 *            which no up-to-date counts exist.
	 */
	// TODO check: when updates for individual facets are done, can it happen it
	// is asked for already existing information?
	private void adjustQueryForFacetCountsInFacet(SolrQuery query,
			FacetConfiguration facetConfiguration,
			Collection<IFacetTerm> displayedTermsInFacet) {
		// If the facet terms should be shown in a hierarchical manner we
		// query the facet term counts directly.
		if (facetConfiguration.isHierarchical()) {
			for (IFacetTerm term : displayedTermsInFacet) {
				if (alreadyQueriedTermIndicator.contains(term.getId()))
					continue;
				alreadyQueriedTermIndicator.add(term.getId());
				query.add("facet.query", facetConfiguration.getSource()
						.getName() + ":" + term.getId());
			}
		}
		// Otherwise we let Solr give us a sorted list of the top N terms.
		else {
			query.add("facet.field", FACET_TERMS
					+ facetConfiguration.getFacet().getId());
		}
	}

	/**
	 * Performs a search using <code>query</code> on the SolrServer. The
	 * documents determined by <code>start</code> and <code>rows</code> (number
	 * of documents for which the actual data is returned) will be returned for
	 * display.
	 * 
	 * @param query
	 *            The <code>SolrQuery</code> to search for.
	 * @param start
	 *            The document offset from which document data will be returned.
	 * @param rows
	 *            For how many documents data should be returned.
	 * @return Solr's <code>QueryResponse</code> containing the document data,
	 *         facet counts, number of found documents and more.
	 */
	private QueryResponse performSearch(SolrQuery query, int start, int rows) {
		query.setStart(start);
		query.setRows(rows);

		QueryResponse response = null;
		try {
			response = solr.query(query, METHOD.POST);
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.search.IFacetedSearchService#
	 * queryAndStoreFacetCountsInSelectedFacetGroup(java.util.Map)
	 */
	@Override
	public void queryAndStoreFacetCountsInSelectedFacetGroup(
			Map<FacetConfiguration, Set<IFacetTerm>> displayedTerms,
			FacetHit facetHit) {
		String strQ = createQueryString();
		SolrQuery q = getSolrQuery();
		q.setQuery("*:*");
		q.setFilterQueries(strQ);
		q.setFacet(true);
		q.setRows(0);
		adjustQueryForFacetCountsInSelectedFacetGroup(q, displayedTerms);
		try {
			QueryResponse queryResponse = solr.query(q);
			storeHitFacetTermLabels(queryResponse, facetHit);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void queryAndStoreFlatFacetCounts(List<FacetConfiguration> facets,
			FacetHit facetHit) {
		String strQ = createQueryString();
		SolrQuery q = getSolrQuery();
		q.setQuery("*:*");
		q.setFilterQueries(strQ);
		q.setFacet(true);
		q.setRows(0);

		for (FacetConfiguration facetConfiguration : facets) {
			adjustQueryForFacetCountsInFacet(q, facetConfiguration, null);
		}

		try {
			QueryResponse queryResponse = solr.query(q);

			storeHitFacetTermLabels(queryResponse, facetHit);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param displayedTerms
	 * @return
	 */
	@Override
	public void queryAndStoreHierarchichalFacetCounts(
			Multimap<FacetConfiguration, IFacetTerm> displayedTerms,
			FacetHit facetHit) {
		String strQ = createQueryString();
		SolrQuery q = getSolrQuery();
		q.setQuery("*:*");
		q.setFilterQueries(strQ);
		q.setFacet(true);
		q.setRows(0);

		for (FacetConfiguration facetConfiguration : displayedTerms.keySet()) {
			adjustQueryForFacetCountsInFacet(q, facetConfiguration,
					displayedTerms.get(facetConfiguration));
		}
		try {
			QueryResponse queryResponse = solr.query(q, METHOD.POST);

			storeHitFacetTermLabels(queryResponse, facetHit);

			// for (String id : queryResponse.getFacetQuery().keySet()) {
			// String termId = id.split(":")[1];
			// Integer count = queryResponse.getFacetQuery().get(id);
			// if (count == null)
			// count = 0;
			// Label label = labelCacheService.getCachedLabel(termId);
			// label.setHits(new Long(count));
			// hitFacetTermLabels.put(termId, label);
			// }
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Collection<DocumentHit> constructDocumentPage(int start) {
		SolrQuery query = applicationStateManager.get(SearchSessionState.class)
				.getSearchState().getSolrQuery();

		query.setStart(start);
		query.setRows(maxDocumentHits);
		QueryResponse queryResponse = performSearch(query, start,
				maxDocumentHits);
		return createDocumentHitsForPositions(queryResponse);
	}

	// Build Semedico DocumentHit which consists of a Semedico Document (Title,
	// Text, PMID, ...), the kwicQuery string of the disambiguated queryTerms
	// and the size of queryTerms.
	// TODO choose more appropriate name
	private Collection<DocumentHit> createDocumentHitsForPositions(
			QueryResponse queryResponse) {

		Collection<DocumentHit> documentHits = Lists.newArrayList();

		SolrDocumentList solrDocs = queryResponse.getResults();
		for (SolrDocument solrDoc : solrDocs) {
			Integer docId = Integer.parseInt((String) solrDoc
					.getFieldValue(IndexFieldNames.PUBMED_ID));

			SemedicoDocument semedicoDoc = documentCacheService
					.getCachedDocument(docId);
			if (semedicoDoc == null) {
				semedicoDoc = documentService
						.buildSemedicoDocFromSolrDoc(solrDoc);
				documentCacheService.addDocument(semedicoDoc);
			}
			// Is it possible to highlight corresponding to the user input and
			// return fragments for each term hit?
			DocumentHit documentHit = kwicService.createDocumentHit(
					semedicoDoc, queryResponse.getHighlighting());
			documentHits.add(documentHit);
		}

		return documentHits;
	}

	// TODO Still used?
	// @Override
	// public Map<String, Label> getHitFacetTermLabelsForFacetGroup(
	// FacetGroup facetGroup) {
	// List<String> displayedTermIds = new ArrayList<String>();
	// for (Facet facet : facetGroup)
	// getDisplayedTermIdsForFacet(displayedTermIds, facet);
	// return getFacetCountsForTermIds(displayedTermIds, null);
	// }

	/**
	 * @return
	 */
	private String createQueryString() {
		SearchState searchState = applicationStateManager.get(
				SearchSessionState.class).getSearchState();
		String strQ = queryTranslationService.createQueryFromTerms(
				searchState.getQueryTerms(), searchState.getRawQuery());
		return strQ;
	}

	private void storeHitFacetTermLabels(QueryResponse queryResponse,
			FacetHit facetHit) {

		// INITIALIZATION

		FacetGroup<FacetConfiguration> selectedFacetGroup = applicationStateManager
				.get(SearchSessionState.class).getUiState()
				.getSelectedFacetGroup();
		Collection<FacetConfiguration> flatFacets = selectedFacetGroup
				.getFacetsBySourceType(Facet.FIELD_FLAT);

		// One single Map to associate with each queried term id its facet
		// count.
		Map<String, TermLabel> labelsHierarchical = facetHit
				.getLabelsHierarchical();
		// Facet-ID-associated Label lists for per-field frequency-ordered facet
		// counts.
		Map<Integer, List<Label>> labelsFlat = facetHit.getLabelsFlat();

		// HIERARCHICAL FACET STORAGE

		// A map from the field name to its count hierarchical terms. Store the
		// query facet values.
		Map<String, Integer> facetQuery = queryResponse.getFacetQuery();
		// First check whether there are query term counts at all in Solr's
		// response.
		if (facetQuery != null && facetQuery.size() > 0) {
			for (String fieldAndId : facetQuery.keySet()) {
				// Returned format from Solr for query facet identification is
				// 'fieldName:term'; 'term' hereby is the FacetTerm's identifier
				// (at least indexing should happen that way).
				String termId = fieldAndId.split(":")[1];
				Integer count = facetQuery.get(fieldAndId);
				// Counts may be null as we queried for particular terms which
				// the user could see at the currently selected facet depth.
				// There is no guaranty that there are hits on that terms.
				if (count == null)
					count = 0;
				
				if (labelsHierarchical.containsKey(termId))
					throw new IllegalStateException("Term " + termId 
							+ " has been queried twice.");

				TermLabel label = labelCacheService.getCachedTermLabel(termId);

				for (IFacetTerm parent : label.getTerm().getAllParents()) {
					String parentId = parent.getId();
					// As long as the user browses the facets in hierarchical
					// mode, this cannot happen as we query facet counts
					// level-wise. But when switched to flat mode, this almost
					// certainly will happen.
					if (!labelsHierarchical.containsKey(parentId))
						continue;
					TermLabel parentLabel = labelsHierarchical.get(parentId);
					for (Facet facet : parent.getFacets()) {
						if (label.getTerm().isContainedInFacet(facet))
							parentLabel.setHasChildHitsInFacet(facet);
					}
				}
				label.setCount(new Long(count));

				// A label could have been added to the map before when one of
				// its children came through this loop before.
				// TermLabel label = labelsHierarchical.get(termId);
				// if (label == null)
				// label = labelCacheService.getCachedTermLabel(termId);
				//
				//
				// // Tell the parents of the current term that they have child
				// // hits and add them to the map, too.
				// for (IFacetTerm parent : label.getTerm().getAllParents()) {
				// TermLabel parentLabel = labelsHierarchical.get(parent
				// .getId());
				// if (parentLabel == null) {
				// parentLabel = labelCacheService
				// .getCachedTermLabel(parent.getId());
				// labelsHierarchical.put(parent.getId(), parentLabel);
				// }
				// for (Facet facet : parent.getFacets()) {
				// if (label.getTerm().isContainedInFacet(facet))
				// parentLabel.setHasChildHitsInFacet(facet);
				// }
				// }

				labelsHierarchical.put(termId, label);
			}
		}

		// FLAT FACET STORAGE

		for (FacetConfiguration facetConfiguration : flatFacets) {
			List<Label> labelList = labelsFlat.get(facetConfiguration
					.getFacet().getId());
			if (labelList == null) {
				labelList = new ArrayList<Label>();
				labelsFlat
						.put(facetConfiguration.getFacet().getId(), labelList);
			}
			FacetField facetField = queryResponse
					.getFacetField(facetConfiguration.getSource().getName());
			for (Count count : facetField.getValues()) {
				Label label = null;
				if (facetConfiguration.getFacet().isHierarchical())
					label = labelCacheService.getCachedStringLabel(count
							.getName());
				else
					label = labelCacheService.getCachedTermLabel(count
							.getName());
				label.setCount(count.getCount());
				labelList.add(label);
			}
		}

	}

	/**
	 * @param queryResponse
	 * @param facetHit
	 */
	private void storeTotalFacetCounts(QueryResponse queryResponse,
			FacetHit facetHit) {
		for (FacetField field : queryResponse.getFacetFields()) {
			// This field has no hit facets. When no documents were found,
			// no field will have any hits.
			if (field.getValues() == null)
				continue;
			// The the facet category counts, e.g. for "Proteins and Genes".
			else if (field.getName().equals(IndexFieldNames.FACETS)) {
				// Iterate over the actual facet counts.
				for (Count count : field.getValues()) {
					Facet facet = facetService.getFacetWithId(Integer
							.parseInt(count.getName()));
					facetHit.setTotalFacetCount(facet, count.getCount());
				}
			}
		}
	}
	
	private SolrQuery getSolrQuery() {
		 SolrQuery solrQuery = applicationStateManager
				.get(SearchSessionState.class).getSearchState().getSolrQuery();
		 solrQuery.clear();
		 return solrQuery;
	}

}
