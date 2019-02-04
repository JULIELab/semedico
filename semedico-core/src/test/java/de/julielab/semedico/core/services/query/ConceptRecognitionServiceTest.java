package de.julielab.semedico.core.services.query;

import com.aliasi.chunk.Chunker;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.ConceptCreator;
import de.julielab.semedico.core.concepts.TopicTag;
import de.julielab.semedico.core.search.query.QueryAnalysis;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.*;
import de.julielab.semedico.core.services.interfaces.ICacheService;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.internal.services.SymbolSourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.assertj.core.api.Assertions.*;
public class ConceptRecognitionServiceTest {

	private static Registry registry;

	@BeforeClass(groups = {"neo4jtests"})
	public static void setup() {
		registry = TestUtils.createTestRegistry();
	}

	@AfterClass(groups = {"neo4jtests"})
	public static void shutdown() {
		registry.shutdown();
	}

	@Test(groups = {"neo4jtests"})
	public void testBestOccurrence() throws Exception {
	    // This test requires a concept with a synonym named "FRAP" in the test database.
        final ICacheService cacheService = registry.getService(ICacheService.class);
        final ConceptCreator conceptCreator = new ConceptCreator(new FacetNeo4jService(LoggerFactory.getLogger(ConceptCreator.class), true, true, Neo4jServiceTest.neo4jService));
        final ConceptNeo4jService conceptService = new ConceptNeo4jService(LoggerFactory.getLogger(ConceptNeo4jService.class), cacheService, Neo4jServiceTest.neo4jService, conceptCreator, new StringTermService(LoggerFactory.getLogger(StringTermService.class), null, null, null, null, null));
        ConceptRecognitionService service = new ConceptRecognitionService(registry.getService(Chunker.class),
				registry.getService(IConceptService.class), new SymbolSourceImpl(Collections.singletonList(new ArraySymbolProvider(SemedicoSymbolConstants.QUERY_ANALYSIS, QueryAnalysis.CONCEPTS.name()))));
		List<QueryToken> tokens = new ArrayList<>();
		Method m = ConceptRecognitionService.class.getDeclaredMethod("recognizeWithDictionary", String.class,
				Collection.class, int.class, long.class);
		m.setAccessible(true);
		m.invoke(service, "frap", tokens, 0, 0);
		assertEquals("FRAP", tokens.get(0).getMatchedSynonym());
	}

	@Test(groups = {"neo4jtests"})
	public void testConceptRecognition() throws Exception {
		IConceptRecognitionService service = registry.getService(IConceptRecognitionService.class);
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken qt = new QueryToken(0, 4);
		qt.setOriginalValue("frap");
		qt.setType(QueryTokenizerImpl.ALPHANUM);
		qt.setInputTokenType(TokenType.FREETEXT);
		tokens.add(qt);
		List<QueryToken> recognizeTerms = service.recognizeTerms(tokens, 0);
		assertEquals("FRAP", recognizeTerms.get(0).getMatchedSynonym());
	}

	@Test(groups = {"neo4jtests"})
	public void testPhrase() throws IOException {
		IConceptRecognitionService service = registry.getService(IConceptRecognitionService.class);
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken qt = new QueryToken(0, 4);
		qt.setOriginalValue("mtor");
		qt.setType(QueryTokenizerImpl.PHRASE);
		qt.setInputTokenType(TokenType.FREETEXT);
		tokens.add(qt);
		List<QueryToken> recognizeTerms = service.recognizeTerms(tokens, 0);
		assertEquals(1, recognizeTerms.size());
		QueryToken phraseToken = recognizeTerms.get(0);
		assertEquals("Wrong input token type", TokenType.KEYWORD, phraseToken.getInputTokenType());
	}
	
	@Test(groups = {"neo4jtests"})
	public void testDash() throws IOException {
		IConceptRecognitionService service = registry.getService(IConceptRecognitionService.class);
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken qt = new QueryToken(0, 11);
		qt.setOriginalValue("water-level");
		qt.setType(QueryTokenizerImpl.DASH);
		qt.setInputTokenType(TokenType.FREETEXT);
		tokens.add(qt);
		List<QueryToken> recognizedConceptTokens = service.recognizeTerms(tokens, 0);
		assertEquals(1, recognizedConceptTokens.size());
		QueryToken phraseToken = recognizedConceptTokens.get(0);
		assertEquals("Wrong input token type", TokenType.KEYWORD, phraseToken.getInputTokenType());
	}

	@Test(groups = {"neo4jtests"})
	public void testTopicTag() throws IOException {
		IConceptRecognitionService service = registry.getService(IConceptRecognitionService.class);
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken qt = new QueryToken(0, 11);
		qt.setOriginalValue("#medicine");
		qt.setType(QueryTokenizerImpl.HASHTAG);
		qt.setInputTokenType(TokenType.TOPIC_TAG);
		tokens.add(qt);
		List<QueryToken> recognizedConceptTokens = service.recognizeTerms(tokens, 0);
		assertEquals(1, recognizedConceptTokens.size());
		QueryToken topicToken = recognizedConceptTokens.get(0);
        assertEquals(1, topicToken.getConceptList().size());
        assertThat(topicToken.getConceptList().get(0).getClass()).isEqualTo(TopicTag.class);
        assertThat(topicToken.getConceptList().get(0).getId()).isEqualTo("#medicine");
	}
}
