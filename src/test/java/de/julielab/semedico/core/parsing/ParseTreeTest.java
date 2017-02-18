package de.julielab.semedico.core.parsing;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.Registry;
import org.easymock.EasyMock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.aliasi.chunk.Chunker;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetLabels;
import de.julielab.semedico.core.facetterms.FacetTerm;
import de.julielab.semedico.core.parsing.Node.NodeType;
import de.julielab.semedico.core.parsing.ParseTree.SERIALIZATION;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.ILexerService;
import de.julielab.semedico.core.services.interfaces.IParsingService;
import de.julielab.semedico.core.services.interfaces.IQueryAnalysisService;
import de.julielab.semedico.core.services.interfaces.ITermRecognitionService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.query.QueryAnalysisServiceTest;
import de.julielab.semedico.core.services.query.TermRecognitionService;

/**
 * Some simple tests for the ParseTree and Parser. The tests here work with a
 * small, manually configured set of test data. For more realistic but more
 * flexible tests, see {@link QueryAnalysisServiceTest}.
 * 
 * @author hellrich
 * 
 */
public class ParseTreeTest {

	private static Registry registry;
	private static ILexerService lexerService;
	private static IParsingService parsingService;
	private static ITermRecognitionService termRecognitionService;
	@Deprecated
	private static TermRecognitionService eventRecognitionService;

	@BeforeClass
	public static void setup() {
		registry = TestUtils.createTestRegistry();
		lexerService = registry.getService(ILexerService.class);
		parsingService = registry.getService(IParsingService.class);

		// set the default operator for this test, does not depend on actual
		// productivity mode
		try {
			Field field = ParseTree.class.getDeclaredField("defaultOperator");
			field.setAccessible(true);
			field.set(null, ParseTree.AND);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Ignore
	public void testEventParse() throws Exception {
		ParseTree parseTree;
		// We currently do not handle relation expressions
		parseTree = parseAndRecognizeTerms("y Binding x");
		assertEquals("(y Binding x)", parseTree.toString(SERIALIZATION.TEXT));
		List<Node> conceptNodes = parseTree.getConceptNodes();
		assertEquals(1, conceptNodes.size());
		assertEquals(EventNode.class, conceptNodes.get(0).getClass());
		List<Node> eventNodes = parseTree.getEventNodes();
		assertEquals(1, eventNodes.size());
		assertEquals(EventNode.class, eventNodes.get(0).getClass());

		parseTree = parseAndRecognizeTerms("-y Binding x");
		assertEquals("((NOT y) Binding x)", parseTree.toString(SERIALIZATION.TEXT));

		parseTree = parseAndRecognizeTerms("IL2_HUMAN Binding Y");
		assertEquals("(IL2_HUMAN Binding Y)", parseTree.toString(SERIALIZATION.TEXT));

		parseTree = parseAndRecognizeTerms("x Binding any");
		assertEquals("(x-id binding-id ani)", parseTree.toString(SERIALIZATION.TERMS));

		parseTree = parseAndRecognizeTerms("foo bar Binding y");
		assertEquals("(foo bar Binding y)", parseTree.toString(SERIALIZATION.TEXT));

		// we currently have to put parenthesis around the event, otherwise (y
		// and y) would be interpreted as a complex
		// argument (which interestingly would make no sense here, should this
		// perhaps be used as a hint to take another
		// interpretation?)
		parseTree = parseAndRecognizeTerms("y and (y Binding x)");
		assertEquals("(y AND (y Binding x))", parseTree.toString(SERIALIZATION.TEXT));
		conceptNodes = parseTree.getConceptNodes();
		assertEquals(2, conceptNodes.size());
		assertEquals(TextNode.class, conceptNodes.get(0).getClass());
		assertEquals(EventNode.class, conceptNodes.get(1).getClass());
		eventNodes = parseTree.getEventNodes();
		assertEquals(1, eventNodes.size());
		assertEquals(EventNode.class, eventNodes.get(0).getClass());

		// Don't know what this test should be good for other than what is
		// covered by the tests above.
		// parseTree = parseAndRecognizeTerms("IL2_HUMAN Binding Y_X");
		// assertEquals("(IL2_HUMAN-Binding-Y_X)",
		// parseTree.toString(SERIALIZATION.TEXT));
	}

	@Test
	public void testParse() throws Exception {
		ParseTree.getDefaultOperator();
		ParseTree parseTree = parse("\"u\" OR (x y)");
		assertEquals("(u OR (x AND y))", parseTree.toString(SERIALIZATION.TEXT));

		// the not operator "-" must be connected directly to the negated
		// expression, no whitespaces (because there actually could be
		// in-word-dashes in biomed terms)
		parseTree = parse("- (x y)");
		assertEquals("(x AND y)", parseTree.toString(SERIALIZATION.TEXT));

		parseTree = parse("-(x y)");
		assertEquals("(NOT (x AND y))", parseTree.toString(SERIALIZATION.TEXT));

		parseTree = parse("-y");
		assertEquals("(NOT y)", parseTree.toString(SERIALIZATION.TEXT));

		parseTree = parse("-y AND x");
		assertEquals("((NOT y) AND x)", parseTree.toString(SERIALIZATION.TEXT));

		parseTree = parse("\"foo\" OR NOT bar");
		assertEquals("(foo OR (NOT bar))", parseTree.toString(SERIALIZATION.TEXT));

		parseTree = parse("x y z AND (\"foo\" OR NOT bar)");
		assertEquals("(((x AND y) AND z) AND (foo OR (NOT bar)))", parseTree.toString(SERIALIZATION.TEXT));

		// Left parentheses errors are repaired.
		parseTree = parse("((x Or (y or z)");
		assertEquals("(x OR (y OR z))", parseTree.toString(SERIALIZATION.TEXT));

		// New children are added to (grand)children as necessary.
		// Please note a minor quirk here: In the last clause there is a
		// whitespace in front of '!'. If it wouldn't be there, we would not
		// recognize the NOT. But if we do not require the whitespace, there
		// would be NOT in other cases. It should be allowed in a valid boolean
		// expression but for now we don't build the required validation
		// facility.
		parseTree = parse("(x y) OR -(x !( !u v))");
		assertEquals("((x AND y) OR (NOT (x AND (NOT ((NOT u) AND v)))))", parseTree.toString(SERIALIZATION.TEXT));
	}

	@Test
	public void testParse2() throws Exception {
		// Lexer has special rules for tokens without whitespaces.
		ParseTree parseTree = parse("NOT(x AND y)OR c");
		assertEquals("((NOT (x AND y)) OR c)", parseTree.toString(SERIALIZATION.TEXT));
	}

	@Test
	public void testAddAndRemove() throws Exception {
		// There was an error where the parent node of a replaced node was not
		// set to null and the parent of the
		// replacement node was not set to the former parent of the replaced
		// node. With this error in place, the
		// following sequence of operations led to an exception. If it doesn't,
		// this test is successful.
		ParseTree parseTree = parse("x");
		parseTree.add(parseTree.getRoot(), new TextNode("y"), NodeType.AND);
		parseTree.add(parseTree.getRoot(), new TextNode("z"), NodeType.AND);
		parseTree.remove(0);
		parseTree.remove(2);
	}

	@Test
	public void testAddAndRemoveCompressed() throws Exception {
		ParseTree parseTree = parse("x");
		// get a compressed parse tree. This does not only mean that existing
		// structure are compressed but that all
		// operations - especially the add operation - will work with compressed
		// operator nodes and not binary nodes any
		// more.
		parseTree = parseTree.compress();
		parseTree.add(parseTree.getRoot(), new TextNode("y"), NodeType.AND);
		parseTree.add(parseTree.getRoot(), new TextNode("z"), NodeType.AND);
		parseTree.add(parseTree.getRoot(), new TextNode("q"), NodeType.AND);
		assertEquals(4, ((BranchNode) parseTree.getRoot()).getChildren().size());
		assertEquals("(x AND y AND z AND q)", parseTree.toString());
		parseTree.remove(0);
		parseTree.remove(2);
	}

	@Test
	public void testTerms() throws Exception {
		termRecognitionService = new TermRecognitionService(prepareTermMockChunker(), prepareMockTermService());
		eventRecognitionService = new TermRecognitionService(prepareTermMockChunker(), prepareMockTermService());

		ParseTree parseTree;

		// Tokens are combined and matched to dictionary entries. Original text
		// values are used.
		parseTree = parseAndRecognizeTerms("foo bar AND y");
		System.out.println(parseTree.toString(SERIALIZATION.TERMS));
		assertTrue(Pattern.matches("\\(\\(dicCategoryI+ OR dicCategoryI+\\) AND y-id\\)",
				parseTree.toString(SERIALIZATION.TERMS)));

		termRecognitionService = new TermRecognitionService(prepareTermMockChunker(), prepareMockTermService());
		eventRecognitionService = new TermRecognitionService(prepareTermMockChunker(), prepareMockTermService());
		// Tokens are combined and matched to dictionary, but original text
		// values are used instead of terms.
		parseTree = parseAndRecognizeTerms("foo bar AND y");
		assertEquals("(foo bar AND y)", parseTree.toString(SERIALIZATION.TEXT));
	}

	@Test
	public void testManipulation() throws Exception {

		ParseTree parseTree = parse("\"u\" OR (x y)");
		assertEquals("(u OR (x AND y))", parseTree.toString(SERIALIZATION.TEXT));

		// Replace texts.
		parseTree.expandTerm("x", "v");
		assertEquals("(u OR (v AND y))", parseTree.toString(SERIALIZATION.TEXT));

		parseTree.expandTerm("v", "(Tom AND Jerry)");
		assertEquals("(u OR ((Tom AND Jerry) AND y))", parseTree.toString(SERIALIZATION.TEXT));
		// Nodes are removed.
		parseTree.remove("u");
		assertEquals("((Tom AND Jerry) AND y)", parseTree.toString(SERIALIZATION.TEXT));

		parseTree = parse("tom and jerry");
		parseTree.remove("tom");
		assertEquals("jerry", parseTree.toString(SERIALIZATION.TEXT));
		assertEquals(parseTree.getNode("jerry"), parseTree.getRoot());
		assertNull(parseTree.getRoot().getParent());
		parseTree.remove("jerry");
		assertNull(parseTree.getRoot());

		// Nodes are added as siblings to specified nodes by inserting new
		// parent nodes.
		parseTree = parse("u OR (x y)");
		Node existingNode = parseTree.getNode("u");
		TextNode newNode = new TextNode("added");
		parseTree.add(existingNode, newNode, NodeType.OR);
		assertEquals("((u OR added) OR (x AND y))", parseTree.toString(SERIALIZATION.TEXT));
		existingNode = parseTree.getRoot();
		parseTree.add(existingNode, newNode, NodeType.AND);
		assertEquals("(((u OR added) OR (x AND y)) AND added)", parseTree.toString(SERIALIZATION.TEXT));
	}

	@Test
	public void testManipulationWithIds() throws Exception {

		ParseTree parseTree = parse("\"u\" OR (x y)");
		assertEquals("(1 0{OR} (3 2{AND} 4))", parseTree.toString(SERIALIZATION.IDS));
		Map<Long, Node> idMap = getIdMap(parseTree);
		assertEquals(5, idMap.size());
		Map<String, Node> textMap = getTextMap(parseTree);
		assertEquals(3, textMap.size());

		// Replace texts.
		parseTree.expandTerm("x", "v");
		assertEquals("(1 0{OR} (3 2{AND} 4))", parseTree.toString(SERIALIZATION.IDS));
		idMap = getIdMap(parseTree);
		assertEquals(5, idMap.size());
		textMap = getTextMap(parseTree);
		assertEquals(3, textMap.size());

		parseTree.expandTerm("v", new String[] { "Tom", "Jerry" });
		assertEquals("(1 0{OR} ((6 5{AND} 7) 2{AND} 4))", parseTree.toString(SERIALIZATION.IDS));
		idMap = getIdMap(parseTree);
		assertEquals(7, idMap.size());
		textMap = getTextMap(parseTree);
		assertEquals(4, textMap.size());

		// Nodes are removed.
		parseTree.remove("u");
		assertEquals("((6 5{AND} 7) 2{AND} 4)", parseTree.toString(SERIALIZATION.IDS));
		idMap = getIdMap(parseTree);
		assertEquals(5, idMap.size());
		textMap = getTextMap(parseTree);
		assertEquals(3, textMap.size());
		ArrayList<Long> ids = new ArrayList<Long>();
		for (Long id : idMap.keySet()) {
			ids.add(id);
		}
		assertEquals("2 4 5 6 7", StringUtils.join(ids, " "));
		ArrayList<String> texts = new ArrayList<String>();
		for (String text : textMap.keySet()) {
			texts.add(text);
		}
		assertEquals("Jerry Tom y", StringUtils.join(texts, " "));

		// Further testing of node removal.
		parseTree = parse("tom and jerry");
		parseTree.remove("tom");
		// only the node with index 2 (jerry) remains
		assertEquals("2", parseTree.toString(SERIALIZATION.IDS));
		assertEquals(parseTree.getNode("jerry"), parseTree.getRoot());
		assertNull(parseTree.getRoot().getParent());
		idMap = getIdMap(parseTree);
		assertEquals(1, idMap.size());
		textMap = getTextMap(parseTree);
		assertEquals(1, textMap.size());
		parseTree.remove("jerry");
		assertNull(parseTree.getRoot());
		idMap = getIdMap(parseTree);
		assertEquals(0, idMap.size());
		textMap = getTextMap(parseTree);
		assertEquals(0, textMap.size());

		parseTree = parse("x AND (NOT tom)");
		parseTree.remove("tom");
		assertEquals("1", parseTree.toString(SERIALIZATION.IDS));
		idMap = getIdMap(parseTree);
		assertEquals(1, idMap.size());
		textMap = getTextMap(parseTree);
		assertEquals(1, textMap.size());

		// Nodes are added as siblings to specified nodes by inserting new
		// parent nodes.
		parseTree = parse("\"u\" OR (x y)");
		Node existingNode = parseTree.getNode("u");
		TextNode newNode = new TextNode("added");
		parseTree.add(existingNode, newNode, NodeType.OR);
		assertEquals("((1 5{OR} 6) 0{OR} (3 2{AND} 4))", parseTree.toString(SERIALIZATION.IDS));
		idMap = getIdMap(parseTree);
		assertEquals(7, idMap.size());
		textMap = getTextMap(parseTree);
		assertEquals(4, textMap.size());
		existingNode = parseTree.getRoot();
		newNode = new TextNode("alsoAdded");
		parseTree.add(existingNode, newNode, NodeType.AND);
		assertEquals("(((1 5{OR} 6) 0{OR} (3 2{AND} 4)) 7{AND} 8)", parseTree.toString(SERIALIZATION.IDS));
		idMap = getIdMap(parseTree);
		assertEquals(9, idMap.size());
		textMap = getTextMap(parseTree);
		assertEquals(5, textMap.size());
	}

	@Test
	public void testOperations() throws Exception {
		TextNode n1 = new TextNode("left");
		TextNode n2 = new TextNode("right");

		ParseTree parseTree = new ParseTree(n1, new ParseErrors());
		assertEquals("left", parseTree.toString(SERIALIZATION.TEXT));

		parseTree.add(parseTree.getRoot(), n2, NodeType.AND);
		assertEquals("(left AND right)", parseTree.toString(SERIALIZATION.TEXT));

		parseTree.remove(n1.getId());
		assertEquals("right", parseTree.toString(SERIALIZATION.TEXT));

	}

	/**
	 * Runs the <tt>LexerService</tt>, the <tt>TermRecognitionService</tt> and
	 * the <tt>ParsingService</tt>.
	 * 
	 * @param toParse
	 * @return
	 * @throws Exception
	 */
	private ParseTree parseAndRecognizeTerms(String toParse) throws Exception {
		termRecognitionService = new TermRecognitionService(prepareTermMockChunker(), prepareMockTermService());
		IFacetService facetService = EasyMock.createMock(IFacetService.class);
		expect(facetService.getKeywordFacet()).andReturn(Facet.KEYWORD_FACET);
		replay(facetService);

		List<QueryToken> lex = lexerService.lex(toParse);
		lex = termRecognitionService.recognizeTerms(lex, 0);
		ParseTree parse = parsingService.parse(lex);
		return parse;
	}

	/**
	 * Runs the <tt>LexerService</tt> and the <tt>ParsingService</tt> but NOT
	 * the <tt>TermRecognitionService</tt>, thus to search for terms is done.
	 * 
	 * @param toParse
	 * @return
	 * @throws Exception
	 */
	private ParseTree parse(String toParse) throws Exception {
		List<QueryToken> lex = lexerService.lex(toParse);
		ParseTree parse = parsingService.parse(lex);
		return parse;
	}

	public static Chunker prepareTermMockChunker() {
		MapDictionary<String> dic = new MapDictionary<String>();
		dic.addEntry(new DictionaryEntry<String>("binding", "binding-id"));
		dic.addEntry(new DictionaryEntry<String>("foo bar", "dicCategoryI"));
		dic.addEntry(new DictionaryEntry<String>("foo bar", "dicCategoryII"));
		dic.addEntry(new DictionaryEntry<String>("x", "x-id"));
		dic.addEntry(new DictionaryEntry<String>("y", "y-id"));
		ExactDictionaryChunker chunker = new ExactDictionaryChunker(dic, IndoEuropeanTokenizerFactory.INSTANCE, true,
				false);
		return chunker;
	}

	public static ITermService prepareMockTermService() {
		FacetTerm bindingTerm = new FacetTerm("binding-id", "Binding");
		bindingTerm.setEventValence(Sets.newHashSet(2));
		bindingTerm.setIsEventTrigger(true);

		Facet eventFacet = new Facet("event-facet-id", "Event Facet",
				Lists.newArrayList(IIndexInformationService.ABSTRACT, IIndexInformationService.TITLE),
				Collections.<String> emptyList(), Collections.<FacetLabels.General> emptySet(),
				Collections.<FacetLabels.Unique> emptySet(), 1, "cssIdEvents", null, null);
		FacetTerm termI = new FacetTerm("dicCategoryI", "facetI");
		FacetTerm termII = new FacetTerm("dicCategoryII", "facetII");
		FacetTerm xTerm = new FacetTerm("x-id", "X");
		FacetTerm yTerm = new FacetTerm("y-id", "Y");
		Facet xyFacet = new Facet("xy-facet-id", "XY Facet",
				Lists.newArrayList(IIndexInformationService.ABSTRACT, IIndexInformationService.TITLE),
				Collections.<String> emptyList(), Collections.<FacetLabels.General> emptySet(),
				Collections.<FacetLabels.Unique> emptySet(), 1, "cssIdEvents", null, null);
		bindingTerm.addFacet(eventFacet);
		xTerm.addFacet(xyFacet);
		yTerm.addFacet(xyFacet);

		Facet facetI = EasyMock.createMock(Facet.class);
		expect(facetI.hasUniqueLabel(FacetLabels.Unique.NO_FACET)).andReturn(false);
		expect(facetI.hasUniqueLabel(FacetLabels.Unique.NO_FACET)).andReturn(false);
		expect(facetI.hasUniqueLabel(FacetLabels.Unique.NO_FACET)).andReturn(false);
		expect(facetI.hasUniqueLabel(FacetLabels.Unique.NO_FACET)).andReturn(false);
		expect(facetI.getId()).andReturn("fidI");
		expect(facetI.getId()).andReturn("fidI");
		expect(facetI.getId()).andReturn("fidI");
		expect(facetI.getName()).andReturn("fnameI");
		expect(facetI.getName()).andReturn("fnameI");
		expect(facetI.getName()).andReturn("fnameI");
		replay(facetI);
		Facet facetII = EasyMock.createMock(Facet.class);
		expect(facetII.hasUniqueLabel(FacetLabels.Unique.NO_FACET)).andReturn(false);
		expect(facetII.hasUniqueLabel(FacetLabels.Unique.NO_FACET)).andReturn(false);
		expect(facetII.hasUniqueLabel(FacetLabels.Unique.NO_FACET)).andReturn(false);
		expect(facetII.hasUniqueLabel(FacetLabels.Unique.NO_FACET)).andReturn(false);
		expect(facetII.getId()).andReturn("fidII");
		expect(facetII.getId()).andReturn("fidII");
		expect(facetII.getId()).andReturn("fidII");
		expect(facetII.getName()).andReturn("fnameII");
		expect(facetII.getName()).andReturn("fnameII");
		expect(facetII.getName()).andReturn("fnameII");
		replay(facetII);
		termI.addFacet(facetI);
		termII.addFacet(facetII);
		ITermService mock = EasyMock.createNiceMock(ITermService.class);
		expect(mock.isStringTermID("dicCategoryI")).andReturn(false);
		expect(mock.isStringTermID("dicCategoryI")).andReturn(false);
		expect(mock.isStringTermID("dicCategoryII")).andReturn(false);
		expect(mock.isStringTermID("dicCategoryII")).andReturn(false);
		expect(mock.isStringTermID("x-id")).andReturn(false);
		expect(mock.isStringTermID("y-id")).andReturn(false);
		expect(mock.isStringTermID("binding-id")).andReturn(false);
		expect(mock.isStringTermID("binding-id")).andReturn(false);
		expect(mock.mapQueryStringTerms(Collections.<QueryToken> emptyList(), 0))
				.andReturn(Collections.<QueryToken> emptyList());
		expect(mock.mapQueryStringTerms(Collections.<QueryToken> emptyList(), 0))
				.andReturn(Collections.<QueryToken> emptyList());
		expect(mock.mapQueryStringTerms(Collections.<QueryToken> emptyList(), 0))
				.andReturn(Collections.<QueryToken> emptyList());
		expect(mock.mapQueryStringTerms(Collections.<QueryToken> emptyList(), 0))
				.andReturn(Collections.<QueryToken> emptyList());
		expect(mock.mapQueryStringTerms(Collections.<QueryToken> emptyList(), 0))
				.andReturn(Collections.<QueryToken> emptyList());
		expect(mock.getTermSynchronously("dicCategoryI")).andReturn(termI);
		expect(mock.getTermSynchronously("dicCategoryI")).andReturn(termI);
		expect(mock.getTermSynchronously("dicCategoryII")).andReturn(termII);
		expect(mock.getTermSynchronously("dicCategoryII")).andReturn(termII);
		expect(mock.getTermSynchronously("x-id")).andReturn(xTerm);
		expect(mock.getTermSynchronously("y-id")).andReturn(yTerm);
		expect(mock.getTermSynchronously("y-id")).andReturn(yTerm);
		expect(mock.getTermSynchronously("binding-id")).andReturn(bindingTerm);
		expect(mock.getTermSynchronously("binding-id")).andReturn(bindingTerm);
		// expect(mock.createKeywordTerm("il2_human",
		// "IL2_HUMAN")).andReturn(new KeywordTerm("il2_human", "IL2_HUMAN"));
		// expect(mock.createKeywordTerm("ani", "any")).andReturn(new
		// KeywordTerm("ani", "any"));
		replay(mock);
		return mock;
	}

	@Test
	public void testInOrderTraversal() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "one two";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		List<Node> traversal = parseTree.traverseInOrder();
		assertEquals(3, traversal.size());
		assertEquals(TextNode.class, traversal.get(0).getClass());
		assertEquals(BinaryNode.class, traversal.get(1).getClass());
		assertEquals(TextNode.class, traversal.get(2).getClass());
	}

	@Test
	public void testInOrderTraversal2() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "one two three";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		List<Node> traversal = parseTree.traverseInOrder();
		assertEquals(5, traversal.size());
		assertEquals(TextNode.class, traversal.get(0).getClass());
		assertEquals(BinaryNode.class, traversal.get(1).getClass());
		assertEquals(TextNode.class, traversal.get(2).getClass());
		assertEquals(BinaryNode.class, traversal.get(3).getClass());
		assertEquals(TextNode.class, traversal.get(4).getClass());
	}

	@Test
	public void testPreOrderTraversal() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "one and two";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		List<Node> traversal = parseTree.traversePreOrder();
		assertEquals(3, traversal.size());
		assertEquals(BinaryNode.class, traversal.get(0).getClass());
		assertEquals(TextNode.class, traversal.get(1).getClass());
		assertEquals(TextNode.class, traversal.get(2).getClass());
	}

	@Test
	public void testPreOrderTraversal1() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "one and two and (three regulates four)";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		List<Node> traversal = parseTree.traversePreOrder();
		assertEquals(9, traversal.size());
		// middle 'and'
		assertEquals(BinaryNode.class, traversal.get(0).getClass());
		// one AND two
		assertEquals(BinaryNode.class, traversal.get(1).getClass());
		assertEquals(TextNode.class, traversal.get(2).getClass());
		assertEquals("one", traversal.get(2).getText());
		assertEquals(TextNode.class, traversal.get(3).getClass());
		assertEquals("two", traversal.get(3).getText());
		assertEquals(BinaryNode.class, traversal.get(4).getClass());
		assertEquals(BinaryNode.class, traversal.get(5).getClass());
		assertEquals(TextNode.class, traversal.get(6).getClass());
		assertEquals("three", traversal.get(6).getText());
		assertEquals(TextNode.class, traversal.get(7).getClass());
		assertEquals("regulates", traversal.get(7).getText());
		assertEquals(TextNode.class, traversal.get(8).getClass());
		assertEquals("four", traversal.get(8).getText());
	}

	@Ignore
	@Test
	public void testPreOrderTraversal2() {
		// same as above, but do not split events into their parts
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "one and two and (three regulates four)";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		List<Node> traversal = parseTree.traversePreOrder(false);
		assertEquals(5, traversal.size());
		// middle 'and'
		assertEquals(BinaryNode.class, traversal.get(0).getClass());
		// one AND two
		assertEquals(BinaryNode.class, traversal.get(1).getClass());
		assertEquals(TextNode.class, traversal.get(2).getClass());
		assertEquals("one", traversal.get(2).getText());
		assertEquals(TextNode.class, traversal.get(3).getClass());
		assertEquals("two", traversal.get(3).getText());
		assertEquals(EventNode.class, traversal.get(4).getClass());
	}

	@Test
	public void testCompressParseTree() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "one and two and three and four and five";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		ParseTree compressedTree = parseTree.compress();
		assertEquals(CompressedBooleanNode.class, compressedTree.getRoot().getClass());
		CompressedBooleanNode root = (CompressedBooleanNode) compressedTree.getRoot();
		assertEquals(5, root.getChildren().size());
	}

	@Test
	public void testCompressParseTree2() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "one and two and three or four and five";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		ParseTree compressedTree = parseTree.compress();
		assertEquals(CompressedBooleanNode.class, compressedTree.getRoot().getClass());
		CompressedBooleanNode root = (CompressedBooleanNode) compressedTree.getRoot();
		assertEquals(2, root.getChildren().size());
		assertEquals(NodeType.OR, root.getNodeType());

		Node firstChild = root.getFirstChild();
		assertEquals(CompressedBooleanNode.class, firstChild.getClass());
		assertEquals(NodeType.AND, firstChild.getNodeType());
		assertEquals(3, ((BranchNode) firstChild).getChildren().size());

		Node secondChild = root.getLastChild();
		assertEquals(CompressedBooleanNode.class, secondChild.getClass());
		assertEquals(NodeType.AND, secondChild.getNodeType());
		assertEquals(2, ((BranchNode) secondChild).getChildren().size());
	}

	@Test
	public void testCompressParseTree3() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "one or two or three and four or five";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		ParseTree compressedTree = parseTree.compress();
		assertEquals(CompressedBooleanNode.class, compressedTree.getRoot().getClass());
		CompressedBooleanNode root = (CompressedBooleanNode) compressedTree.getRoot();
		assertEquals(4, root.getChildren().size());
		assertEquals(NodeType.OR, root.getNodeType());

		Node child = root.getChildren().get(2);
		assertEquals(CompressedBooleanNode.class, child.getClass());
		CompressedBooleanNode andChild = (CompressedBooleanNode) child;
		assertEquals(NodeType.AND, andChild.getNodeType());
		assertEquals(2, andChild.getChildren().size());
		assertEquals("three", andChild.getFirstChild().getText());
		assertEquals("four", andChild.getLastChild().getText());
	}

	@Test
	public void testCompressParseTree4() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "one or not not two";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		ParseTree compressedTree = parseTree.compress();
		assertEquals("(one OR two)", compressedTree.toString());
		CompressedBooleanNode root = (CompressedBooleanNode) compressedTree.getRoot();
		assertEquals(2, root.getChildren().size());
		assertEquals(TextNode.class, root.getFirstChild().getClass());
		assertEquals(TextNode.class, root.getLastChild().getClass());
	}

	@Test
	public void testCompressParseTree5() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "not not not not not two";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		ParseTree compressedTree = parseTree.compress();
		assertEquals("(NOT two)", compressedTree.toString());
	}

	@Ignore
	@Test
	public void testCompressParseTree6() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "x or y or (q and r and s and t regulates u)";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		ParseTree compressedTree = parseTree.compress();
		assertEquals("(x OR y OR (q AND r AND s AND (t regulates u)))", compressedTree.toString());
	}

	@Ignore
	@Test
	public void testCompressParseTreePreOrderTraversal() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "one and two or three regulates four";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		ParseTree compressedTree = parseTree.compress();
		assertEquals("((one AND two) OR (three regulates four))", compressedTree.toString());
		List<Node> traversal = new ArrayList<>();
		ParseTree.traversePreOrder(compressedTree.getRoot(), traversal, false);
		assertEquals("OR", traversal.get(0).getText());
		assertEquals("AND", traversal.get(1).getText());
		assertEquals("one", traversal.get(2).getText());
		assertEquals("two", traversal.get(3).getText());
		assertEquals("regulates", traversal.get(4).getText());
	}

	@Test
	public void testCompressParseTreePreOrderTraversal1() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "one or two or three or four and five";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		ParseTree compressedTree = parseTree.compress();
		List<Node> traversal = new ArrayList<>();
		ParseTree.traversePreOrder(compressedTree.getRoot(), traversal, false);
		assertEquals(7, traversal.size());
		assertEquals("OR", traversal.get(0).getText());
		assertEquals("one", traversal.get(1).getText());
		assertEquals("two", traversal.get(2).getText());
		assertEquals("three", traversal.get(3).getText());
		assertEquals("AND", traversal.get(4).getText());
		assertEquals("four", traversal.get(5).getText());
		assertEquals("five", traversal.get(6).getText());
	}

	@Deprecated
	@Ignore
	@Test
	public void testCompressParseTreePreOrderTraversal2() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "x or y or (q and r and s and t regulates u)";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		ParseTree compressedTree = parseTree.compress();
		List<Node> traversal = new ArrayList<>();
		ParseTree.traversePreOrder(compressedTree.getRoot(), traversal, false);
		assertEquals(8, traversal.size());
		assertEquals("OR", traversal.get(0).getText());
		assertEquals("x", traversal.get(1).getText());
		assertEquals("y", traversal.get(2).getText());
		assertEquals("AND", traversal.get(3).getText());
		assertEquals("q", traversal.get(4).getText());
		assertEquals("r", traversal.get(5).getText());
		assertEquals("s", traversal.get(6).getText());
		assertEquals("regulates", traversal.get(7).getText());
	}

	@Ignore
	@Test
	public void testHeight() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "x or y or (q and r and s and t regulates u) and not v";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		ParseTree compressedTree = parseTree.compress();
		List<Node> traversal = new ArrayList<>();
		ParseTree.traversePreOrder(compressedTree.getRoot(), traversal, false);
		assertEquals(10, traversal.size());
		assertEquals("OR", traversal.get(0).getText());
		assertEquals(0, traversal.get(0).getHeight());
		assertEquals("x", traversal.get(1).getText());
		assertEquals(1, traversal.get(1).getHeight());
		assertEquals("y", traversal.get(2).getText());
		assertEquals(1, traversal.get(2).getHeight());
		assertEquals("AND", traversal.get(3).getText());
		assertEquals(1, traversal.get(3).getHeight());
		assertEquals("q", traversal.get(4).getText());
		assertEquals(2, traversal.get(4).getHeight());
		assertEquals("r", traversal.get(5).getText());
		assertEquals(2, traversal.get(5).getHeight());
		assertEquals("s", traversal.get(6).getText());
		assertEquals(2, traversal.get(6).getHeight());
		assertEquals("regulates", traversal.get(7).getText());
		assertEquals(2, traversal.get(7).getHeight());
		assertEquals("NOT", traversal.get(8).getText());
		assertEquals(2, traversal.get(8).getHeight());
		assertEquals("v", traversal.get(9).getText());
		assertEquals(3, traversal.get(9).getHeight());
	}

	@Test
	public void testHeight2() throws Exception {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "x";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		ParseTree compressedTree = parseTree.compress();
		assertEquals(0, compressedTree.getRoot().getHeight());
		TextNode yNode = new TextNode("y");
		compressedTree.add(compressedTree.getRoot(), yNode, NodeType.OR);
		assertEquals("OR", compressedTree.getRoot().getText());
		assertEquals(0, compressedTree.getRoot().getHeight());
		for (Node rootChild : ((BranchNode) compressedTree.getRoot()).getChildren())
			assertEquals(1, rootChild.getHeight());
		assertEquals("(x OR y)", compressedTree.toString());

		Node zNode = new TextNode("z");
		compressedTree.add(yNode, zNode, NodeType.AND);
		assertEquals("(x OR (y AND z))", compressedTree.toString());
		assertEquals(2, yNode.getHeight());
		assertEquals(2, zNode.getHeight());

		compressedTree.remove(yNode);
		assertEquals("(x OR z)", compressedTree.toString());
		assertEquals(0, compressedTree.getRoot().getHeight());
		for (Node rootChild : ((BranchNode) compressedTree.getRoot()).getChildren())
			assertEquals(1, rootChild.getHeight());

		compressedTree.remove("x");
		assertEquals("z", compressedTree.toString());
		assertEquals(0, compressedTree.getRoot().getHeight());
	}

	@Test
	public void testParenthesisExpression() throws IOException {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "p70(s6)k";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("p70(s6)k", parseTree.compress().toString());
	}

	@Test
	public void testParenthesisError() {
		// Actually here we test a paranthesis error: If we can't make sense of
		// parenthesis and they do not seem to belong to an expression itself,
		// we ignore parenthesis.
		// A strange effect is that the search term 'kinase' is interpreted as a
		// negation. Should it be this way? That's not really clear.
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "p70[S6)-kinase";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(p70 AND S6 AND kinase)", parseTree.compress().toString());
	}

	@Test
	public void testParenthesisError2() {
		// the single parenthesis should be ignored, it makes no sense
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "this ) is a query";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		parseTree = parseTree.compress();
		assertEquals("(this AND is AND a AND query)", parseTree.toString());
	}
	
	@Test
	public void testReplace() throws Exception {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "x";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		
		QueryToken qt = new QueryToken(2, 3, "y");
		TextNode y = new TextNode(qt.getOriginalValue(), qt);
		
		parseTree.add(parseTree.getRoot(), y, NodeType.AND);
		
		qt = new QueryToken(2, 3, "z");
		TextNode z = new TextNode(qt.getOriginalValue(), qt);
		parseTree.replaceNode(parseTree.getNode("y"), z);
		
		assertEquals("(x AND z)", parseTree.toString());
		
		System.out.println(parseTree.toString(SERIALIZATION.IDS));
	}

	@AfterClass
	public static void shutdown() {
		registry.shutdown();
	}

	private Map<Long, Node> getIdMap(ParseTree parseTree) throws Exception {
		Field idMapField = ParseTree.class.getDeclaredField("idMap");
		idMapField.setAccessible(true);
		Map<Long, Node> idMap = (Map<Long, Node>) idMapField.get(parseTree);
		return idMap;
	}

	private Map<String, Node> getTextMap(ParseTree parseTree) throws Exception {
		Field idMapField = ParseTree.class.getDeclaredField("textMap");
		idMapField.setAccessible(true);
		Map<String, Node> idMap = (Map<String, Node>) idMapField.get(parseTree);
		return idMap;
	}
}
