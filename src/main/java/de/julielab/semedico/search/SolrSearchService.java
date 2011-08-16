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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	public FacettedSearchResult search(Multimap<String, IFacetTerm> queryTerms, String rawQuery,
			SortCriterium sortCriterium, boolean filterReviews)
			throws IOException {

		String solrQueryString = queryTranslationService
				.createQueryFromTerms(queryTerms, rawQuery);
		List<String> displayedTermIds = getDisplayedTermIds();
		SolrQuery query = applicationStateManager
				.get(SearchSessionState.class).getSearchState().getSolrQuery();
		adjustQuery(query, solrQueryString, sortCriterium,
				filterReviews, queryTerms.size(), displayedTermIds);

		QueryResponse queryResponse = performSearch(query, 0, maxDocumentHits);

		Map<String, Label> hitFacetTermLabels = getHitFacetTermLabels(queryResponse);

		SearchSessionState searchConfiguration = applicationStateManager
				.get(SearchSessionState.class);
		UserInterfaceState uiState = searchConfiguration.getUiState();
		FacetHit facetHit = uiState.getFacetHit();
		facetHit.setLabels(hitFacetTermLabels);
		
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

		// facetHitCollectorService.setFacetFieldList(queryResponse
		// .getFacetFields());
		// FacetHit facetHit = facetHitCollectorService
		// .collectFacetHits(queryTerms);

		// FacetHit facetHit = facetHits.get(0);
		// ILabelCacheService labelCacheService =
		// facetHit.getLabelCacheService();
		// System.out.println("SolrSearchService, latestSearch: " +
		// labelCacheService.getLastSearchTimestamp());
		// for (Label l : labelCacheService.getNodes())
		// if (l.getHits() != null && l.getHits() > 0)
		// System.out.println("SolrSearchService: " + l);

		Collection<DocumentHit> documentHits = createDocumentHitsForPositions(queryResponse);
		return new FacettedSearchResult(facetHit, documentHits,
				(int) queryResponse.getResults().getNumFound());
	}

	// TODO should this be synchonized?!
	private void adjustQuery(SolrQuery query, String queryString,
			SortCriterium sortCriterium, boolean reviewFilter,
			int maxNumberOfHighlightedSnippets, List<String> displayedTermIds) {
		
		query.setQuery(queryString);

		// Facets
		query.setFacet(true);
		// Collect term counts over all fields which contain facet terms.
		// query.add("facet.field", FACET_TERMS);
		query.add("facet.field", FACETS);
		for (String id : displayedTermIds) {
			query.add("facet.query", FACET_TERMS + ":" + id);
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
		SolrQuery query = applicationStateManager
				.get(SearchSessionState.class).getSearchState().getSolrQuery();

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

	@Override
	public Map<String, Label> getHitFacetTermLabelsForFacetGroup(
			FacetGroup facetGroup) {
		List<String> displayedTermIds = new ArrayList<String>();
		for (Facet facet : facetGroup)
			getDisplayedTermIdsForFacet(displayedTermIds, facet);
		return getFacetCountsForTermIds(displayedTermIds);
	}

	/**
	 * @param displayedTermIds
	 * @return
	 */
	@Override
	public Map<String, Label> getFacetCountsForTermIds(
			List<String> displayedTermIds) {
		Map<String, Label> hitFacetTermLabels = new HashMap<String, Label>();

		SearchState searchState = applicationStateManager
				.get(SearchSessionState.class).getSearchState();
		String strQ = queryTranslationService
				.createQueryFromTerms(searchState.getQueryTerms(), searchState.getRawQuery());
		SolrQuery q = new SolrQuery("*:*");
		q.setFilterQueries(strQ);
		q.setFacet(true);
		q.setRows(0);
		for (String id : displayedTermIds) {
			q.add("facet.query", "facetTerms:" + id);
		}
		try {
			QueryResponse queryResponse = solr.query(q, METHOD.POST);
			for (String id : queryResponse.getFacetQuery().keySet()) {
				String termId = id.split(":")[1];
				Integer count = queryResponse.getFacetQuery().get(id);
				if (count == null)
					count = 0;
				Label label = labelCacheService.getCachedLabel(termId);
				label.setHits(new Long(count));
				hitFacetTermLabels.put(termId, label);
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hitFacetTermLabels;
	}

	private List<String> getDisplayedTermIds() {
		UserInterfaceState uiState = applicationStateManager
				.get(SearchSessionState.class).getUiState();
		List<String> displayedTermIds = new ArrayList<String>();
		for (Facet facet : uiState.getSelectedFacetGroup()) {
			getDisplayedTermIdsForFacet(displayedTermIds, facet);
		}
		return displayedTermIds;
	}

	/**
	 * @param searchConfiguration
	 * @param displayedTermIds
	 * @param facet
	 */
	private void getDisplayedTermIdsForFacet(List<String> displayedTermIds,
			Facet facet) {
		UserInterfaceState uiState = applicationStateManager
				.get(SearchSessionState.class).getUiState();
		Map<Facet, FacetConfiguration> facetConfigurations = uiState
				.getFacetConfigurations();
		FacetConfiguration facetConfiguration = facetConfigurations.get(facet);
		if (facetConfiguration.isDrilledDown()) {
			IFacetTerm lastPathTerm = facetConfiguration.getLastPathElement();
			IFacetTerm term = termService.getNode(lastPathTerm.getId());
			Iterator<IFacetTerm> childIt = term.childIterator();
			while (childIt.hasNext())
				displayedTermIds.add(childIt.next().getId());

		} else {
			Iterator<IFacetTerm> rootIt = termService.getFacetRoots(
					facetConfiguration.getFacet()).iterator();
			while (rootIt.hasNext())
				displayedTermIds.add(rootIt.next().getId());
		}
	}

	private Map<String, Label> getHitFacetTermLabels(QueryResponse queryResponse) {
		Map<String, Label> hitFacetTermLabels = new HashMap<String, Label>();

		for (String id : queryResponse.getFacetQuery().keySet()) {
			String termId = id.split(":")[1];
			Integer count = queryResponse.getFacetQuery().get(id);
			if (count == null)
				count = 0;
			Label label = labelCacheService.getCachedLabel(termId);
			label.setHits(new Long(count));
			hitFacetTermLabels.put(termId, label);
		}

		return hitFacetTermLabels;
	}

}
