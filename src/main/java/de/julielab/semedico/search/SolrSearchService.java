package de.julielab.semedico.search;

import static de.julielab.semedico.IndexFieldNames.DATE;
import static de.julielab.semedico.IndexFieldNames.FACETS;
import static de.julielab.semedico.IndexFieldNames.FACET_TERMS;
import static de.julielab.semedico.IndexFieldNames.TEXT;
import static de.julielab.semedico.IndexFieldNames.TITLE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.Facet.Source;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.FacettedSearchResult;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.SearchSessionState;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.core.SortCriterium;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.IDocumentCacheService;
import de.julielab.semedico.core.services.IDocumentService;
import de.julielab.semedico.core.services.IFacetService;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.query.IQueryTranslationService;

public class SolrSearchService implements IFacettedSearchService {

	private IQueryTranslationService queryTranslationService;
	private IKwicService kwicService;
	private IDocumentCacheService documentCacheService;
	private IDocumentService documentService;

	private SolrServer solr;

	int maxDocumentHits;
	private final ITermService termService;
	private final ApplicationStateManager applicationStateManager;
	private final ILabelCacheService labelCacheService;
	private final IFacetService facetService;

	public SolrSearchService(
			SolrServer solr,
			IQueryTranslationService queryTranslationService,
			IDocumentCacheService documentCacheService,
			IDocumentService documentService,
			IKwicService kwicService,
			ApplicationStateManager applicationStateManager,
			ITermService termService,
			ILabelCacheService labelCacheService,
			IFacetService facetService,
			@Symbol(SemedicoSymbolConstants.SEARCH_MAX_NUMBER_DOC_HITS) int maxDocumentHits) {
		super();
		this.applicationStateManager = applicationStateManager;
		this.termService = termService;
		this.labelCacheService = labelCacheService;
		this.facetService = facetService;
		this.maxDocumentHits = maxDocumentHits;
		this.solr = solr;
		this.queryTranslationService = queryTranslationService;
		this.documentCacheService = documentCacheService;
		this.documentService = documentService;
		this.kwicService = kwicService;
	}

	@Override
	public FacettedSearchResult search(Multimap<String, IFacetTerm> queryTerms,
			String rawQuery, SortCriterium sortCriterium, boolean filterReviews)
			throws IOException {

		// Get the Solr-compatible query for the current query state (boolean
		// tree of term identifiers and key words).
		String solrQueryString = queryTranslationService.createQueryFromTerms(
				queryTerms, rawQuery);

		// Get the state objects.
		SearchSessionState searchSessionState = applicationStateManager
				.get(SearchSessionState.class);
		SolrQuery query = searchSessionState.getSearchState().getSolrQuery();
		UserInterfaceState uiState = searchSessionState.getUiState();

		// Adjust the user session's Solr query object to retrieve all
		// information needed. This includes facet counts, which is why we need
		// to know the IDs of currently (i.e. immediately after the current
		// search process) displayed terms in the facet boxes.
		Map<Facet, Set<String>> displayedTermIds = uiState
				.getDisplayedTermIdsForCurrentFacetGroup();
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
	 * @param displayedTermIds
	 */
	private void adjustQuery(SolrQuery query, String queryString,
			SortCriterium sortCriterium, boolean reviewFilter,
			int maxNumberOfHighlightedSnippets,
			Map<Facet, Set<String>> displayedTermIds) {

		query.setQuery(queryString);

		// Facets
		query.setFacet(true);
		// Collect term counts over all fields which contain facet terms.
		// query.add("facet.field", FACET_TERMS);
		query.add("facet.field", FACETS);
		for (Set<String> idSet : displayedTermIds.values()) {
			for (String id : idSet) {
				query.add("facet.query", FACET_TERMS + ":" + id);
			}
		}

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

	@Override
	public void getFacetCountsForFlatFacets(List<Facet> facets,
			FacetHit facetHit) {
		SearchState searchState = applicationStateManager.get(
				SearchSessionState.class).getSearchState();
		String strQ = queryTranslationService.createQueryFromTerms(
				searchState.getQueryTerms(), searchState.getRawQuery());
		SolrQuery q = new SolrQuery("*:*");
		q.setFilterQueries(strQ);
		q.setFacet(true);
		q.setRows(0);

		for (Facet facet : facets) {
			Facet.Source source = facet.getSource();
			q.add("facet.field", source.getName());
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
	 * @param termIds
	 * @return
	 */
	@Override
	public void getFacetCountsForHierarchicFacets(
			Multimap<Facet, String> termIds, FacetHit facetHit) {
		SearchState searchState = applicationStateManager.get(
				SearchSessionState.class).getSearchState();
		String strQ = queryTranslationService.createQueryFromTerms(
				searchState.getQueryTerms(), searchState.getRawQuery());
		SolrQuery q = new SolrQuery("*:*");
		q.setFilterQueries(strQ);
		q.setFacet(true);
		q.setRows(0);

		for (Facet facet : termIds.keySet()) {
			if (!facet.isHierarchical())
				throw new IllegalStateException(
						facet
								+ " is not hierarchic yet particular term counts are questioned"
								+ " (which makes no sense for flat facets)");

			Facet.Source source = facet.getSource();
			for (String id : termIds.get(facet)) {
				q.add("facet.query", source.getName() + ":" + id);
			}
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

	// private List<String> getDisplayedTermIds() {
	// UserInterfaceState uiState = applicationStateManager.get(
	// SearchSessionState.class).getUiState();
	// List<String> displayedTermIds = new ArrayList<String>();
	// for (Facet facet : uiState.getSelectedFacetGroup()) {
	// getDisplayedTermIdsForFacet(displayedTermIds, facet);
	// }
	// return displayedTermIds;
	// }
	//
	// /**
	// * @param searchConfiguration
	// * @param displayedTermIds
	// * @param facet
	// */
	// private void getDisplayedTermIdsForFacet(List<String> displayedTermIds,
	// Facet facet) {
	// UserInterfaceState uiState = applicationStateManager.get(
	// SearchSessionState.class).getUiState();
	// Map<Facet, FacetConfiguration> facetConfigurations = uiState
	// .getFacetConfigurations();
	// FacetConfiguration facetConfiguration = facetConfigurations.get(facet);
	// if (facetConfiguration.isDrilledDown()) {
	// IFacetTerm lastPathTerm = facetConfiguration.getLastPathElement();
	// IFacetTerm term = termService.getNode(lastPathTerm.getId());
	// Iterator<IFacetTerm> childIt = term.childIterator();
	// while (childIt.hasNext())
	// displayedTermIds.add(childIt.next().getId());
	//
	// } else {
	// Iterator<IFacetTerm> rootIt = termService.getFacetRoots(
	// facetConfiguration.getFacet()).iterator();
	// while (rootIt.hasNext())
	// displayedTermIds.add(rootIt.next().getId());
	// }
	// }

	private Map<Source, Object> storeHitFacetTermLabels(
			QueryResponse queryResponse, FacetHit facetHit) {

		// INITIALIZATION

		// Get all field related FacetSources.
		Collection<Facet.Source> fieldFacetSources = facetService
				.getFacetSourcesByTypes(Facet.FIELD_FLAT,
						Facet.FIELD_HIERARCHICAL);

		// Sort the received FacetSources by name.
		Multimap<String, Facet.Source> facetSources = HashMultimap.create();
		for (Facet.Source source : fieldFacetSources)
			facetSources.put(source.getName(), source);

		// We return a map from each source to an object holding the
		// corresponding label objects for display.
		// Labels for flat facet types - e.g. journals - which have no
		// hierarchical structure but are simply enumerated are stored
		// in a List.
		// Hierarchical organized facet types need to access their labels by
		// FacetTerm ID. In this manner, only the labels for the currently shown
		// hierarchy level must be retrieved. This saves computation time,
		// memory consumption and Solr response size.
		// Thus we return a map which contains an entry for each Label from its
		// corresponding FacetTerm ID to the Label itself.
		Map<Facet.Source, Object> retLabels = facetHit.getHitFacetTermLabels();
		for (Facet.Source source : fieldFacetSources) {
			if (source.isFlat() && retLabels.get(source) == null)
				retLabels.put(source, new ArrayList<Label>());
			else if (retLabels.get(source) == null)
				retLabels.put(source, new HashMap<String, Label>());
		}

		// HIERARCHICAL FACET STORAGE

		// A map from the facet name to its count hierarchical terms. Store the
		// query facet values.
		Map<String, Integer> facetQuery = queryResponse.getFacetQuery();
		// First check whether there are query term counts at all in Solr's
		// response.
		if (facetQuery != null && facetQuery.size() > 0) {
			for (String fieldAndId : facetQuery.keySet()) {
				// Returned format from Solr for query facet identification is
				// 'fieldName:term'; 'term' hereby is the FacetTerm's identifier
				// (at least indexing should happen that way).
				String[] fieldAndIdSplits = fieldAndId.split(":");
				String fieldName = fieldAndIdSplits[0];
				String termId = fieldAndIdSplits[1];
				Integer count = facetQuery.get(fieldAndId);
				if (count == null)
					count = 0;
				Label label = labelCacheService.getCachedTermLabel(termId);
				label.setCount(new Long(count));

				for (Facet.Source source : facetSources.get(fieldName)) {
					if (source.isHierarchical()) {
						@SuppressWarnings("unchecked")
						Map<String, Label> labelMap = (Map<String, Label>) retLabels
								.get(source);
						labelMap.put(termId, label);
					}
				}
			}
		}

		// FLAT FACET STORAGE

		// TODO hier ist ein Aussortierungsalgorithmus von Nöten, weil das Feld FacetTerms
		// die Terme aller möglichen Facetten beinhaltet. Und falls eien Facette auf Flach
		// geschaltet ist, müssen die N top
		
		for (Facet.Source source : fieldFacetSources) {
			if (source.isFlat()) {
				@SuppressWarnings("unchecked")
				List<Label> labelList = (List<Label>) retLabels.get(source);
				FacetField facetField = queryResponse.getFacetField(source
						.getName());
				for (Count count : facetField.getValues()) {
					Label label = labelCacheService.getCachedStringLabel(count
							.getName());
					label.setCount(count.getCount());
					labelList.add(label);
				}
			}
		}

		return retLabels;
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

}
