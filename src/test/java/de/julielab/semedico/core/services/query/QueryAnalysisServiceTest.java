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
import de.julielab.semedico.core.facetterms.FacetTerm;
import de.julielab.semedico.core.parsing.BinaryNode;
import de.julielab.semedico.core.parsing.EventNode;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.ParseTree.SERIALIZATION;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.search.query.UserQuery;
import de.julielab.semedico.core.parsing.TextNode;
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

	@Test
	public void testUserQueryTokensBoolean1() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);

		// The query will be: mapk14 or immunosuppression or immunosupression
		// The latter two terms are ambiguous. We test whether we the correct user choices as expected

		// "user input" token 'mapk14'
		QueryToken qt1 = new QueryToken(0, 6);
		qt1.setOriginalValue("mapk14");
		qt1.addTermToList(new FacetTerm(NodeIDPrefixConstants.TERM + 1839));
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
		qt3.addTermToList(new FacetTerm(NodeIDPrefixConstants.TERM + 1257));
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
		qt5.addTermToList(new FacetTerm(NodeIDPrefixConstants.TERM + 158));
		qt5.setInputTokenType(TokenType.CONCEPT);
		qt5.setType(QueryTokenizerImpl.ALPHANUM);

		UserQuery userQuery = new UserQuery();
		userQuery.tokens = Arrays.asList(qt1, qt2, qt3, qt4, qt5);
		ParseTree parseTree = queryAnalysisService.analyseQueryString(userQuery, 0);

		assertEquals(5, parseTree.getNumberNodes());
		assertEquals(3, parseTree.getNumberConceptNodes());
		assertEquals("((tid1839 OR tid1257) OR tid158)", parseTree.toString(SERIALIZATION.TERMS));
	}
}
