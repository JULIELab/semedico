package de.julielab.stemnet.search;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.OpenBitSet;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.julielab.stemnet.core.SemedicoDocument;
import de.julielab.stemnet.core.DocumentHit;
import de.julielab.stemnet.core.FacetConfiguration;
import de.julielab.stemnet.core.FacetHit;
import de.julielab.stemnet.core.FacettedSearchResult;
import de.julielab.stemnet.core.SortCriterium;
import de.julielab.stemnet.core.FacetTerm;
import de.julielab.stemnet.core.services.IDocumentCacheService;
import de.julielab.stemnet.core.services.IDocumentService;
import de.julielab.stemnet.query.IQueryTranslationService;

public class FacettedSearchServiceTest {
	
	private ISearchService searchService;
	private IQueryTranslationService queryTranslationService;
	private IFacetHitCollectorService facetHitCollectorService;
	private IDocumentSetLimitizerService documentSetLimitizerService;
	private IKwicService kwicService;
	private IDocumentCacheService documentCacheService;
	private IDocumentService documentService;
	private FacettedSearchService facettedSearchService;
	private Multimap<String, FacetTerm> queryTerms;
	private Collection<FacetConfiguration> facetConfigurations;
	private Query query;
	private SortCriterium sortCriterium;
	private TopDocs topDocs;
	private OpenBitSet limitedDocuments;
	private List<FacetHit> facetHits;
	private SemedicoDocument document;
	private DocumentHit documentHit;
	private String kwicQuery;
	
	@Before
	public void setUp(){
		searchService = createMock(ISearchService.class);
		queryTranslationService = createMock(IQueryTranslationService.class);
		facetHitCollectorService = createMock(IFacetHitCollectorService.class);
		documentSetLimitizerService = createMock(IDocumentSetLimitizerService.class);
		kwicService = createMock(IKwicService.class);
		documentCacheService = createMock(IDocumentCacheService.class);
		documentService = createMock(IDocumentService.class);
		
		facettedSearchService = new FacettedSearchService(3,3);
		facettedSearchService.setSearchService(searchService);
		facettedSearchService.setQueryTranslationService(queryTranslationService);
		facettedSearchService.setFacetHitCollectorService(facetHitCollectorService);
		facettedSearchService.setDocumentSetLimitizerService(documentSetLimitizerService);
		facettedSearchService.setKwicService(kwicService);
		facettedSearchService.setDocumentCacheService(documentCacheService);
		facettedSearchService.setDocumentService(documentService);

		facettedSearchService.setMaxDocumentHits(3);
		facettedSearchService.setMaxFacettedDocuments(3);
		
		queryTerms = HashMultimap.create();
		queryTerms.put("term", new FacetTerm("term"));
		
		facetConfigurations = new ArrayList<FacetConfiguration>();
		sortCriterium = SortCriterium.RELEVANCE;
		ScoreDoc[] scoreDocs = new ScoreDoc[]{new ScoreDoc(1,3)};
		topDocs = new TopDocs(5, scoreDocs, 4);
		limitedDocuments = new OpenBitSet();
		
		facetHits = Lists.newArrayList();
		document = new SemedicoDocument(1);
		documentHit = new DocumentHit(document);
		kwicQuery = "";
	}
	
//	@Test
//	public void testSearch() throws IOException{
//		expect(queryTranslationService.createQueryFromTerms(queryTerms)).andReturn(query);
//		expect(searchService.processQuery(same(query), isA(OpenBitSet.class) , same(sortCriterium), eq(false))).andReturn(topDocs);
//		expect(documentSetLimitizerService.limitDocumentSetWithIncludedScoreDocs(isA(OpenBitSet.class), same(topDocs.scoreDocs), eq(3))).andReturn(limitedDocuments);
//		expect(facetHitCollectorService.collectFacetHits(facetConfigurations, limitedDocuments)).andReturn(facetHits);
//		expect(documentCacheService.getCachedDocument(1)).andReturn(null);
//		expect(documentService.readDocumentWithLuceneId(1)).andReturn(document);
//		documentCacheService.addDocument(document);
//		expect(queryTranslationService.createKwicQueryFromTerms(queryTerms)).andReturn(kwicQuery);
//		expect(kwicService.createDocumentHit(document, kwicQuery, 1)).andReturn(documentHit);
//		
//		replay(queryTranslationService);
//		replay(searchService);
//		replay(documentSetLimitizerService);
//		replay(facetHitCollectorService);
//		replay(documentCacheService);
//		replay(documentService);
//		replay(kwicService);
//		
//		FacettedSearchResult result = facettedSearchService.search(facetConfigurations, queryTerms, sortCriterium, false);
//		
//		verify(queryTranslationService);
//		verify(searchService);
//		verify(documentSetLimitizerService);
//		verify(facetHitCollectorService);
//		verify(documentCacheService);
//		verify(documentService);
//		verify(kwicService);
//
//		Collection<DocumentHit> documentHits = result.getDocumentHits();
//		assertEquals(1, documentHits.size());
//		assertEquals(documentHit, documentHits.iterator().next());
//		assertEquals(facetHits, result.getFacetHits());
//		assertNotNull(result.getDocuments());
//		assertEquals(5, result.getTotalHits());
//	}
}
