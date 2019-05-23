package de.julielab.semedico.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.test.PageTester;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

//import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;

@Ignore
public class TokenInputServiceTest {
	private static Registry registry;
	private static ITokenInputService tokenInputService;

//	@BeforeClass
//	public static void setup() {
//		org.junit.Assume.assumeTrue(TestUtils.isAddressReachable(TestUtils.neo4jTestEndpoint));
//		registry = new PageTester("de.julielab.semedico", "SemedicoFrontend").getRegistry();
//		tokenInputService = registry.getService(ITokenInputService.class);
//	}

	@AfterClass
	public static void shutdown() {
		registry.shutdown();
	}

	@Test
	public void testConvertToQueryTokens1() {
		// concept token, boolean operator and freetext
		JSONArray userInputTokens =
				new JSONArray(" [\n" + "  {\n"
						+ "    \"tokentype\" : CONCEPT,\n"
						+ "    \"name\" : \"Mapk14\",\n"
						+ "    \"facetid\" : \"fid11\",\n"
						+ "    \"tokenid\" : \"tid1839\",\n"
						+ "    \"freetext\" : false\n"
						+ "  },\n"
						+ "  {\n"
						+ "    \"tokentype\" : AND,\n"
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
		assertEquals(1, qt.getTermList().size());
		assertEquals("Mapk14", qt.getOriginalValue());
		assertFalse(qt.isFreetext());
		assertEquals("ALPHANUM", qt.getType());

		qt = queryTokens.get(1);
		assertEquals(0, qt.getTermList().size());
		assertEquals("AND", qt.getOriginalValue());
		assertFalse(qt.isFreetext());
		assertEquals("AND", qt.getType());
		
		qt = queryTokens.get(2);
		assertEquals(0, qt.getTermList().size());
		assertEquals("this thing regulates other", qt.getOriginalValue());
		assertTrue(qt.isFreetext());
		assertEquals("ALPHANUM", qt.getType());
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
						+ "    \"tokentype\" : KEYWORD,\n"
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
		assertEquals(1, qt.getTermList().size());
		assertEquals("Any term", qt.getOriginalValue());
		assertFalse(qt.isFreetext());
		assertEquals("ALPHANUM", qt.getType());
		
		qt = queryTokens.get(1);
		assertEquals(1, qt.getTermList().size());
		assertEquals("keyword", qt.getOriginalValue());
		assertFalse(qt.isFreetext());
		assertEquals("ALPHANUM", qt.getType());
	}
	
}
