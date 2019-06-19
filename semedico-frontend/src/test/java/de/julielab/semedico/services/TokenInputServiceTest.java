package de.julielab.semedico.services;

import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.query.QueryTokenizerImpl;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.test.PageTester;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TokenInputServiceTest {
	private static Registry registry;
	private static ITokenInputService tokenInputService;

	@BeforeClass
	public static void setup() {
		//org.junit.Assume.assumeTrue(TestUtils.isAddressReachable(TestUtils.neo4jTestEndpoint));
		registry = new PageTester("de.julielab.semedico", "SemedicoFrontend").getRegistry();
		tokenInputService = registry.getService(ITokenInputService.class);
	}

	@AfterClass
	public static void shutdown() {
		registry.shutdown();
	}

	@Test
	public void testConvertToQueryTokens1() {
		// concept token, boolean operator and freetext
		JSONArray userInputTokens =
				new JSONArray(" [\n" + "  {\n"
						+ "    \"tokentype\" : 0,\n"
						+ "    \"name\" : \"Mapk14\",\n"
						+ "    \"facetid\" : \"fid11\",\n"
						+ "    \"tokenid\" : \"tid1839\",\n"
						+ "    \"freetext\" : false\n"
						+ "  },\n"
						+ "  {\n"
						+ "    \"tokentype\" : 18,\n"
						+ "    \"name\" : \"AND\",\n"
						+ "    \"facetid\" : \"fid-3\",\n"
						+ "    \"tokenid\" : \"AND\",\n"
						+ "    \"freetext\" : false\n"
						+ "  },\n"
						+ "  {\n"
						+ "    \"name\" : \"this thing regulates other\",\n"
						+ "    \"facetid\" : \"fid-1\",\n"
						+ "    \"tokenid\" : \"this thing regulates other\",\n"
						+ "    \"freetext\" : true\n"
						+ "  }\n"
						+ "]");
		List<QueryToken> queryTokens = tokenInputService.convertToQueryTokens(userInputTokens);

		assertEquals(3, queryTokens.size());
		QueryToken qt;
		qt = queryTokens.get(0);
		assertEquals(1, qt.getConceptList().size());
		assertEquals("Mapk14", qt.getOriginalValue());
		assertFalse(qt.isFreetext());
		assertEquals(QueryTokenizerImpl.ALPHANUM, qt.getType());

		qt = queryTokens.get(1);
		assertEquals(0, qt.getConceptList().size());
		assertEquals("AND", qt.getOriginalValue());
		assertFalse(qt.isFreetext());
		assertEquals(QueryTokenizerImpl.AND_OPERATOR, qt.getType());
		
		qt = queryTokens.get(2);
		assertEquals(0, qt.getConceptList().size());
		assertEquals("this thing regulates other", qt.getOriginalValue());
		assertTrue(qt.isFreetext());
		assertEquals(QueryTokenizerImpl.ALPHANUM, qt.getType());
	}
	
	@Test
	public void testConvertToQueryTokens2() {
		// wildcard term and keyword token
		JSONArray userInputTokens =
				new JSONArray(" [\n" + "  {\n"
						+ "    \"tokentype\" : 0,\n"
						+ "    \"name\" : \"Any term\",\n"
						+ "    \"facetid\" : \"fid-2\",\n"
						+ "    \"tokenid\" : \"ctid0\",\n"
						+ "    \"freetext\" : false\n"
						+ "  },\n"
						+ "  {\n"
						+ "    \"tokentype\" : 0,\n"
						+ "    \"name\" : \"keyword\",\n"
						+ "    \"facetid\" : \"fid-1\",\n"
						+ "    \"tokenid\" : \"keyword\",\n"
						+ "    \"freetext\" : false\n"
						+ "  },\n"
					+ "]");
		List<QueryToken> queryTokens = tokenInputService.convertToQueryTokens(userInputTokens);

		assertEquals(2, queryTokens.size());
		QueryToken qt;
		qt = queryTokens.get(0);
		assertEquals(1, qt.getConceptList().size());
		assertEquals("Any term", qt.getOriginalValue());
		assertFalse(qt.isFreetext());
		assertEquals(QueryTokenizerImpl.ALPHANUM, qt.getType());
		
		qt = queryTokens.get(1);
		assertEquals(1, qt.getConceptList().size());
		assertEquals("keyword", qt.getOriginalValue());
		assertFalse(qt.isFreetext());
		assertEquals(QueryTokenizerImpl.ALPHANUM, qt.getType());
	}
	
	@Test
	public void testConvertToQueryTokens3() {
		// event
		JSONArray userInputTokens =
				new JSONArray(" [\n" + "  {\n"
						+ "    \"tokentype\" : 0,\n"
						+ "    \"name\" : \"Mapk14\",\n"
						+ "    \"facetid\" : \"fid11\",\n"
						+ "    \"tokenid\" : \"tid1839\",\n"
						+ "    \"freetext\" : false\n"
						+ "  },\n"
						+ "  {\n"
						+ "    \"tokentype\" : "+QueryTokenizerImpl.UNARY_OR_BINARY_EVENT+",\n"
						+ "    \"name\" : \"regulation\",\n"
						+ "    \"facetid\" : \"fid12\",\n"
						+ "    \"tokenid\" : \"tid1852\",\n"
						+ "    \"freetext\" : false\n"
						+ "  },\n"
						+ "  {\n"
						+ "    \"tokentype\" : 0,\n"
						+ "    \"name\" : \"Becn1\",\n"
						+ "    \"facetid\" : \"fid11\",\n"
						+ "    \"tokenid\" : \"tid1841\",\n"
						+ "    \"freetext\" : false\n"
						+ "  }\n"
						+ "]");
		List<QueryToken> queryTokens = tokenInputService.convertToQueryTokens(userInputTokens);

		assertEquals(3, queryTokens.size());
		QueryToken qt;
		qt = queryTokens.get(0);
		assertEquals(1, qt.getConceptList().size());
		assertEquals("Mapk14", qt.getOriginalValue());
		assertFalse(qt.isFreetext());
		assertEquals(QueryTokenizerImpl.ALPHANUM, qt.getType());

		qt = queryTokens.get(1);
		assertEquals(1, qt.getConceptList().size());
		assertEquals("regulation", qt.getOriginalValue());
		assertFalse(qt.isFreetext());
		assertEquals(QueryTokenizerImpl.UNARY_OR_BINARY_EVENT, qt.getType());
		//assertTrue(qt.isUnaryEvent());
		//assertTrue(qt.isBinaryEvent());
		
		qt = queryTokens.get(2);
		assertEquals(1, qt.getConceptList().size());
		assertEquals("Becn1", qt.getOriginalValue());
		assertFalse(qt.isFreetext());
		assertEquals(QueryTokenizerImpl.ALPHANUM, qt.getType());
	}
}
