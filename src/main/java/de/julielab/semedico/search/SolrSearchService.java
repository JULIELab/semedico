package de.julielab.semedico.search;

import static de.julielab.semedico.IndexFieldNames.DATE;
import static de.julielab.semedico.IndexFieldNames.FACETS;
import static de.julielab.semedico.IndexFieldNames.TEXT;
import static de.julielab.semedico.IndexFieldNames.TITLE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
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

	// TODO Information bleed between users!!!!=!=!="¤$
	private final Set<String> alreadyQueriedTermIndicator;

	private static final String REVIEW_TERM = "Review";

	private final Logger logger;

	public SolrSearchService(
			Logger logger,
			@InjectService("SolrSearcher") SolrServer solr,
			IQueryTranslationService queryTranslationService,
			IDocumentCacheService documentCacheService,
			IDocumentService documentService,
			IKwicService kwicService,
			ApplicationStateManager applicationStateManager,
			ILabelCacheService labelCacheService,
			IFacetService facetService,
			@Symbol(SemedicoSymbolConstants.SEARCH_MAX_NUMBER_DOC_HITS) int maxDocumentHits) {
		super();
		this.logger = logger;
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
	public FacettedSearchResult search(String solrQueryString,
			int maxNumberOfHighlightedSnippets, SortCriterium sortCriterium,
			boolean filterReviews) throws IOException {

		alreadyQueriedTermIndicator.clear();

		// Get the state objects.
		SearchSessionState searchSessionState = applicationStateManager
				.get(SearchSessionState.class);
		SolrQuery query = getSolrQuery();
		UserInterfaceState uiState = searchSessionState.getUiState();

		// Adjust the user session's Solr query object to retrieve all
		// information needed. This includes facet counts, which is why we need
		// to know the IDs of currently (i.e. immediately after the current
		// search process) displayed terms in the facet boxes.
		Map<FacetConfiguration, Collection<IFacetTerm>> displayedTermIds = uiState
				.getDisplayedTermsInSelectedFacetGroup();
		adjustQuery(query, solrQueryString, sortCriterium, filterReviews,
				maxNumberOfHighlightedSnippets, displayedTermIds);

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

	public Collection<DocumentHit> constructDocumentPage(int start) {

		SearchState searchState = applicationStateManager.get(
				SearchSessionState.class).getSearchState();
		UserInterfaceState uiState = applicationStateManager.get(
				SearchSessionState.class).getUiState();

		SolrQuery query = searchState.getSolrQuery();

		Multimap<String, IFacetTerm> queryTerms = searchState.getQueryTerms();
		String rawQuery = searchState.getRawQuery();
		String solrQueryString = queryTranslationService.createQueryFromTerms(
				queryTerms, rawQuery);
		SortCriterium sortCriterium = searchState.getSortCriterium();
		boolean reviewsFiltered = searchState.isReviewsFiltered();

		Map<FacetConfiguration, Collection<IFacetTerm>> displayedTermIds = uiState
				.getDisplayedTermsInSelectedFacetGroup();

		adjustQuery(query, solrQueryString, sortCriterium, reviewsFiltered,
				queryTerms.size(), displayedTermIds);

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
			// Is it possible to highlight corresponding to the user input and
			// return fragments for each term hit?
			DocumentHit documentHit = documentService.getHitListDocument(
					solrDoc, queryResponse.getHighlighting());
			documentHits.add(documentHit);
		}

		return documentHits;
	}

	/**
	 * Configures the <code>SolrQuery</code> object <code>query</code> to
	 * reflect the current search state determined by the user, e.g. by query
	 * input, term selection etc.
	 * 
	 * @param query
	 *            The <code>SolrQuery</code> to configure.
	 * @param solrQueryString
	 *            The user's original query string.
	 * @param sortCriterium
	 * @param reviewFilter
	 * @param maxNumberOfHighlightedSnippets
	 * @param displayedTerms
	 */
	private void adjustQuery(SolrQuery query, String solrQueryString,
			SortCriterium sortCriterium, boolean reviewFilter,
			int maxNumberOfHighlightedSnippets,
			Map<FacetConfiguration, Collection<IFacetTerm>> displayedTerms) {

		query.setQuery(solrQueryString);

		// Faceting is always set to true by 'getSolrQuery()'.
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

		if (reviewFilter) {
			query.addFilterQuery(IndexFieldNames.FACET_PUBTYPES + ":"
					+ REVIEW_TERM);
		}
	}

	/**
	 * @param query
	 * @param displayedTerms
	 */
	private void adjustQueryForFacetCountsInSelectedFacetGroup(SolrQuery query,
			Map<FacetConfiguration, Collection<IFacetTerm>> displayedTerms) {
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
	private void adjustQueryForFacetCountsInFacet(SolrQuery query,
			FacetConfiguration facetConfiguration,
			Collection<IFacetTerm> displayedTermsInFacet) {
		// If the facet terms should be shown in a hierarchical manner we
		// query the facet term counts directly.
		if (facetConfiguration.isHierarchical()
				&& !facetConfiguration.isForcedToFlatFacetCounts()) {
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
			query.add("facet.field", facetConfiguration.getSource().getName());
		}
		// In any case, we want to know how many facet terms were found for the
		// facet.
		// TODO When Solr is updated beyond 3.6, check again whether this works.
		// Until 3.6, there will be an error when performing the search in
		// FieldStatsInfo.class
		// because this class expects only number values and no strings.
		// query.setGetFieldStatistics(facetConfiguration.getSource().getName());
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
			logger.error(
					"Error while performing Solr search. Search query was '"
							+ query.getQuery() + "'. Error: ", e);
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
			Map<FacetConfiguration, Collection<IFacetTerm>> displayedTerms,
			FacetHit facetHit) {
		String strQ = createQueryString();
		SolrQuery q = getSolrQuery();
		q.setQuery("*:*");
		q.setFilterQueries(strQ);
		q.setRows(0);
		adjustQueryForFacetCountsInSelectedFacetGroup(q, displayedTerms);
		try {
			QueryResponse queryResponse = solr.query(q, METHOD.POST);
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
		q.add("facet.mincount", "1");
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
				.getFlatElements();

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
					logger.warn("Term " + termId + " has been queried twice.");

				TermLabel label = labelCacheService.getCachedTermLabel(termId);
				label.setCount(new Long(count));

				// If the current facet term actually has been hit, set its
				// parents in the same facet as having a child in that facet.
				if (count > 0) {
					for (IFacetTerm parent : label.getTerm().getAllParents()) {
						String parentId = parent.getId();
						// As long as the user browses the facets in
						// hierarchical
						// mode, this cannot happen as we query facet counts
						// level-wise. But when switched to flat mode, this
						// almost
						// certainly will happen.
						// TODO but - when in flat mode we won't come here, will
						// we?! For that the part at the bottom exists...
						if (!labelsHierarchical.containsKey(parentId))
							continue;
						TermLabel parentLabel = labelsHierarchical
								.get(parentId);
						for (Facet facet : parent.getFacets()) {
							if (label.getTerm().isContainedInFacet(facet))
								parentLabel.setHasChildHitsInFacet(facet);
						}
					}
				}

				labelsHierarchical.put(termId, label);
			}
		}

		// FLAT FACET STORAGE
		for (FacetField facetField : queryResponse.getFacetFields()) {
			FacetConfiguration facetConfiguration = selectedFacetGroup
					.getElementsBySourceName(facetField.getName());

			// Happens when we come over a Solr facet field which does not serve
			// a facet. This could be the field for total facet counts, for
			// example.
			if (facetConfiguration == null)
				continue;

			List<Label> labelList = new ArrayList<Label>();
			labelsFlat.put(facetConfiguration.getFacet().getId(), labelList);

			List<Count> facetValues = facetField.getValues();
			// Happens when no terms for the field are returned (e.g. when
			// there are no terms found for the facet and facet.mincount is
			// set to 1 or higher).
			if (facetValues == null)
				continue;
			for (Count count : facetValues) {
				Label label = null;
				if (facetConfiguration.getFacet().isFlat())
					label = labelCacheService.getCachedStringLabel(count
							.getName());
				else {
					// If we have a facet which genuinely contains
					// (hierarchical) terms but is set to flat state, we do
					// not only get a label and put in the list.
					// Additionally we put it into the hierarchical labels
					// map and resolve child term hits for parents.
					String termId = count.getName();
					label = labelsHierarchical.get(termId);
					if (label == null) {
						label = labelCacheService.getCachedTermLabel(count
								.getName());
						labelsHierarchical.put(termId, (TermLabel) label);
						resolveChildHitsRecursively(
								((TermLabel) label).getTerm(),
								labelsHierarchical);
					}
				}
				label.setCount(count.getCount());
				labelList.add(label);
			}
		}
	}

	/**
	 * <p>
	 * Recursively resolves child term hits for all ancestors of
	 * <code>term</code>.
	 * </p>
	 * 
	 * @param term
	 * @param labelsHierarchical
	 */
	private void resolveChildHitsRecursively(IFacetTerm term,
			Map<String, TermLabel> labelsHierarchical) {
		for (IFacetTerm parent : term.getAllParents()) {
			for (Facet facet : term.getFacets()) {
				if (parent.isContainedInFacet(facet)) {
					TermLabel parentLabel = labelsHierarchical.get(parent
							.getId());
					if (parentLabel == null) {
						parentLabel = labelCacheService
								.getCachedTermLabel(parent.getId());
						labelsHierarchical.put(parent.getId(), parentLabel);
					}
					if (!parentLabel.hasChildHitsInFacet(facet)) {
						parentLabel.setHasChildHitsInFacet(facet);
						resolveChildHitsRecursively(parent, labelsHierarchical);
					}
				}
			}
		}
	}

	/**
	 * @param queryResponse
	 * @param facetHit
	 */
	private void storeTotalFacetCounts(QueryResponse queryResponse,
			FacetHit facetHit) {

		// TODO Won't work until the statistics component is fixed in solrj to
		// work with string fields.
		// See remark in adjustQueryForFacetCountsInFacet
		// Map<String, FieldStatsInfo> fieldStatsInfo = queryResponse
		// .getFieldStatsInfo();
		// FacetGroup<FacetConfiguration> selectedFacetGroup =
		// applicationStateManager
		// .get(SearchSessionState.class).getUiState()
		// .getSelectedFacetGroup();
		//
		// for (FacetConfiguration facetConfiguration : selectedFacetGroup) {
		// FieldStatsInfo fieldStats = fieldStatsInfo.get(facetConfiguration
		// .getSource().getName());
		// facetHit.setTotalFacetCount(facetConfiguration.getFacet(),
		// fieldStats.getCount());
		// }

		if (queryResponse.getResults().getNumFound() == 0) {
			for (Facet facet : facetService.getFacets())
				facetHit.setTotalFacetCount(facet, 0);
		}

		for (FacetField field : queryResponse.getFacetFields()) {
			// This field has no hit facets. When no documents were found,
			// no field will have any hits.
			if (field.getValues() == null)
				continue;
			// The facet category counts, e.g. for "Proteins and Genes".
			else if (field.getName().equals(IndexFieldNames.FACETS)) {
				// Iterate over the actual facet counts.
				for (Count count : field.getValues()) {
					Facet facet = facetService.getFacetById(Integer
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
		// Setting some default values.
		// Facets
		solrQuery.setFacet(true);
		// Don't return zero-counts for faceting over whole fields.
		solrQuery.add("facet.mincount", "1");
		solrQuery.add("facet.limit", "200");
		return solrQuery;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.IFacetedSearchService#getPmidsForSearch(java
	 * .lang.String, de.julielab.semedico.core.SearchState)
	 */
	@Override
	public Collection<String> getPmidsForSearch(String originalQueryString,
			SearchState searchState) {
		SolrQuery query = getSolrQuery();
		Map<FacetConfiguration, Collection<IFacetTerm>> displayedTermIds = Collections
				.emptyMap();
		adjustQuery(query, originalQueryString, searchState.getSortCriterium(),
				searchState.isReviewsFiltered(), 0, displayedTermIds);
		query.set("facet.field", IndexFieldNames.PUBMED_ID);
		QueryResponse response = performSearch(query, 0, 0);
		FacetField facetField = response
				.getFacetField(IndexFieldNames.PUBMED_ID);
		List<Count> values = facetField.getValues();
		return Collections2.transform(values, new Function<Count, String>() {
			@Override
			public String apply(Count input) {
				return input.getName();
			}
		});
	}

}
