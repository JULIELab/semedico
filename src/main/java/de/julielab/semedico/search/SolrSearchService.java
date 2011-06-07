package de.julielab.semedico.search;

import static de.julielab.semedico.IndexFieldNames.DATE;
import static de.julielab.semedico.IndexFieldNames.FACET_CATEGORIES;
import static de.julielab.semedico.IndexFieldNames.FACET_TERMS;
import static de.julielab.semedico.IndexFieldNames.TEXT;
import static de.julielab.semedico.IndexFieldNames.TITLE;

import static de.julielab.semedico.core.services.SemedicoSymbolProvider.SEARCH_MAX_NUMBER_DOC_HITS;
import static de.julielab.semedico.core.services.SemedicoSymbolProvider.SEARCH_MAX_FACETTED_DOCS;

import java.io.IOException;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.tapestry5.ioc.annotations.Symbol;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.FacettedSearchResult;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.core.SortCriterium;
import de.julielab.semedico.core.services.IDocumentCacheService;
import de.julielab.semedico.core.services.IDocumentService;
import de.julielab.semedico.query.IQueryTranslationService;

public class SolrSearchService implements IFacettedSearchService {

	private IQueryTranslationService queryTranslationService;
	private IFacetHitCollectorService facetHitCollectorService;
	private IKwicService kwicService;
	private IDocumentCacheService documentCacheService;
	private IDocumentService documentService;

	private SolrServer solr;
	private SolrQuery query;

	private int maxFacettedDocuments;
	int maxDocumentHits;

	public SolrSearchService(SolrServer solr,
			IQueryTranslationService queryTranslationService,
			IFacetHitCollectorService facetHitCollectorService,
			IDocumentCacheService documentCacheService,
			IDocumentService documentService,
			IKwicService kwicService,
			@Symbol(SEARCH_MAX_NUMBER_DOC_HITS) int maxFacettedDocuments,
			@Symbol(SEARCH_MAX_FACETTED_DOCS) int maxDocumentHits) {
		super();
		this.maxFacettedDocuments = maxFacettedDocuments;
		this.maxDocumentHits = maxDocumentHits;
		this.solr = solr;
		this.queryTranslationService = queryTranslationService;
		this.facetHitCollectorService = facetHitCollectorService;
		this.documentCacheService = documentCacheService;
		this.documentService = documentService;
		this.kwicService = kwicService;
		query = new SolrQuery();
	}

	@Override
	public FacettedSearchResult search(
			Collection<FacetConfiguration> facetConfigurations,
			Multimap<String, FacetTerm> queryTerms,
			SortCriterium sortCriterium, boolean filterReviews)
			throws IOException {

		String solrQueryString = queryTranslationService
				.createQueryFromTerms(queryTerms);
		buildQuery(solrQueryString, sortCriterium, filterReviews,
				queryTerms.size());

		QueryResponse queryResponse = performSearch(0, maxDocumentHits);

		facetHitCollectorService.setFacetFieldList(queryResponse
				.getFacetFields());
		FacetHit facetHit = facetHitCollectorService
				.collectFacetHits(facetConfigurations);

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

	private void buildQuery(String queryString, SortCriterium sortCriterium,
			boolean reviewFilter, int maxNumberOfHighlightedSnippets) {
		query.clear();
		query.setQuery(queryString);

		// Facets
		query.setFacet(true);
		// Collect term counts over all fields which contain facet terms.
		// TODO store field names in an appropriate Constant
		query.add("facet.field", FACET_TERMS);
		query.add("facet.field", FACET_CATEGORIES);
		query.add("facet.limit", "-1");
		query.add("facet.mincount", "1");

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

	private QueryResponse performSearch(int start, int rows) {
		query.setStart(start);
		query.setRows(rows);

		QueryResponse response = null;
		try {
			response = solr.query(query);
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return response;
	}

	public Collection<DocumentHit> constructDocumentPage(int start) {
		query.setStart(start);
		query.setRows(maxDocumentHits);
		QueryResponse queryResponse = performSearch(start, maxDocumentHits);
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

	public IKwicService getKwicService() {
		return kwicService;
	}

	public void setKwicService(IKwicService kwicService) {
		this.kwicService = kwicService;
	}

	public IDocumentCacheService getDocumentCacheService() {
		return documentCacheService;
	}

	public void setDocumentCacheService(
			IDocumentCacheService documentCacheService) {
		this.documentCacheService = documentCacheService;
	}

	public IDocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(IDocumentService documentService) {
		this.documentService = documentService;
	}

	@Override
	public int getIndexSize() {
		SolrQuery allQuery = new SolrQuery("*:*");
		try {
			return (int) solr.query(allQuery).getResults().getNumFound();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return -1;
	}

}
