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
import de.julielab.semedico.core.search.components.data.ArticleSearchResult;
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

public class SearchServiceIT {
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
	


	@Test
	public void testArticleSearchWithHighlight() throws InterruptedException, ExecutionException {
		TextNode textNode = new TextNode("cyclase");
		ParseTree parseTree = new ParseTree(textNode, new ParseErrors());
		ArticleSearchResult searchResult =  searchService
				.doArticleSearch("10089566", IIndexInformationService.Indexes.DocumentTypes.medline, parseTree).get();
		HighlightedSemedicoDocument article = searchResult.article;
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
	public void testArticleSearchWithHighlight3() throws InterruptedException, ExecutionException {
		TextNode textNode = new TextNode("atid117");
		ParseTree parseTree = new ParseTree(textNode, new ParseErrors());
		ArticleSearchResult searchResult = searchService
				.doArticleSearch("PMC1942070", IIndexInformationService.Indexes.DocumentTypes.pmc, parseTree).get();
		HighlightedSemedicoDocument article = searchResult.article;
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
		ArticleSearchResult searchResult =  searchService
				.doArticleSearch("PMC2817888", IIndexInformationService.Indexes.DocumentTypes.pmc, parseTree).get();
		HighlightedSemedicoDocument article = searchResult.article;
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
