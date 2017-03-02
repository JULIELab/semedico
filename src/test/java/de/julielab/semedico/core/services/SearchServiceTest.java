package de.julielab.semedico.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.julielab.elastic.query.components.ISearchComponent;
import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.interfaces.IFacetTerm;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetLabels.General;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.facets.UIFacetGroup;
import de.julielab.semedico.core.facetterms.AggregateTerm;
import de.julielab.semedico.core.parsing.BinaryNode;
import de.julielab.semedico.core.parsing.Node.NodeType;
import de.julielab.semedico.core.parsing.ParseErrors;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.query.UserQuery;
import de.julielab.semedico.core.search.annotations.TermDocumentFrequencyChain;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;
import de.julielab.semedico.core.search.components.data.LabelStore;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchResult;
import de.julielab.semedico.core.search.interfaces.ILabelCacheService;
import de.julielab.semedico.core.services.TermNeo4jService.TermCacheLoader;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.query.QueryTokenizerImpl;
import de.julielab.semedico.core.util.LazyDisplayGroup;
import de.julielab.semedico.core.util.TripleStream;

public class SearchServiceTest {
	private static final Logger log = LoggerFactory.getLogger(SearchService.class);

	private static Registry registry;
	private static ISearchService searchService;
	private static AsyncCacheLoader<String, IConcept>.LoadingWorkerReference loadingWorkerReference;

	@BeforeClass
	public static void setup() {
		org.junit.Assume.assumeTrue(TestUtils.isAddressReachable(TestUtils.neo4jTestEndpoint));
		org.junit.Assume.assumeTrue(TestUtils.isAddressReachable(TestUtils.searchServerUrl));

		// For the testNewDocumentHighlighting test we want to get a few more
		// documents back to have our test cases
		// returned.
		System.setProperty(SemedicoSymbolConstants.SEARCH_MAX_NUMBER_DOC_HITS, "40");
		registry = TestUtils.createTestRegistry();
		searchService = registry.getService(ISearchService.class);
		loadingWorkerReference = registry.getService(TermCacheLoader.class).getLoadingWorkerReference();
	}
	

	@Ignore
	@Deprecated
	@Test
	public void setSuggestionSearch2() throws InterruptedException, ExecutionException {
		// This test checks that the event token types are correctly entered
		// into the index
		LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) searchService.doSuggestionSearch("binding", null).get();
		ArrayList<FacetTermSuggestionStream> suggestions = searchResult.suggestions;
		assertTrue(suggestions.size() > 0);
		FacetTermSuggestionStream suggestionStream = suggestions.get(0);
		while (suggestionStream.incrementTermSuggestion()) {
			assertEquals("binding", suggestionStream.getTermName());
			assertEquals(QueryTokenizerImpl.UNARY_OR_BINARY_EVENT, suggestionStream.getLexerType());
		}
	}

	@Ignore
	@Deprecated
	@Test
	public void testNewDocumentSearchWithoutSuggestion() throws InterruptedException, ExecutionException {
		TermCacheLoader cacheLoader = registry.getService(TermCacheLoader.class);
		AsyncCacheLoader<String, IConcept>.LoadingWorkerReference loadingWorkerReference = cacheLoader
				.getLoadingWorkerReference();

		// Should be recognized as a term (Leukocytes)
		String userQuery = "white blood cells";
		SearchState searchState = new SearchState();
		UserInterfaceState uiState = getUiState();

		UserQuery uq = new UserQuery(userQuery);
		LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) searchService.doNewDocumentSearch(uq, searchState, uiState).get();

		// Check the recognition of the term.
		ParseTree semedicoQuery = searchState.getSemedicoQuery();
		assertEquals(1, semedicoQuery.getNumberConceptNodes());

		loadingWorkerReference.interruptAndJoin();

		List<? extends IConcept> terms = semedicoQuery.getConceptNodes().get(0).asTextNode().getTerms();
		assertEquals(1, terms.size());
		IConcept term = terms.get(0);
		assertEquals(NodeIDPrefixConstants.TERM + 17, term.getId());
		assertEquals("Leukocytes", term.getPreferredName());

		assertNotNull(searchResult);
		LazyDisplayGroup<HighlightedSemedicoDocument> documentHits = searchResult.documentHits;
		Collection<HighlightedSemedicoDocument> displayedObjects = documentHits.getDisplayedObjects();
		for (HighlightedSemedicoDocument hit : displayedObjects) {
			assertNotNull(hit);
			assertFalse(StringUtils.isBlank(hit.getKwicTitle()));
			assertNotNull(hit.getDocument());
			assertNotNull(hit.getDocument().getAuthors());
		}

		assertEquals("Number of hits", new Long(171), new Long(documentHits.getTotalSize()));

		loadingWorkerReference.interruptAndJoin();
	}

	@Ignore
	@Deprecated
	@Test
	public void testNewDocumentSearchWithAggregatedTerm() throws InterruptedException, ExecutionException {
		TermCacheLoader cacheLoader = registry.getService(TermCacheLoader.class);
		AsyncCacheLoader<String, IConcept>.LoadingWorkerReference loadingWorkerReference = cacheLoader
				.getLoadingWorkerReference();

		// cell is an aggregated term; i.e. there is a class 'cell' in GO and in
		// GRO. They are mapped by the LOOM
		// algorithm and thus are aggregated.
		String userQuery = "cell";
		SearchState searchState = new SearchState();
		UserInterfaceState uiState = getUiState();

		UserQuery uq = new UserQuery(userQuery);
		LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) searchService.doNewDocumentSearch(uq, searchState, uiState).get();

		// Check the recognition of the term.
		ParseTree semedicoQuery = searchState.getSemedicoQuery();
		assertEquals(1, semedicoQuery.getNumberConceptNodes());

		loadingWorkerReference.interruptAndJoin();

		List<? extends IConcept> terms = semedicoQuery.getConceptNodes().get(0).asTextNode().getTerms();
		assertEquals(1, terms.size());
		IConcept term = terms.get(0);
		assertEquals(AggregateTerm.class, term.getClass());
		assertEquals(NodeIDPrefixConstants.AGGREGATE_TERM + 0, term.getId());
		assertEquals("cell", term.getPreferredName());

		assertNotNull(searchResult);
		LazyDisplayGroup<HighlightedSemedicoDocument> documentHits = searchResult.documentHits;
		Collection<HighlightedSemedicoDocument> displayedObjects = documentHits.getDisplayedObjects();
		for (HighlightedSemedicoDocument hit : displayedObjects) {
			assertNotNull(hit);
			assertFalse(StringUtils.isBlank(hit.getKwicTitle()));
			assertNotNull(hit.getDocument());
			assertNotNull(hit.getDocument().getAuthors());
		}
		assertEquals("Number of hits", new Long(589), new Long(documentHits.getTotalSize()));
		loadingWorkerReference.interruptAndJoin();
	}

	@Ignore
	@Deprecated
	@Test
	public void testNewDocumentHighlighting() throws InterruptedException, ExecutionException {
		TermCacheLoader cacheLoader = registry.getService(TermCacheLoader.class);
		AsyncCacheLoader<String, IConcept>.LoadingWorkerReference loadingWorkerReference = cacheLoader
				.getLoadingWorkerReference();

		IFacetService facetService = registry.getService(IFacetService.class);

		// Medicine isn't an entry in the test query dictionary; it should be
		// recognized as a keyword and then
		// lowercased and stemmed by the query parsing algorithm and then find
		// some highlighted documents.
		String userQuery = "Medicine";
		SearchState searchState = new SearchState();
		UserInterfaceState uiState = getUiState();

		UserQuery uq = new UserQuery(userQuery);
		LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) searchService.doNewDocumentSearch(uq, searchState, uiState).get();

		// Check the recognition of the term.
		ParseTree semedicoQuery = searchState.getSemedicoQuery();
		assertEquals(new Integer(1), new Integer(semedicoQuery.getNumberConceptNodes()));
		List<? extends IConcept> terms = semedicoQuery.getConceptNodes().get(0).asTextNode().getTerms();
		assertEquals(1, terms.size());
		IConcept term = terms.get(0);
		assertEquals(facetService.getKeywordFacet(), term.getFirstFacet());
		assertEquals("medicin", term.getId());

		// loadingWorkerReference.interruptAndJoin();

		assertNotNull(searchResult);
		// The most hits actually come from the journal titles. Only 7 hits come
		// from text or document title.
		assertEquals(new Long(39), new Long(searchResult.documentHits.getTotalSize()));
		LazyDisplayGroup<HighlightedSemedicoDocument> documentHits = searchResult.documentHits;
		Collection<HighlightedSemedicoDocument> displayedObjects = documentHits.getDisplayedObjects();

		Integer expectedDocumentsFound = 0;
		for (HighlightedSemedicoDocument hit : displayedObjects) {
			assertNotNull(hit);
			String pubmedId = hit.getDocument().getDocId();
			if (pubmedId.equals("21826600")) {
				log.debug("Document {} found", pubmedId);
				expectedDocumentsFound++;
				assertTrue(hit.hasTitleKeywords());
				assertTrue(hit.hasAbstractKeywords());
				assertEquals(new Integer(1), new Integer(hit.getNumberAbstractKwics()));
			} else if (pubmedId.equals("22348515")) {
				log.debug("Document {} found", pubmedId);
				expectedDocumentsFound++;
				assertFalse(hit.hasTitleKeywords());
				assertTrue(hit.hasAbstractKeywords());
				assertEquals(new Integer(1), new Integer(hit.getNumberAbstractKwics()));
			} else if (pubmedId.equals("23387296")) {
				log.debug("Document {} found", pubmedId);
				expectedDocumentsFound++;
				assertFalse(hit.hasTitleKeywords());
				assertTrue(hit.hasAbstractKeywords());
				assertEquals(new Integer(1), new Integer(hit.getNumberAbstractKwics()));
			}
		}
		assertEquals(new Integer(3), expectedDocumentsFound);

		loadingWorkerReference.interruptAndJoin();
	}

	@Ignore
	@Deprecated
	@Test
	public void testNewDocumentSearchWithEvents() throws InterruptedException, ExecutionException {

		SearchState searchState = new SearchState();
		UserInterfaceState uiState = getUiState();

		// Query String is empty. Only search for event.
		String userQuery = "mapk14 regulates mef2c";
		UserQuery uq = new UserQuery(userQuery);
		LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) searchService
				.doNewDocumentSearch(uq, Lists.newArrayList(IIndexInformationService.ABSTRACT), searchState, uiState)
				.get();

		assertNotNull(searchResult);
		assertEquals(new Long(1), new Long(searchResult.documentHits.getTotalSize()));

		// Search for query String as well as for event.
		userQuery = "mapk14 regulates mef2c or inhibitor";
		uq = new UserQuery(userQuery);
		searchResult = (LegacySemedicoSearchResult) searchService
				.doNewDocumentSearch(uq, Lists.newArrayList(IIndexInformationService.ABSTRACT), searchState, uiState)
				.get();
		assertNotNull(searchResult);
		// 155 hits for "inhibitor" and 1 for the event
		assertEquals(new Long(156), new Long(searchResult.documentHits.getTotalSize()));
		LazyDisplayGroup<HighlightedSemedicoDocument> documentHits = searchResult.documentHits;
		Collection<HighlightedSemedicoDocument> displayedObjects = documentHits.getDisplayedObjects();
		ArrayList<HighlightedSemedicoDocument> hits = new ArrayList<HighlightedSemedicoDocument>(displayedObjects);
		HighlightedSemedicoDocument hit = hits.get(0);
		assertNotNull(hit);
		assertEquals(new Integer(23788171), hit.getDocument().getDocId());

	}
	@Ignore
	@Deprecated
	@Test
	public void testSearchForEventType() throws Exception {
		TextNode textNode = new TextNode("regulation");
		ITermService termService = registry.getService(ITermService.class);
		IFacetService facetService = registry.getService(IFacetService.class);
		IFacetTerm regulationTerm = (IFacetTerm) termService.getTerm(NodeIDPrefixConstants.TERM + 1842);
		textNode.setTerms(Lists.newArrayList(regulationTerm));
		Facet eventFacet = facetService.getFacetsByLabel(General.EVENTS).get(0);
		textNode.setFacetMapping(regulationTerm, eventFacet);

		ParseTree parseTree = new ParseTree(textNode, new ParseErrors());
		searchService.doTermSelectSearch(parseTree, new SearchState(), getUiState());
	}
	@Ignore
	@Deprecated
	@Test
	public void testArticleSearch() throws InterruptedException, ExecutionException {
		LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) searchService
				.doArticleSearch("10089566", IIndexInformationService.Indexes.DocumentTypes.medline, null).get();
		HighlightedSemedicoDocument article = searchResult.semedicoDoc;
		assertNotNull(article);
		assertEquals("10089566", article.getDocument().getDocId());
		assertFalse(StringUtils.isBlank(article.getDocument().getAbstractText()));
		assertFalse(StringUtils.isBlank(article.getDocument().getTitle()));

		// Probably not needed; just to be sure.
		loadingWorkerReference.interruptAndJoin();
	}
	@Test
	public void testArticleSearchWithHighlight() throws InterruptedException, ExecutionException {
		TextNode textNode = new TextNode("cyclase");
		ParseTree parseTree = new ParseTree(textNode, new ParseErrors());
		LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) searchService
				.doArticleSearch("10089566", IIndexInformationService.Indexes.DocumentTypes.medline, parseTree).get();
		HighlightedSemedicoDocument article = searchResult.semedicoDoc;
		assertNotNull(article);
		assertEquals("10089566", article.getDocument().getDocId());
		assertFalse(StringUtils.isBlank(article.getDocument().getAbstractText()));
		assertFalse(StringUtils.isBlank(article.getDocument().getTitle()));
		
		assertNotNull(article.getHighlightedAbstract());
		assertNotNull(article.getTitleHighlight());
		assertTrue(article.getTitleHighlight().highlight.contains("<span class=\"highlightFull\">cyclase</span>"));

		// Probably not needed; just to be sure.
		loadingWorkerReference.interruptAndJoin();
	}
	
	@Test
	public void testArticleSearchWithHighlight2() throws InterruptedException, ExecutionException {
//		TextNode textNode = new TextNode("tid3270");
//		ParseTree parseTree = new ParseTree(textNode, new ParseErrors());
//		SemedicoSearchResult searchResult = searchService
//				.doArticleSearch("19149881", IIndexInformationService.Indexes.DocumentTypes.medline, parseTree).get();
//		HighlightedSemedicoDocument article = searchResult.semedicoDoc;
//		assertNotNull(article);
//		assertEquals("19149881", article.getDocument().getDocId());
//		assertFalse(StringUtils.isBlank(article.getDocument().getAbstractText()));
//		assertFalse(StringUtils.isBlank(article.getDocument().getTitle()));
//		
//		assertNotNull(article.getHighlightedAbstract());
//		assertNotNull(article.getTitleHighlight());
//		String highlight = article.getHighlightedAbstract().highlight;
//		assertTrue("Abstract highlight has unexpected form: " + highlight, highlight.contains("<span class=\"highlightFull\">Adenomatous polyposis coli</span>"));
//		boolean filteringSuccessful = true;
//		for (Highlight hl : article.getTextContentHighlights()) {
//			if (hl.highlight.equals("<span class=\"highlightFull\">Adenomatous polyposis coli</span> (<span class=\"highlightFull\">Apc</span>) is a large multifunctional protein known to be important for Wnt/beta-catenin signalling, cytoskeletal dynamics, and cell polarity.")) {
//				filteringSuccessful = false;
//			}
//		}
//		assertTrue(filteringSuccessful);
//		// Probably not needed; just to be sure.
//		loadingWorkerReference.interruptAndJoin();
	}
	
	@Test
	public void testArticleSearchWithHighlight3() throws InterruptedException, ExecutionException {
		TextNode textNode = new TextNode("atid117");
		ParseTree parseTree = new ParseTree(textNode, new ParseErrors());
		LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) searchService
				.doArticleSearch("PMC1942070", IIndexInformationService.Indexes.DocumentTypes.pmc, parseTree).get();
		HighlightedSemedicoDocument article = searchResult.semedicoDoc;
		assertNotNull(article);
		assertEquals("PMC1942070", article.getDocument().getDocId());
		assertFalse(StringUtils.isBlank(article.getDocument().getAbstractText()));
		assertFalse(StringUtils.isBlank(article.getDocument().getTitle()));
		
		assertTrue(article.getTextContentHighlights().size() >= 10);
		
		// Probably not needed; just to be sure.
		loadingWorkerReference.interruptAndJoin();
	}
	
	@Test
	public void testArticleSearchWithHighlight4() throws InterruptedException, ExecutionException {
		TextNode mtor = new TextNode("mtor");
		TextNode elegans = new TextNode("elegans");
		BinaryNode or = new BinaryNode(NodeType.OR, mtor, elegans);
		ParseTree parseTree = new ParseTree(or, new ParseErrors());
		LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) searchService
				.doArticleSearch("PMC2817888", IIndexInformationService.Indexes.DocumentTypes.pmc, parseTree).get();
		HighlightedSemedicoDocument article = searchResult.semedicoDoc;
		assertNotNull(article);
		assertEquals("PMC2817888", article.getDocument().getDocId());
		assertFalse(StringUtils.isBlank(article.getDocument().getAbstractText()));
		assertFalse(StringUtils.isBlank(article.getDocument().getTitle()));

		// With this query, the document has no hits in the abstract. Still, the abstract should be set so it can be displayed. 
		assertNotNull(article.getHighlightedAbstract());
		// there really should be no highlighting and thus, no HTML tags within the text (and no 'less' character)
		assertFalse(StringUtils.isBlank(article.getHighlightedAbstract().highlight));
		assertFalse(article.getHighlightedAbstract().highlight.contains("<"));
		
		// Probably not needed; just to be sure.
		loadingWorkerReference.interruptAndJoin();
	}
	
	@Test
	public void testTermSelectSearch() throws Exception {
		ITermService termService = registry.getService(ITermService.class);
		// Term "Granulocytes"
		IConcept term = termService.getTerm(NodeIDPrefixConstants.TERM + 15);
//		IFacetTermFactory termFactory = registry.getService(IFacetTermFactory.class);
//		IConcept keywordTerm = termFactory.createKeywordTerm("inhibitor", "inhibitor");

		// keyword "inhibitor"
		TextNode tn1 = new TextNode("inhibitor");
//		tn1.setTerms(Lists.<IConcept> newArrayList(keywordTerm));
		TextNode tn2 = new TextNode("Granulocytes");
		tn2.setTerms(Lists.<IConcept> newArrayList(term));
		BinaryNode andNode = new BinaryNode(NodeType.AND, tn1, tn2);
		ParseTree semedicoQuery = new ParseTree(andNode, new ParseErrors());

		// String userQuery = "i search something";
		// Multimap<String, IFacetTerm> semedicoQuery = HashMultimap.create();
		// semedicoQuery.put(userQuery, term);
		SearchState searchState = new SearchState();
		UserInterfaceState uiState = getUiState();

		SemedicoSearchResult searchResult = searchService.doTermSelectSearch(semedicoQuery, searchState, uiState).get();
		assertNotNull(searchResult);

		loadingWorkerReference.interruptAndJoin();
	}
	
	
	@Ignore
	@Deprecated
	@Test
	public void testGetFacetIndexTerms() throws InterruptedException, ExecutionException {
		IFacetService facetService = registry.getService(IFacetService.class);

		Facet facet = facetService.getFacetById(NodeIDPrefixConstants.FACET + 8);

		LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) searchService.doRetrieveFacetIndexTerms(Lists.newArrayList(facet)).get();

		List<String> facetIndexTerms = searchResult.facetIndexTerms;
		// Just a few terms that should be in this field (looked up by a JSON
		// query to ElasticSearch by hand (using the
		// 'kopf' site plugin)).
		System.out.println(facetIndexTerms);
		assertTrue(facetIndexTerms.contains(NodeIDPrefixConstants.TERM + 160));
		assertTrue(facetIndexTerms.contains(NodeIDPrefixConstants.TERM + 162));
		assertTrue(facetIndexTerms.contains(NodeIDPrefixConstants.TERM + 192));
		assertEquals(new Integer(9), new Integer(facetIndexTerms.size()));
	}
	@Ignore
	@Deprecated
	@SuppressWarnings("unchecked")
	@Test
	public void testGetTermDocumentFrequencies() {
		ISearchComponent docFreqChain = registry.getService(ISearchComponent.class, TermDocumentFrequencyChain.class);
		SemedicoSearchCarrier searchCarrier = new SemedicoSearchCarrier("DocFreqChain");
		docFreqChain.process(searchCarrier);
		LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) searchCarrier.result;
		assertNotNull(searchResult);
		TripleStream<String, Long, Long> termDocumentFrequencies = searchResult.termDocumentFrequencies;
		assertNotNull(termDocumentFrequencies);
		while (termDocumentFrequencies.incrementTuple()) {
			String termId = termDocumentFrequencies.getLeft();
			long count = termDocumentFrequencies.getMiddle();
			if (termId.equals(NodeIDPrefixConstants.TERM + 1063))
				assertEquals(1, count);
			if (termId.equals(NodeIDPrefixConstants.TERM + 103))
				assertEquals(170, count);
			if (termId.equals(NodeIDPrefixConstants.AGGREGATE_TERM + 0))
				assertEquals(589, count);
		}
	}
	
	private UserInterfaceState getUiState() {
		Logger uiLog = LoggerFactory.getLogger(UserInterfaceState.class);
		IFacetService facetService = registry.getService(IFacetService.class);

		Facet facet = facetService.getFacetById(NodeIDPrefixConstants.FACET + 0);
		UIFacet uiFacet = facet.getUiFacetCopy(uiLog);
		Facet facet2 = facetService.getFacetById(NodeIDPrefixConstants.FACET + 3);
		UIFacet uiFacet2 = facet2.getUiFacetCopy(uiLog);
		Facet facet3 = facetService.getFacetById(NodeIDPrefixConstants.FACET + 2);
		UIFacet uiFacet3 = facet3.getUiFacetCopy(uiLog);
		Facet facet4 = facetService.getFacetById(NodeIDPrefixConstants.FACET + 4);
		UIFacet uiFacet4 = facet4.getUiFacetCopy(uiLog);
		Map<Facet, UIFacet> uiFacets = new HashMap<>();
		uiFacets.put(facet, uiFacet);
		uiFacets.put(facet2, uiFacet2);
		uiFacets.put(facet3, uiFacet3);
		uiFacets.put(facet4, uiFacet4);

		UIFacetGroup facetGroup = new UIFacetGroup("testfg", 0);
		facetGroup.add(uiFacet);
		facetGroup.add(uiFacet2);
		facetGroup.add(uiFacet3);
		facetGroup.add(uiFacet4);
		List<UIFacetGroup> uiFacetGroups = new ArrayList<>();
		uiFacetGroups.add(facetGroup);

		LabelStore labelStore = new LabelStore(registry.getService(ILabelCacheService.class));
		UserInterfaceState uiState = new UserInterfaceState(uiLog, uiFacets, uiFacetGroups, labelStore);
		return uiState;
	}

	@AfterClass
	public static void shutdown() {
		if (null != registry)
			registry.shutdown();
	}
}
