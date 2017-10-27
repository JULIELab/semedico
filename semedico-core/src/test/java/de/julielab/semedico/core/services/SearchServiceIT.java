package de.julielab.semedico.core.services;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.facets.UIFacetGroup;
import de.julielab.semedico.core.parsing.BinaryNode;
import de.julielab.semedico.core.parsing.Node.NodeType;
import de.julielab.semedico.core.parsing.ParseErrors;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;
import de.julielab.semedico.core.search.components.data.LabelStore;
import de.julielab.semedico.core.search.interfaces.ILabelCacheService;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.search.results.ArticleSearchResult;
import de.julielab.semedico.core.search.results.DocumentSearchResult;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;
import de.julielab.semedico.core.services.TermNeo4jService.TermCacheLoader;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.core.services.interfaces.ITermService;

public class SearchServiceIT {
	private static final Logger log = LoggerFactory.getLogger(SearchService.class);

	private static Registry registry;
	private static ISearchService searchService;
	private static AsyncCacheLoader<String, IConcept>.LoadingWorkerReference loadingWorkerReference;

	@BeforeClass
	public static void setup() {
		registry = TestUtils.createTestRegistry();
		searchService = registry.getService(ISearchService.class);
		loadingWorkerReference = registry.getService(TermCacheLoader.class).getLoadingWorkerReference();
	}
	
	@AfterClass
	public static void shutdown() {
		if (null != registry)
			registry.shutdown();
	}

	@Test
	public void testArticleSearchWithoutHighlight() throws InterruptedException, ExecutionException {
		ArticleSearchResult searchResult =  searchService
				.doArticleSearch("18214854", IIndexInformationService.Indexes.DocumentTypes.medline, null).get();
		HighlightedSemedicoDocument article = searchResult.article;
		assertNotNull(article);
		assertEquals("18214854", article.getDocument().getDocId());
		assertFalse(StringUtils.isBlank(article.getDocument().getAbstractText()));
		assertFalse(StringUtils.isBlank(article.getDocument().getTitle()));

		assertNotNull(article.getHighlightedAbstract());
		assertNotNull(article.getTitleHighlight());

		// Probably not needed; just to be sure.
		loadingWorkerReference.interruptAndJoin();
	}
	
	@Test
	public void testArticleSearchWithHighlight() throws InterruptedException, ExecutionException {
		TextNode textNode = new TextNode("HMGA2");
		ParseTree parseTree = new ParseTree(textNode, new ParseErrors());
		ArticleSearchResult searchResult =  searchService
				.doArticleSearch("18214854", IIndexInformationService.Indexes.DocumentTypes.medline, ()->parseTree).get();
		HighlightedSemedicoDocument article = searchResult.article;
		assertNotNull(article);
		assertEquals("18214854", article.getDocument().getDocId());
		assertFalse(StringUtils.isBlank(article.getDocument().getAbstractText()));
		assertFalse(StringUtils.isBlank(article.getDocument().getTitle()));
		
		assertNotNull(article.getHighlightedAbstract());
		assertNotNull(article.getTitleHighlight());
		// TODO fix highlighting
//		assertTrue(article.getTitleHighlight().highlight.contains("<span class=\"highlightFull\">HMGA2</span>"));

		// Probably not needed; just to be sure.
		loadingWorkerReference.interruptAndJoin();
	}
	
	@Ignore
	@Test
	public void testTermSelectSearch() throws Exception {
		ITermService termService = registry.getService(ITermService.class);
		// Term "Granulocytes"
		IConcept term = termService.getTerm(NodeIDPrefixConstants.TERM + 15);

		// keyword "inhibitor"
		TextNode tn1 = new TextNode("inhibitor");
		TextNode tn2 = new TextNode("Granulocytes");
		tn2.setQueryToken(new QueryToken(0, 0, "Granulocytes"));
		tn2.setTerms(Lists.<IConcept> newArrayList(term));
		BinaryNode andNode = new BinaryNode(NodeType.AND, tn1, tn2);
		ParseTree semedicoQuery = new ParseTree(andNode, new ParseErrors());

		SearchState searchState = new SearchState();
		UserInterfaceState uiState = getUiState();

		SemedicoSearchResult searchResult = searchService.doTermSelectSearch(()->semedicoQuery, searchState, uiState).get();
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
	
	@Test
	public void testDocumentSearch() throws Exception {
		TextNode textNode = new TextNode("st14");
		textNode.setQueryToken(new QueryToken(0, 0, "st14"));
		ParseTree parseTree = new ParseTree(textNode, new ParseErrors());
		DocumentSearchResult result = searchService.doDocumentSearch(()->parseTree, Collections.emptyList(), new SearchState(), getUiState()).get();
		assertTrue(result.totalNumDocs > 0);
		assertEquals("19546220", result.documentHits.getDisplayedObjects().iterator().next().getDocument().getDocId());
	}
	
	@Test
	public void testDoRetrieveFieldTermsByDocScore() {
		TextNode textNode = new TextNode("mtor");
		textNode.setQueryToken(new QueryToken(0, 0, "mtor"));
		ParseTree parseTree = new ParseTree(textNode, new ParseErrors());
		Future<SemedicoSearchResult> result = searchService.doRetrieveFieldTermsByDocScore(() -> parseTree, IIndexInformationService.PUBMED_ID, 1000);
		
	}

}
