package de.julielab.semedico.core.services.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facetterms.CoreTerm;
import de.julielab.semedico.core.facetterms.CoreTerm.CoreTermType;
import de.julielab.semedico.core.parsing.BinaryNode;
import de.julielab.semedico.core.parsing.EventNode;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.ParseTree.SERIALIZATION;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.query.UserQuery;
import de.julielab.semedico.core.services.interfaces.IQueryAnalysisService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

public class QueryAnalysisServiceTest {
	private static Registry registry;

	@BeforeClass
	public static void setup() {
		registry = TestUtils.createTestRegistry();
		org.junit.Assume.assumeTrue(TestUtils.isAddressReachable(TestUtils.neo4jTestEndpoint));
		
		// set the default operator for this test, does not depend on actual productivity mode
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

	@AfterClass
	public static void shutdown() {
		registry.shutdown();
	}

	@Deprecated
	@Ignore
	@Test
	public void testEventQueryAnalysis() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "isl1 regulation";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(isl1 regulation)", parseTree.toString());
	}

	@Test
	@Deprecated@Ignore
	public void testEventQueryAnalysis1() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "expression isl1";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(isl1 expression)", parseTree.toString());
	}
	
	@Deprecated@Ignore
	@Test
	public void testEventQueryAnalysis2() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a regulates binding";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(a regulates binding)", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testEventQueryAnalysis3() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a regulates *";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(a regulates *)", parseTree.toString());
		EventNode eventNode = (EventNode) parseTree.getRoot();
		List<Node> secondArgumentNodes = eventNode.getSecondArgumentNodes();
		assertEquals(1, secondArgumentNodes.size());
		TextNode argument = (TextNode) secondArgumentNodes.get(0);
		List<? extends IConcept> terms = argument.getTerms();
		assertEquals(1, terms.size());
		assertEquals(CoreTermType.ANY_TERM, ((CoreTerm) terms.get(0)).getCoreTermType());
	}
	@Deprecated@Ignore
	@Test
	public void testBooleanEventQueryAnalysis() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "ic50 or iga regulates igd";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertTrue(parseTree.getRoot().getClass().equals(BinaryNode.class));
		assertEquals(BinaryNode.OR, ((BinaryNode) parseTree.getRoot()).getText());
		assertEquals(2, parseTree.getNumberConceptNodes());
		assertEquals("(ic50 OR (iga regulates igd))", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testBooleanEventQueryAnalysis2() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a or b regulates c and d";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(a OR ((b regulates c) AND d))", parseTree.toString());
	}

	@Test@Deprecated@Ignore
	public void testBooleanEventQueryAnalysis3() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a regulates b and c binds d and e";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(((a regulates b) AND (c binds d)) AND e)", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testBooleanEventQueryAnalysis4() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a regulates b c binds d and e";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(((a regulates b) AND (c binds d)) AND e)", parseTree.toString());
	}

	@Test@Deprecated@Ignore
	public void testBooleanEventQueryAnalysis5() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a regulates b binding c and d";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(((a regulates b) AND (c binding)) AND d)", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testBooleanEventQueryAnalysis6() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "not a regulates b";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		// TODO I think this query is currently not supported by our elasticsearch event support!
		assertEquals("((NOT a) regulates b)", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testBooleanEventQueryAnalysis7() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "(a or b) regulates (b and (c or d))";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("((a OR b) regulates (b AND (c OR d)))", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testBooleanEventQueryAnalysis8() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "(a and b) regulates (b or c) and d";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(((a AND b) regulates (b OR c)) AND d)", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testBooleanEventQueryAnalysis9() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "(a and not b) regulates d";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("((a AND (NOT b)) regulates d)", parseTree.toString());
		EventNode root = (EventNode) parseTree.getRoot();
		List<Node> childrenInOrder = root.getChildrenInOrder(0);
		System.out.println(childrenInOrder);
	}
	@Deprecated@Ignore
	@Test
	public void testBooleanEventQueryAnalysis10() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a and not regulates b";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(a AND (NOT (b regulates)))", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testBooleanEventQueryAnalysis11() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "binds stopword regulates b";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("((stopword binds) AND (b regulates))", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testBooleanEventQueryAnalysis12() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a (regulates) b";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("((a AND regulates) AND b)", parseTree.toString());
	}

	@Test
	public void testBooleanQueryAnalysis() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a or b and c and d";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(a OR ((b AND c) AND d))", parseTree.toString());
	}

	@Test
	public void testBooleanQueryAnalysis2() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "(a or b) and c and d";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(((a OR b) AND c) AND d)", parseTree.toString());
	}

	@Test
	public void testBooleanQueryAnalysis3() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "(a or (b and (c or d))) and e or (f or (g and h))";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(((a OR (b AND (c OR d))) AND e) OR (f OR (g AND h)))", parseTree.toString());
	}

	@Test
	public void testBooleanQueryAnalysis4() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a or b or c and d and e or f";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(((a OR b) OR ((c AND d) AND e)) OR f)", parseTree.toString());
	}

	@Test
	public void testBooleanQueryAnalysis5() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a or not b";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(a OR (NOT b))", parseTree.toString());
	}

	@Test
	public void testBooleanQueryAnalysis6() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a or not b and c";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(a OR ((NOT b) AND c))", parseTree.toString());
	}

	@Test
	public void testBooleanQueryAnalysis7() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a and b or c and d";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("((a AND b) OR (c AND d))", parseTree.toString());
	}

	@Test
	public void testBooleanQueryAnalysis8() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a and not (b or c) and d";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("((a AND (NOT (b OR c))) AND d)", parseTree.toString());
	}

	@Test
	public void testBooleanQueryAnalysis9() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a b";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(a AND b)", parseTree.toString());
	}

	@Test
	public void testBooleanQueryAnalysis10() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a b or c";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("((a AND b) OR c)", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testEventCoreTerms() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "a * binds mefc2";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(a AND (* binds mefc2))", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testEventCoreTerm2() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "* binds mefc2";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertTrue(parseTree.getRoot().getClass().equals(EventNode.class));
		EventNode eventNode = (EventNode) parseTree.getRoot();
		List<Node> firstArgumentNodes = eventNode.getFirstArgumentNodes();
		assertEquals(1, firstArgumentNodes.size());
		TextNode textNode = (TextNode) firstArgumentNodes.get(0);
		List<? extends IConcept> terms = textNode.getTerms();
		assertEquals(1, terms.size());
		assertEquals(CoreTerm.class, terms.get(0).getClass());
		CoreTerm term = (CoreTerm) terms.get(0);
		assertEquals(ConceptType.CORE, term.getConceptType());
		assertEquals(CoreTerm.CoreTermType.ANY_TERM, term.getCoreTermType());
	}
	@Deprecated@Ignore
	@Test
	public void testEventCoreTerm3() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "* * mefc2";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertTrue(parseTree.getRoot().getClass().equals(EventNode.class));
		EventNode eventNode = (EventNode) parseTree.getRoot();

		// check any event argument
		List<Node> firstArgumentNodes = eventNode.getFirstArgumentNodes();
		assertEquals(1, firstArgumentNodes.size());
		TextNode textNode = (TextNode) firstArgumentNodes.get(0);
		List<? extends IConcept> terms = textNode.getTerms();
		assertEquals(1, terms.size());
		assertEquals(CoreTerm.class, terms.get(0).getClass());
		CoreTerm term = (CoreTerm) terms.get(0);
		assertEquals(ConceptType.CORE, term.getConceptType());
		assertEquals(CoreTerm.CoreTermType.ANY_TERM, term.getCoreTermType());

		// check any event type
		List<IConcept> eventTypes = eventNode.getEventTypes();
		assertEquals(1, eventTypes.size());
		assertEquals(CoreTerm.class, eventTypes.get(0).getClass());
		CoreTerm eventType = (CoreTerm) eventTypes.get(0);
		assertEquals(CoreTermType.ANY_MOLECULAR_INTERACTION, eventType.getCoreTermType());
	}
	@Deprecated@Ignore
	@Test
	public void testEventCoreTerm4() {
		// exact same test as above, just other synonyms...
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "any interacts mefc2";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertTrue(parseTree.getRoot().getClass().equals(EventNode.class));
		EventNode eventNode = (EventNode) parseTree.getRoot();

		// check any event argument
		List<Node> firstArgumentNodes = eventNode.getFirstArgumentNodes();
		assertEquals(1, firstArgumentNodes.size());
		TextNode textNode = (TextNode) firstArgumentNodes.get(0);
		List<? extends IConcept> terms = textNode.getTerms();
		assertEquals(1, terms.size());
		assertEquals(CoreTerm.class, terms.get(0).getClass());
		CoreTerm term = (CoreTerm) terms.get(0);
		assertEquals(ConceptType.CORE, term.getConceptType());
		assertEquals(CoreTerm.CoreTermType.ANY_TERM, term.getCoreTermType());

		// check any event type
		List<IConcept> eventTypes = eventNode.getEventTypes();
		assertEquals(1, eventTypes.size());
		assertEquals(CoreTerm.class, eventTypes.get(0).getClass());
		CoreTerm eventType = (CoreTerm) eventTypes.get(0);
		assertEquals(CoreTermType.ANY_MOLECULAR_INTERACTION, eventType.getCoreTermType());
	}
	@Deprecated@Ignore
	@Test
	public void testEventCoreTerm5() {
		// exact same test as above, just other synonyms...
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "? interacts * any all mefc2";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("((? interacts *) AND (any all mefc2))", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testEventCoreTerm6() {
		// exact same test as above, just other synonyms...
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "* * *";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(* * *)", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testEventCoreTerm7() {
		// exact same test as above, just other synonyms...
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "* * * * *";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("((* * *) AND (* *))", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testEventCoreTerm8() {
		// exact same test as above, just other synonyms...
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "any (interacts mefc2)";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("(any AND (mefc2 interacts))", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testEventCoreTerm9() {
		// exact same test as above, just other synonyms...
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "(interacts mefc2) any";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("((mefc2 interacts) AND any)", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testEventCoreTerm10() {
		// exact same test as above, just other synonyms...
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "*";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("*", parseTree.toString());
		TextNode root = (TextNode) parseTree.getRoot();
		List<? extends IConcept> terms = root.getTerms();
		assertEquals(1, terms.size());
		assertEquals(ConceptType.CORE, terms.get(0).getConceptType());
		CoreTerm anyTerm = (CoreTerm) terms.get(0);
		assertEquals(CoreTermType.ANY_TERM, anyTerm.getCoreTermType());
	}
	@Deprecated@Ignore
	@Test
	public void testEventCoreTerm11() {
		// exact same test as above, just other synonyms...
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "isl1 regulation x *";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		assertEquals("((isl1 regulation x) AND *)", parseTree.toString());
		BinaryNode root = (BinaryNode) parseTree.getRoot();
		// the wildcard token
		TextNode secondChild = (TextNode) root.getRightChild();
		List<? extends IConcept> terms = secondChild.getTerms();
		assertEquals(1, terms.size());
		assertEquals(ConceptType.CORE, terms.get(0).getConceptType());
		CoreTerm anyTerm = (CoreTerm) terms.get(0);
		assertEquals(CoreTermType.ANY_TERM, anyTerm.getCoreTermType());
	}

	@Test
	public void testUserQueryTokensBoolean1() {
		ITermService termService = registry.getService(ITermService.class);
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);

		// The query will be: mapk14 or immunosuppression or immunosupression
		// The latter two terms are ambiguous. We test whether we the correct user choices as expected

		// "user input" token 'mapk14'
		QueryToken qt1 = new QueryToken(0, 6);
		qt1.setOriginalValue("mapk14");
		qt1.addTermToList(termService.getTerm(NodeIDPrefixConstants.TERM + 1839));
		qt1.setInputTokenType(TokenType.CONCEPT);
		qt1.setType(QueryTokenizerImpl.ALPHANUM);

		// "user input" token 'or'
		QueryToken qt2 = new QueryToken(7, 9);
		qt2.setOriginalValue("or");
		qt2.setInputTokenType(TokenType.OR);
		qt2.setType(QueryTokenizerImpl.OR_OPERATOR);

		// "user input" token 'immunosuppression' which is ambiguous in our testdata (terms 1257 and 158)
		QueryToken qt3 = new QueryToken(10, 27);
		qt3.setOriginalValue("immunosuppression");
		qt3.addTermToList(termService.getTerm(NodeIDPrefixConstants.TERM + 1257));
		qt3.setInputTokenType(TokenType.CONCEPT);
		qt3.setType(QueryTokenizerImpl.ALPHANUM);

		// "user input" token 'or'
		QueryToken qt4 = new QueryToken(28, 30);
		qt4.setOriginalValue("or");
		qt4.setInputTokenType(TokenType.OR);
		qt4.setType(QueryTokenizerImpl.OR_OPERATOR);

		// "user input" token 'immunosuppression' which is ambiguous in our testdata (terms 1257 and 158)
		QueryToken qt5 = new QueryToken(31, 49);
		qt5.setOriginalValue("immunosuppression");
		qt5.addTermToList(termService.getTerm(NodeIDPrefixConstants.TERM + 158));
		qt5.setInputTokenType(TokenType.CONCEPT);
		qt5.setType(QueryTokenizerImpl.ALPHANUM);

		UserQuery userQuery = new UserQuery();
		userQuery.tokens = Arrays.asList(qt1, qt2, qt3, qt4, qt5);
		ParseTree parseTree = queryAnalysisService.analyseQueryString(userQuery, 0);

		assertEquals(5, parseTree.getNumberNodes());
		assertEquals(3, parseTree.getNumberConceptNodes());
		assertEquals("((tid1839 OR tid1257) OR tid158)", parseTree.toString(SERIALIZATION.TERMS));
	}
	@Deprecated@Ignore
	@Test
	public void testUserQueryTokensEvents1() {
		ITermService termService = registry.getService(ITermService.class);
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);

		// The query will be: mapk14 binds becn1

		// "user input" token 'mapk14'
		QueryToken qt1 = new QueryToken(0, 6);
		qt1.setOriginalValue("mapk14");
		qt1.addTermToList(termService.getTerm(NodeIDPrefixConstants.TERM + 1839));
		qt1.setFreetext(false);
		qt1.setType(QueryTokenizerImpl.ALPHANUM);

		// "user input" token 'binds'
		QueryToken qt2 = new QueryToken(7, 12);
		qt2.setOriginalValue("binds");
		qt2.addTermToList(termService.getTerm(NodeIDPrefixConstants.TERM + 1847));
		qt2.setFreetext(false);
		qt2.setType(QueryTokenizerImpl.BINARY_EVENT);

		// "user input" token 'becn1'
		QueryToken qt3 = new QueryToken(13, 18);
		qt3.setOriginalValue("becn1");
		qt3.addTermToList(termService.getTerm(NodeIDPrefixConstants.TERM + 1841));
		qt3.setFreetext(false);
		qt3.setType(QueryTokenizerImpl.ALPHANUM);

		UserQuery userQuery = new UserQuery();
		userQuery.tokens = Arrays.asList(qt1, qt2, qt3);
		ParseTree parseTree = queryAnalysisService.analyseQueryString(userQuery, 0);

		assertEquals("(mapk14 binds becn1)", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testUserQueryTokensEvents2() {
		ITermService termService = registry.getService(ITermService.class);
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);

		// The query will be: mapk14 binding

		// "user input" token 'mapk14'
		QueryToken qt1 = new QueryToken(0, 6);
		qt1.setOriginalValue("mapk14");
		qt1.addTermToList(termService.getTerm(NodeIDPrefixConstants.TERM + 1839));
		qt1.setFreetext(false);
		qt1.setType(QueryTokenizerImpl.ALPHANUM);

		// "user input" token 'binding'
		QueryToken qt2 = new QueryToken(7, 14);
		qt2.setOriginalValue("binding");
		qt2.addTermToList(termService.getTerm(NodeIDPrefixConstants.TERM + 1847));
		qt2.setFreetext(false);
		qt2.setType(QueryTokenizerImpl.UNARY_OR_BINARY_EVENT);

		UserQuery userQuery = new UserQuery();
		userQuery.tokens = Arrays.asList(qt1, qt2);
		ParseTree parseTree = queryAnalysisService.analyseQueryString(userQuery, 0);

		assertEquals("(mapk14 binding)", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testUserQueryTokensEvents3() {
		ITermService termService = registry.getService(ITermService.class);
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);

		// The query will be: (mapk14 binding) becn1

		// "user input" token '('
		QueryToken qt1 = new QueryToken(0, 1);
		qt1.setOriginalValue("(");
		qt1.setFreetext(false);
		qt1.setType(QueryTokenizerImpl.LEFT_PARENTHESIS);

		// "user input" token 'mapk14'
		QueryToken qt2 = new QueryToken(1, 7);
		qt2.setOriginalValue("mapk14");
		qt2.addTermToList(termService.getTerm(NodeIDPrefixConstants.TERM + 1839));
		qt2.setFreetext(false);
		qt2.setType(QueryTokenizerImpl.ALPHANUM);

		// "user input" token 'binding'
		QueryToken qt3 = new QueryToken(8, 15);
		qt3.setOriginalValue("binding");
		qt3.addTermToList(termService.getTerm(NodeIDPrefixConstants.TERM + 1847));
		qt3.setFreetext(false);
		qt3.setType(QueryTokenizerImpl.UNARY_OR_BINARY_EVENT);

		// "user input" token ')'
		QueryToken qt4 = new QueryToken(15, 16);
		qt4.setOriginalValue(")");
		qt4.setFreetext(false);
		qt4.setType(QueryTokenizerImpl.RIGHT_PARENTHESIS);

		// "user input" token 'becn1'
		QueryToken qt5 = new QueryToken(17, 22);
		qt5.setOriginalValue("becn1");
		qt5.addTermToList(termService.getTerm(NodeIDPrefixConstants.TERM + 1841));
		qt5.setFreetext(false);
		qt5.setType(QueryTokenizerImpl.ALPHANUM);

		UserQuery userQuery = new UserQuery();
		userQuery.tokens = Arrays.asList(qt1, qt2, qt3, qt4, qt5);
		ParseTree parseTree = queryAnalysisService.analyseQueryString(userQuery, 0);

		assertEquals("((mapk14 binding) AND becn1)", parseTree.toString());
	}
	@Deprecated@Ignore
	@Test
	public void testUserQueryTokensEvents4() {
		ITermService termService = registry.getService(ITermService.class);
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);

		// The query will be: mapk14 binding becn1 regulation
		// we expect that the binary event "mapk14 binds becn1" will be recognized and "regulation" will just be a
		// concept term

		// "user input" token 'mapk14'
		QueryToken qt1 = new QueryToken(0, 6);
		qt1.setOriginalValue("mapk14");
		qt1.addTermToList(termService.getTerm(NodeIDPrefixConstants.TERM + 1839));
		qt1.setFreetext(false);
		qt1.setType(QueryTokenizerImpl.ALPHANUM);

		// "user input" token 'binding'
		QueryToken qt2 = new QueryToken(7, 14);
		qt2.setOriginalValue("binding");
		qt2.addTermToList(termService.getTerm(NodeIDPrefixConstants.TERM + 1847));
		qt2.setFreetext(false);
		qt2.setType(QueryTokenizerImpl.UNARY_OR_BINARY_EVENT);

		// free text user input 'becn1 regulation', must be recognized and aligned
		QueryToken qt3 = new QueryToken(15, 31);
		qt3.setOriginalValue("becn1 regulation");
		qt3.setFreetext(true);
		qt3.setType(QueryTokenizerImpl.ALPHANUM);

		UserQuery userQuery = new UserQuery();
		userQuery.tokens = Arrays.asList(qt1, qt2, qt3);
		ParseTree parseTree = queryAnalysisService.analyseQueryString(userQuery, 0);

		assertEquals("((mapk14 binding becn1) AND regulation)", parseTree.toString());
	}
}
