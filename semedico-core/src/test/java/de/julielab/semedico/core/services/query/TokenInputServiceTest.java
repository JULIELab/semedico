package de.julielab.semedico.core.services.query;

import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.json.JSONArray;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;


@Test(groups = {"integration", "neo4j"})
public class TokenInputServiceTest {
	private static Registry registry;
	private static ITokenInputService tokenInputService;

	@BeforeClass
	public static void setup() {
		registry = TestUtils.createTestRegistry();
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
						+ "    \"tokentype\" : CONCEPT,\n"
						+ "    \"name\" : \"Concept 42\",\n"
						+ "    \"facetid\" : \"fid0\",\n"
						+ "    \"conceptid\" : \"tid42\"\n"
						+ "  },\n"
						+ "  {\n"
						+ "    \"tokentype\" : AND,\n"
						+ "    \"name\" : \"AND\",\n"
						+ "    \"facetid\" : \"fid-3\",\n"
						+ "    \"conceptid\" : \"AND\"\n"
						+ "  },\n"
						+ "  {\n"
						+ "    \"name\" : \"this thing regulates other\",\n"
						+ "    \"facetid\" : \"fid-1\",\n"
						+ "    \"conceptid\" : \"this thing regulates other\",\n"
						+ "    \"tokentype\" : \"FREETEXT\"\n"
						+ "  }\n"
						+ "]");
		List<QueryToken> queryTokens = tokenInputService.convertToQueryTokens(userInputTokens);

		assertEquals(3, queryTokens.size());
		QueryToken qt;
		qt = queryTokens.get(0);
		assertEquals(1, qt.getConceptList().size());
		assertEquals("Concept 42", qt.getOriginalValue());
		assertFalse(qt.isFreetext());
//		assertEquals(QueryToken.Category.ALPHANUM, qt.getType());
        assertEquals(qt.getConceptList().size(), 1);
        assertEquals(qt.getConceptList().get(0).getId(), "tid42");
        assertEquals(qt.getConceptList().get(0).getPreferredName(), "Concept 42");

		qt = queryTokens.get(1);
		assertEquals(0, qt.getConceptList().size());
		assertEquals("AND", qt.getOriginalValue());
		assertFalse(qt.isFreetext());
//		assertEquals( qt.getType(), QueryToken.Category.AND);
		
		qt = queryTokens.get(2);
		assertEquals(0, qt.getConceptList().size());
		assertEquals("this thing regulates other", qt.getOriginalValue());
		assertTrue(qt.isFreetext());
//		assertEquals(QueryToken.Category.ALPHANUM, qt.getType());
	}
	
	@Test
	public void testConvertToQueryTokens2() {
		// wildcard term and keyword token
		JSONArray userInputTokens =
				new JSONArray(" [\n" + "  {\n"
						+ "    \"tokentype\" : WILDCARD,\n"
						+ "    \"name\" : \"Any term\",\n"
						+ "    \"facetid\" : \"fid-2\",\n"
						+ "    \"conceptid\" : \"ctid0\"\n"
						+ "  },\n"
						+ "  {\n"
						+ "    \"tokentype\" : KEYWORD,\n"
						+ "    \"name\" : \"keyword\",\n"
						+ "    \"facetid\" : \"fid-1\",\n"
						+ "    \"conceptid\" : \"keyword\"\n"
						+ "  },\n"
					+ "]");
		List<QueryToken> queryTokens = tokenInputService.convertToQueryTokens(userInputTokens);

		assertEquals(2, queryTokens.size());
		QueryToken qt;
		qt = queryTokens.get(0);
		assertEquals(qt.getConceptList().size(), 1);
		assertEquals("Any term", qt.getOriginalValue());
		assertFalse(qt.isFreetext());
//		assertEquals(QueryToken.Category.ALPHANUM, qt.getType());
		
		qt = queryTokens.get(1);
		assertEquals(qt.getConceptList().size(), 0);
		assertEquals("keyword", qt.getOriginalValue());
		assertFalse(qt.isFreetext());
//		assertEquals(QueryToken.Category.ALPHANUM, qt.getType());
	}
	
	@Test
	public void testConvertToQueryTokens3() {
		// event
		JSONArray userInputTokens =
				new JSONArray(" [\n" + "  {\n"
						+ "    \"tokentype\" : CONCEPT,\n"
						+ "    \"name\" : \"Concept 42\",\n"
						+ "    \"facetid\" : \"fid0\",\n"
						+ "    \"conceptid\" : \"tid42\"\n"
						+ "  },\n"
						+ "  {\n"
						+ "    \"tokentype\" : CONCEPT,\n"
						+ "    \"name\" : \"Concept 43\",\n"
						+ "    \"facetid\" : \"fid0\",\n"
						+ "    \"conceptid\" : \"tid43\"\n"
						+ "  }\n"
						+ "]");
		List<QueryToken> queryTokens = tokenInputService.convertToQueryTokens(userInputTokens);

		assertEquals(2, queryTokens.size());
		QueryToken qt;
		qt = queryTokens.get(0);
		assertEquals( qt.getConceptList().size(), 1);
		assertEquals( qt.getOriginalValue(), "Concept 42");
		assertFalse(qt.isFreetext());
//		assertEquals(QueryToken.Category.ALPHANUM, qt.getType());

		qt = queryTokens.get(1);
		assertEquals(qt.getConceptList().size(), 1);
		assertEquals( qt.getOriginalValue(), "Concept 43");
		assertFalse(qt.isFreetext());
//		assertEquals(QueryToken.Category.ALPHANUM, qt.getType());
	}

    @Test
    public void testConceptPhrase() {
        // event
        JSONArray userInputTokens =
                new JSONArray("[\n" +
                        "  {\n" +
                        "    \"tokentype\": \"CONCEPT_PHRASE\",\n" +
                        "    \"tokens\": [\n" +
                        "      {\n" +
                        "        \"tokentype\": \"CONCEPT\",\n" +
                        "        \"name\": \"Concept 42\",\n" +
                        "        \"facetid\": \"fid0\",\n" +
                        "        \"conceptid\": \"tid42\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"tokentype\": \"CONCEPT\",\n" +
                        "        \"name\": \"Concept 43\",\n" +
                        "        \"facetid\": \"fid0\",\n" +
                        "        \"conceptid\": \"tid43\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n" +
                        "]");
        System.out.println(userInputTokens);
        List<QueryToken> queryTokens = tokenInputService.convertToQueryTokens(userInputTokens);
        assertEquals(2, queryTokens.size());
        QueryToken qt;
        qt = queryTokens.get(0);
        assertEquals( qt.getConceptList().size(), 1);
        assertEquals( qt.getOriginalValue(), "Concept 42");
        assertFalse(qt.isFreetext());
//		assertEquals(QueryToken.Category.ALPHANUM, qt.getType());

        qt = queryTokens.get(1);
        assertEquals(qt.getConceptList().size(), 1);
        assertEquals( qt.getOriginalValue(), "Concept 43");
        assertFalse(qt.isFreetext());
//		assertEquals(QueryToken.Category.ALPHANUM, qt.getType());
    }
}
