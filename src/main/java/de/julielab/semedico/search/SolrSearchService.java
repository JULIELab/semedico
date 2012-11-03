package de.julielab.semedico.search;

import static de.julielab.semedico.IndexFieldNames.DATE;
import static de.julielab.semedico.IndexFieldNames.FACETS;
import static de.julielab.semedico.IndexFieldNames.TEXT;
import static de.julielab.semedico.IndexFieldNames.TITLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
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
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.NamedList;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.FacetedSearchResult;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.SortCriterium;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IDocumentService;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.query.IQueryDisambiguationService;
import de.julielab.semedico.query.IQueryTranslationService;
import de.julielab.semedico.query.TermAndPositionWrapper;
import de.julielab.semedico.search.interfaces.IFacetedSearchService;
import de.julielab.semedico.search.interfaces.ILabelCacheService;
import de.julielab.util.AbstractPairStream.PairTransformer;
import de.julielab.util.AbstractTripleStream.TripleTransformer;
import de.julielab.util.PairStream;
import de.julielab.util.PairTransformationStream;
import de.julielab.util.TripleStream;
import de.julielab.util.TripleTransformationStream;

public class SolrSearchService implements IFacetedSearchService {

	private TripleTransformer<Entry<String, NamedList<Integer>>, String, Integer, Integer> dfFacetCountsTransformer = new TripleTransformer<Entry<String, NamedList<Integer>>, String, Integer, Integer>() {

		@Override
		public synchronized String transformLeft(
				Entry<String, NamedList<Integer>> sourceElement) {
			return sourceElement.getKey();
		}

		@Override
		public synchronized Integer transformMiddle(
				Entry<String, NamedList<Integer>> sourceElement) {
			return sourceElement.getValue().get("tf");
		}

		@Override
		public synchronized Integer transformRight(
				Entry<String, NamedList<Integer>> sourceElement) {
			return sourceElement.getValue().get("df");
		}
	};

	private IQueryTranslationService queryTranslationService;
	private IDocumentService documentService;

	private SolrServer solr;

	int maxDocumentHits;
	private final ApplicationStateManager applicationStateManager;
	private final ILabelCacheService labelCacheService;
	private final IFacetService facetService;

	private static final String REVIEW_TERM = "Review";

	private final Logger logger;
	private final IQueryDisambiguationService queryDisambiguationService;
	private final ITermService termService;

	public SolrSearchService(
			Logger logger,
			@InjectService("SolrSearcher") SolrServer solr,
			IQueryTranslationService queryTranslationService,
			IDocumentService documentService,
			ApplicationStateManager applicationStateManager,
			ILabelCacheService labelCacheService,
			IFacetService facetService,
			ITermService stringTermService,
			IQueryDisambiguationService queryDisambiguationService,
			@Symbol(SemedicoSymbolConstants.SEARCH_MAX_NUMBER_DOC_HITS) int maxDocumentHits) {
		super();
		this.logger = logger;
		this.applicationStateManager = applicationStateManager;
		this.labelCacheService = labelCacheService;
		this.facetService = facetService;
		this.termService = stringTermService;
		this.queryDisambiguationService = queryDisambiguationService;
		this.maxDocumentHits = maxDocumentHits;
		this.solr = solr;
		this.queryTranslationService = queryTranslationService;
		this.documentService = documentService;
	}

	@Override
	public FacetedSearchResult search(String userQueryString,
			Pair<String, String> termAndFacetId, int flags) {
		StopWatch sw = new StopWatch();
		sw.start();

		SearchState searchState = applicationStateManager
				.get(SearchState.class);
		UserInterfaceState uiState = applicationStateManager
				.get(UserInterfaceState.class);
		searchState.setUserQueryString(userQueryString);
		uiState.reset();

		logger.trace("Disambiguating query.");
		Multimap<String, TermAndPositionWrapper> result = queryDisambiguationService
				.disambiguateQuery(userQueryString, termAndFacetId);
		// --------------------------------------
		// TODO this is for legacy reasons until the new query structure can be
		// used in the whole of Semedico.
		Multimap<String, IFacetTerm> disambiguatedQuery = HashMultimap.create();
		for (String key : result.keySet()) {
			Collection<TermAndPositionWrapper> collection = result.get(key);
			for (TermAndPositionWrapper wrapper : collection) {
				IFacetTerm term = wrapper.getTerm();
				disambiguatedQuery.put(key, term);
				// searchState.getQueryTermFacetMap().put(term,
				// term.getFirstFacet());
			}
		}
		// --------------------------------------

		searchState.setDisambiguatedQuery(disambiguatedQuery);
		Map<IFacetTerm, Facet> queryTermFacetMap = searchState
				.getQueryTermFacetMap();
		for (IFacetTerm queryTerm : disambiguatedQuery.values())
			queryTermFacetMap.put(queryTerm, queryTerm.getFirstFacet());

		FacetedSearchResult searchResult = search(disambiguatedQuery, flags);
		sw.stop();
		searchResult.setElapsedTime(sw.getTime());

		return searchResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.IFacetedSearchService#search(com.google.common
	 * .collect.Multimap)
	 */
	@Override
	public FacetedSearchResult search(
			Multimap<String, IFacetTerm> disambiguatedQuery, int flags) {
		StopWatch sw = new StopWatch();
		sw.start();

		// Get the state objects and set the current state.
		SearchState searchState = applicationStateManager
				.get(SearchState.class);
		UserInterfaceState uiState = applicationStateManager
				.get(UserInterfaceState.class);
		// searchState.setDisambiguatedQuery(disambiguatedQuery);
		// Map<IFacetTerm, Facet> queryTermFacetMap = searchState
		// .getQueryTermFacetMap();
		// for (IFacetTerm queryTerm : disambiguatedQuery.values())
		// queryTermFacetMap.put(queryTerm, queryTerm.getFirstFacet());
		uiState.clear();

		Map<FacetConfiguration, Collection<IFacetTerm>> displayedTermIds = uiState
				.getDisplayedTermsInSelectedFacetGroup();

		String userQueryString = searchState.getUserQueryString();
		if (userQueryString == null)
			throw new IllegalStateException(
					"The user query string is null. This method should not be called for the first search.");
		String solrQueryString = queryTranslationService.createQueryFromTerms(
				disambiguatedQuery, userQueryString);
		searchState.setSolrQueryString(solrQueryString);

		int maxNumberOfHighlightedSnippets = disambiguatedQuery.size();
		SortCriterium sortCriterium = searchState.getSortCriterium();
		boolean filterReviews = searchState.isReviewsFiltered();
		LabelStore facetHit = uiState.getLabelStore();

		// Query the Solr server, get the top-facet counts, create labels and
		// store them.
		FacetedSearchResult searchResult = search(solrQueryString,
				displayedTermIds, facetHit, maxNumberOfHighlightedSnippets,
				sortCriterium, filterReviews, flags);

		if ((flags & DO_FACET) > 0) {
			// Now that we now the top-facets, query their children in order to
			// indicate whether the top-terms have child hits.
			logger.debug("Preparing child terms of displayed terms.");
			if (!uiState.prepareLabelsForSelectedFacetGroup())
				logger.debug("No children to prepare.");
		}
		sw.stop();
		searchResult.setElapsedTime(sw.getTime());

		return searchResult;
	}

	@Override
	public FacetedSearchResult searchBTermSearchNode(
			List<Multimap<String, IFacetTerm>> searchNodes, IFacetTerm bTerm,
			int targetSNIndex) {
		StopWatch sw = new StopWatch();
		sw.start();

		// Get the state objects and set the current state.
		SearchState searchState = applicationStateManager
				.get(SearchState.class);
		UserInterfaceState uiState = applicationStateManager
				.get(UserInterfaceState.class);
		uiState.clear();

		// Plus one for the BTerm.
		int maxNumberOfHighlightedSnippets = searchNodes.get(targetSNIndex)
				.size() + 1;
		SortCriterium sortCriterium = searchState.getSortCriterium();
		boolean filterReviews = searchState.isReviewsFiltered();
		LabelStore facetHit = uiState.getLabelStore();

		String solrQueryString = queryTranslationService
				.createQueryForBTermSearchNode(searchNodes, bTerm,
						targetSNIndex);
		// Query the Solr server, get the top-facet counts, create labels and
		// store them.
		FacetedSearchResult searchResult = search(
				solrQueryString,
				Collections
						.<FacetConfiguration, Collection<IFacetTerm>> emptyMap(),
				facetHit, maxNumberOfHighlightedSnippets, sortCriterium,
				filterReviews, 0);

		sw.stop();
		searchResult.setElapsedTime(sw.getTime());

		return searchResult;
	}

	private FacetedSearchResult search(String solrQueryString,
			Map<FacetConfiguration, Collection<IFacetTerm>> displayedTermIds,
			LabelStore facetHit, int maxNumberOfHighlightedSnippets,
			SortCriterium sortCriterium, boolean filterReviews, int flags) {

		SolrQuery query = getSolrQuery(flags);

		// Adjust the user session's Solr query object to retrieve all
		// information needed. This includes facet counts, which is why we need
		// to know the IDs of currently (i.e. immediately after the current
		// search process) displayed terms in the facet boxes.
		adjustQuery(query, solrQueryString, sortCriterium, filterReviews,
				maxNumberOfHighlightedSnippets, displayedTermIds, flags);

		// Do the actual Solr search.
		QueryResponse queryResponse = performSearch(query, 0, maxDocumentHits);

		if ((flags & DO_FACET) > 0) {
			// Extract the facet counts from Solr's response and store them to
			// the
			// user's interface state object.
			storeHitFacetTermLabels(queryResponse, facetHit);
			// Store the total counts for each facet (not individual facet/term
			// counts but the counts of all hit terms of each facet).
			storeTotalFacetCounts(queryResponse, facetHit);
		}

		List<DocumentHit> documentHits = createDocumentHitsForPositions(queryResponse);
		return new FacetedSearchResult(documentHits, (int) queryResponse
				.getResults().getNumFound());
	}

	@Override
	public List<DocumentHit> constructDocumentPage(String solrQueryString,
			int start, int maxHighlightSnippets) {

		SearchState searchState = applicationStateManager
				.get(SearchState.class);
		UserInterfaceState uiState = applicationStateManager
				.get(UserInterfaceState.class);
		SolrQuery query = getSolrQuery(0);

		// Multimap<String, IFacetTerm> queryTerms =
		// searchState.getQueryTerms();
		// String rawQuery = searchState.getUserQueryString();
		// String solrQueryString =
		// queryTranslationService.createQueryFromTerms(
		// queryTerms, rawQuery);
		// String solrQueryString = searchState.getSolrQueryString();
		SortCriterium sortCriterium = searchState.getSortCriterium();
		// TODO make the reviews filter a filter like ageing filter
		boolean reviewsFiltered = searchState.isReviewsFiltered();

		// TODO But this is pageing, the term counts don't change!!
		Map<FacetConfiguration, Collection<IFacetTerm>> displayedTermIds = uiState
				.getDisplayedTermsInSelectedFacetGroup();

		adjustQuery(query, solrQueryString, sortCriterium, reviewsFiltered,
				maxHighlightSnippets, displayedTermIds, 0);

		QueryResponse queryResponse = performSearch(query, start,
				maxDocumentHits);
		return createDocumentHitsForPositions(queryResponse);
	}

	// Build Semedico DocumentHit which consists of a Semedico Document (Title,
	// Text, PMID, ...), the kwicQuery string of the disambiguated queryTerms
	// and the size of queryTerms.
	// TODO choose more appropriate name
	private List<DocumentHit> createDocumentHitsForPositions(
			QueryResponse queryResponse) {

		List<DocumentHit> documentHits = Lists.newArrayList();

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
	 * @param flags
	 */
	private void adjustQuery(SolrQuery query, String solrQueryString,
			SortCriterium sortCriterium, boolean reviewFilter,
			int maxNumberOfHighlightedSnippets,
			Map<FacetConfiguration, Collection<IFacetTerm>> displayedTerms,
			int flags) {

		query.setQuery(solrQueryString);

		if ((flags & DO_FACET) > 0) {
			// Faceting is always set to true by 'getSolrQuery()'.
			query.add(FacetParams.FACET_FIELD, FACETS);
			adjustQueryForFacetCountsInSelectedFacetGroup(query, displayedTerms);
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
				.get(UserInterfaceState.class).getSelectedFacetGroup();
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
		LabelStore labelStore = applicationStateManager.get(
				UserInterfaceState.class).getLabelStore();
		// If the facet terms should be shown in a hierarchical manner we
		// query the facet term counts directly.
		if (facetConfiguration.isHierarchical()
				&& !facetConfiguration.isForcedToFlatFacetCounts()) {
			List<String> facetTermsToQuery = new ArrayList<String>(100);
			for (IFacetTerm term : displayedTermsInFacet) {
				if (labelStore.termIdAlreadyQueried(term.getId()))
					continue;
				labelStore.addQueriedTermId(term.getId());
				// query.add("facet.query", facetConfiguration.getSource()
				// .getName() + ":" + term.getId());
				facetTermsToQuery.add(term.getId());
			}
			// Use local params to query only those terms we really want to know
			// about. This way, Solr doesn't have to build facet queries and
			// caches for those.
			query.add("facet.field",
					"{!terms=" + StringUtils.join(facetTermsToQuery, ",") + "}"
							+ facetConfiguration.getSource().getName());
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
		logger.trace("Performing Solr document search.");
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
		logger.info("Solr document search took {} ms.",
				response != null ? response.getQTime() : "<Response is null>");
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
			String solrQueryString,
			Map<FacetConfiguration, Collection<IFacetTerm>> displayedTerms,
			LabelStore facetHit) {
		SolrQuery q = getSolrQuery(DO_FACET);
		q.setQuery("*:*");
		q.setFilterQueries(solrQueryString);
		q.setRows(0);
		adjustQueryForFacetCountsInSelectedFacetGroup(q, displayedTerms);
		try {
			QueryResponse queryResponse = solr.query(q, METHOD.POST);
			logger.info(
					"Retrieving facet counts for selected facet group from Solr took {} ms.",
					queryResponse.getQTime());
			storeHitFacetTermLabels(queryResponse, facetHit);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void queryAndStoreFlatFacetCounts(String solrQueryString,
			List<FacetConfiguration> facets, LabelStore facetHit) {
		SolrQuery q = getSolrQuery(DO_FACET);
		q.setQuery("*:*");
		q.setFilterQueries(solrQueryString);
		q.setFacet(true);
		q.add("facet.mincount", "1");
		q.setRows(0);

		for (FacetConfiguration facetConfiguration : facets) {
			adjustQueryForFacetCountsInFacet(q, facetConfiguration, null);
		}

		try {
			QueryResponse queryResponse = solr.query(q);
			logger.info("Retrieving flat facet counts from Solr took {} ms.",
					queryResponse.getQTime());

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
	public void queryAndStoreHierarchichalFacetCounts(String solrQueryString,
			Multimap<FacetConfiguration, IFacetTerm> displayedTerms,
			LabelStore facetHit) {
		SolrQuery q = getSolrQuery(DO_FACET);
		q.setQuery("*:*");
		q.setFilterQueries(solrQueryString);
		q.setFacet(true);
		q.setRows(0);

		for (FacetConfiguration facetConfiguration : displayedTerms.keySet()) {
			adjustQueryForFacetCountsInFacet(q, facetConfiguration,
					displayedTerms.get(facetConfiguration));
		}
		try {
			QueryResponse queryResponse = solr.query(q, METHOD.POST);
			logger.info(
					"Retrieving hierarchical facet counts from Solr took {} ms.",
					queryResponse.getQTime());

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

	/**
	 * Stores all facet counts from <code>FacetField</code> values into the
	 * label store and marks the parents of hierarchical child terms, that have
	 * been hit, as having child hits.
	 * 
	 * @param queryResponse
	 * @param labelStore
	 */
	private void storeHitFacetTermLabels(QueryResponse queryResponse,
			LabelStore labelStore) {

		FacetGroup<FacetConfiguration> selectedFacetGroup = applicationStateManager
				.get(UserInterfaceState.class).getSelectedFacetGroup();

		Map<Integer, List<Count>> authorCounts = new HashMap<Integer, List<Count>>();
		Map<Integer, PairStream<IFacetTerm, Long>> otherCounts = new HashMap<Integer, PairStream<IFacetTerm, Long>>();
		for (FacetField facetField : queryResponse.getFacetFields()) {
			final FacetConfiguration facetConfiguration = selectedFacetGroup
					.getElementsBySourceName(facetField.getName());

			// Happens when we come over a Solr facet field which does not serve
			// a particular Semedico facet. This could be the field for total
			// facet counts, for
			// example.
			if (facetConfiguration == null)
				continue;

			final List<Count> facetValues = facetField.getValues();
			// Happens when no terms for the field are returned (e.g. when
			// there are no terms found for the facet and facet.mincount is
			// set to 1 or higher).
			if (facetValues == null)
				continue;

			final Integer facetId = facetConfiguration.getFacet().getId();
			if (facetService.isAnyAuthorFacetId(facetId))
				authorCounts.put(facetId, facetValues);
			else {
				PairStream<IFacetTerm, Long> otherTermCounts = new PairTransformationStream<Count, Collection<Count>, IFacetTerm, Long>(
						facetValues,
						new PairTransformer<Count, IFacetTerm, Long>() {

							@Override
							public IFacetTerm transformLeft(Count sourceElement) {
								IFacetTerm term = null;
								if (facetConfiguration.isFlat())
									term = termService
											.getTermObjectForStringTerm(
													sourceElement.getName(),
													facetId);
								else
									term = termService.getNode(sourceElement
											.getName());
								return term;
							}

							@Override
							public Long transformRight(Count sourceElement) {
								return sourceElement.getCount();
							}
						});
				otherCounts.put(facetId, otherTermCounts);
			}
		}

		Map<Integer, PairStream<IFacetTerm, Long>> normalizedAuthorCounts = termService
				.getTermCountsForAuthorFacets(authorCounts);
		createLabels(labelStore, normalizedAuthorCounts);
		createLabels(labelStore, otherCounts);

		// for (FacetField facetField : queryResponse.getFacetFields()) {
		// final FacetConfiguration facetConfiguration = selectedFacetGroup
		// .getElementsBySourceName(facetField.getName());
		//
		// // Happens when we come over a Solr facet field which does not serve
		// // a facet. This could be the field for total facet counts, for
		// // example.
		// if (facetConfiguration == null)
		// continue;
		//
		// List<Label> labelList = new ArrayList<Label>();
		// labelsFlat.put(facetConfiguration.getFacet().getId(), labelList);
		//
		// final List<Count> facetValues = facetField.getValues();
		// // Happens when no terms for the field are returned (e.g. when
		// // there are no terms found for the facet and facet.mincount is
		// // set to 1 or higher).
		// if (facetValues == null)
		// continue;
		// PairStream<IFacetTerm, Long> facetCountIt = null;
		//
		// if (facetConfiguration.isFlat()) {
		// Map<Count, Set<Count>> collapseAuthorNameCounts = termService
		// .normalizeAuthorNameCounts(facetValues);
		// new TripleTransformationStream<Entry<Count, Set<Count>>,
		// Collection<Entry<Count, Set<Count>>>, String, Collection<String>,
		// Long>(
		// collapseAuthorNameCounts.entrySet(),
		// new TripleTransformer<Entry<Count, Set<Count>>, String,
		// Collection<String>, Long>() {
		//
		// @Override
		// public String transformLeft(
		// Entry<Count, Set<Count>> sourceElement) {
		// return sourceElement.getKey().getName();
		// }
		//
		// @Override
		// public Collection<String> transformMiddle(
		// Entry<Count, Set<Count>> sourceElement) {
		// Collection<String> writingVariants = Collections2
		// .transform(sourceElement.getValue(),
		// new Function<Count, String>() {
		// @Override
		// public String apply(
		// Count input) {
		// return input.getName();
		// }
		// });
		// return writingVariants;
		// }
		//
		// @Override
		// public Long transformRight(
		// Entry<Count, Set<Count>> sourceElement) {
		// return sourceElement.getKey().getCount();
		// }
		//
		// });
		// } else { // Not a flat facetConfiguration
		// facetCountIt = new PairTransformationStream<Count, Collection<Count>,
		// IFacetTerm, Long>(
		// facetValues,
		// new PairTransformer<Count, IFacetTerm, Long>() {
		// @Override
		// public IFacetTerm transformLeft(Count sourceElement) {
		// return termService.getNode(sourceElement
		// .getName());
		// }
		//
		// @Override
		// public Long transformRight(Count sourceElement) {
		// return sourceElement.getCount();
		// }
		// });
		// }
		//
		// while (facetCountIt.incrementTuple()) {
		// IFacetTerm term = facetCountIt.getLeft();
		// long count = facetCountIt.getRight();
		//
		// Label label = null;
		// if (facetConfiguration.getFacet().isFlat())
		// label = labelCacheService.getCachedLabel(term);
		// else {
		// // If we have a facet which genuinely contains
		// // (hierarchical) terms but is set to flat state, we do
		// // not only get a label and put in the list.
		// // Additionally we put it into the hierarchical labels
		// // map and resolve child term hits for parents.
		// label = labelsHierarchical.get(term);
		// if (label == null) {
		// label = labelCacheService.getCachedLabel(term);
		// labelsHierarchical.put(term.getId(), (TermLabel) label);
		// resolveChildHitsRecursively(
		// ((TermLabel) label).getTerm(),
		// labelsHierarchical);
		// }
		// }
		// label.setCount(count);
		// labelList.add(label);
		// }
		// Collections.sort(labelList);
		//
		// }
	}

	/**
	 * @param labelsHierarchical
	 * @param labelsFlat
	 * @param termCountsByFacetId
	 */
	private void createLabels(LabelStore labelStore,
			Map<Integer, PairStream<IFacetTerm, Long>> termCountsByFacetId) {
		// One single Map to associate with each queried term id its facet
		// count.
		Map<String, TermLabel> labelsHierarchical = labelStore
				.getLabelsHierarchical();
		// Facet-ID-associated Label lists for per-field frequency-ordered facet
		// counts.
		Map<Integer, List<Label>> labelsFlat = labelStore.getLabelsFlat();

		for (Integer facetId : termCountsByFacetId.keySet()) {
			PairStream<IFacetTerm, Long> termCounts = termCountsByFacetId
					.get(facetId);

			Facet facet = facetService.getFacetById(facetId);

			List<Label> labelList = new ArrayList<Label>();
			labelsFlat.put(facetId, labelList);

			while (termCounts.incrementTuple()) {
				IFacetTerm term = termCounts.getLeft();
				long count = termCounts.getRight();

				Label label = null;
				if (facet.isFlat())
					label = labelCacheService.getCachedLabel(term);
				else {
					// If we have a facet which genuinely contains
					// (hierarchical) terms but is set to flat state, we do
					// not only get a label and put in the list.
					// Additionally we put it into the hierarchical labels
					// map and resolve child term hits for parents.
					label = labelsHierarchical.get(term);
					if (label == null) {
						label = labelCacheService.getCachedLabel(term);
						labelsHierarchical.put(term.getId(), (TermLabel) label);
					}
				}
				label.setCount(count);
				labelList.add(label);
			}
			labelStore.resolveChildHitsRecursively();
			Collections.sort(labelList);
		}
	}

	/**
	 * @param queryResponse
	 * @param labelStore
	 */
	private void storeTotalFacetCounts(QueryResponse queryResponse,
			LabelStore labelStore) {

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
				labelStore.setTotalFacetCount(facet, 0);
		}

		for (FacetField field : queryResponse.getFacetFields()) {
			// This field has no hit facets. When no documents were found,
			// no field will have any hits.
			if (field.getValues() == null)
				continue;
			// The facet category counts, e.g. for "Proteins and Genes".
			else if (facetService.isTotalFacetCountField(field.getName())) {
				// Iterate over the actual facet counts.
				for (Count count : field.getValues()) {
					Facet facet = facetService.getFacetById(Integer
							.parseInt(count.getName()));
					labelStore.setTotalFacetCount(facet, count.getCount());
				}
			}
		}
	}

	private SolrQuery getSolrQuery(int flags) {
		SolrQuery solrQuery = applicationStateManager.get(SearchState.class)
				.getSolrQuery();
		solrQuery.clear();
		// Setting some default values.
		if ((flags & DO_FACET) > 0) {
			// Facets
			solrQuery.setFacet(true);
			// Don't return zero-counts for faceting over whole fields.
			solrQuery.add("facet.mincount", "1");
			solrQuery.setFacetLimit(100);
		}
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
		SolrQuery query = getSolrQuery(DO_FACET);
		Map<FacetConfiguration, Collection<IFacetTerm>> displayedTermIds = Collections
				.emptyMap();
		adjustQuery(query, originalQueryString, searchState.getSortCriterium(),
				searchState.isReviewsFiltered(), 0, displayedTermIds, 0);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.search.interfaces.IFacetedSearchService#
	 * getAllTermsInField(java.lang.String)
	 */
	@Override
	public TripleStream<String, Integer, Integer> getSearchNodeTermsInField(
			List<Multimap<String, IFacetTerm>> searchNodes, int targetSNIndex,
			String field) {
		logger.debug(
				"Retrieving search node terms in field {} for search node {}...",
				field, targetSNIndex);
		String solrQueryString = queryTranslationService
				.createQueryForSearchNode(searchNodes, targetSNIndex);
		SolrQuery solrQuery = getSolrQuery(DO_FACET);
		solrQuery.setQuery(solrQueryString);
		solrQuery.setFacetLimit(-1);
		solrQuery.setFacetMinCount(1);
		solrQuery.set("facet.field", field);
		solrQuery.set("facet.method", "enum");
		solrQuery.set("facetdf", "true");
		solrQuery.setFacetSort("index");
		QueryResponse response = performSearch(solrQuery, 0, 0);

		// Extract the facet-document-frequency information from the response;
		// since the facetdf component is a custom component
		// (julie-solr-facet-df-component), the response cannot be retrieved by
		// a SolrJ API call.
		@SuppressWarnings("unchecked")
		NamedList<NamedList<NamedList<NamedList<Integer>>>> facetDfCounts = (NamedList<NamedList<NamedList<NamedList<Integer>>>>) response
				.getResponse().get("facet_df_counts");
		NamedList<NamedList<NamedList<Integer>>> fieldDfCounts = facetDfCounts
				.get("facet_field_df_counts");
		NamedList<NamedList<Integer>> bTermsDfCounts = fieldDfCounts.get(field);

		TripleTransformationStream<Entry<String, NamedList<Integer>>, Iterable<Entry<String, NamedList<Integer>>>, String, Integer, Integer> bTermTransformationStream = new TripleTransformationStream<Entry<String, NamedList<Integer>>, Iterable<Entry<String, NamedList<Integer>>>, String, Integer, Integer>(
				bTermsDfCounts, dfFacetCountsTransformer);

		logger.debug("{} terms returned.", bTermsDfCounts.size());
		return bTermTransformationStream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.interfaces.IFacetedSearchService#getNumDocs()
	 */
	@Override
	public long getNumDocs() {
		QueryResponse response = performSearch(new SolrQuery("*:*"), 0, 0);
		long numDocs = response.getResults().getNumFound();
		return numDocs;
	}

}
