package de.julielab.semedico.core.services.query;

import de.julielab.java.utilities.spanutils.OffsetMap;
import de.julielab.scicopia.core.parsing.DisambiguatingRangeChunker;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.CoreConcept;
import de.julielab.semedico.core.concepts.TopicTag;
import de.julielab.semedico.core.search.query.QueryAnalysis;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.ArraySymbolProvider;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.internal.services.SymbolSourceImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.julielab.semedico.core.services.interfaces.IConceptService.CORE_TERM_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.AssertJUnit.assertEquals;
public class ConceptRecognitionServiceTest {

	private static Registry registry;

	@BeforeClass
	public static void setup() {
		registry = TestUtils.createTestRegistry();
	}

	@AfterClass
	public static void shutdown() {
		registry.shutdown();
	}

	@Test(groups = "integration")
	public void testBestOccurrence() throws Exception {
	    // This test requires a concept with a synonym named "FRAP" in the test database.
        ConceptRecognitionService service = new ConceptRecognitionService(registry.getService(DisambiguatingRangeChunker.class),
				registry.getService(IConceptService.class), new SymbolSourceImpl(Collections.singletonList(new ArraySymbolProvider(SemedicoSymbolConstants.QUERY_ANALYSIS, QueryAnalysis.CONCEPTS.name()))));
		OffsetMap<QueryToken> tokens = new OffsetMap<>();
		Method m = ConceptRecognitionService.class.getDeclaredMethod("recognizeWithDictionary", String.class,
				OffsetMap.class, int.class);
		m.setAccessible(true);
		m.invoke(service, "frap", tokens, 0);
		assertEquals("FRAP", tokens.firstEntry().getValue().getMatchedSynonym());
	}

	@Test(groups = "integration")
	public void testConceptRecognition() throws Exception {
		IConceptRecognitionService service = registry.getService(IConceptRecognitionService.class);
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken qt = new QueryToken(0, 4);
		qt.setOriginalValue("frap");
		qt.setLexerType(QueryToken.Category.ALPHA);
		qt.setInputTokenType(TokenType.FREETEXT);
		tokens.add(qt);
		List<QueryToken> recognizeTerms = service.recognizeTerms(tokens);
		assertEquals("FRAP", recognizeTerms.get(0).getMatchedSynonym());
	}

	@Test
	public void testPhrase() throws IOException {
		IConceptRecognitionService service = registry.getService(IConceptRecognitionService.class);
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken qt = new QueryToken(0, 4);
		qt.setOriginalValue("mtor");
		qt.setLexerType(QueryToken.Category.KW_PHRASE);
		qt.setInputTokenType(TokenType.FREETEXT);
		tokens.add(qt);
		List<QueryToken> recognizeTerms = service.recognizeTerms(tokens);
		assertEquals(1, recognizeTerms.size());
		QueryToken phraseToken = recognizeTerms.get(0);
		assertEquals("Wrong input token type", TokenType.KEYWORD, phraseToken.getInputTokenType());
	}
	
	@Test
	public void testDash() throws IOException {
		IConceptRecognitionService service = registry.getService(IConceptRecognitionService.class);
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken qt = new QueryToken(0, 11);
		qt.setOriginalValue("water-level");
		qt.setLexerType(QueryToken.Category.DASH);
		qt.setInputTokenType(TokenType.FREETEXT);
		tokens.add(qt);
		List<QueryToken> recognizedConceptTokens = service.recognizeTerms(tokens);
		assertEquals(1, recognizedConceptTokens.size());
		QueryToken phraseToken = recognizedConceptTokens.get(0);
		assertEquals("Wrong input token type", TokenType.KEYWORD, phraseToken.getInputTokenType());
	}

	@Test
	public void testTopicTag() throws IOException {
		IConceptRecognitionService service = registry.getService(IConceptRecognitionService.class);
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken qt = new QueryToken(0, 11);
		qt.setOriginalValue("#medicine");
		qt.setLexerType(QueryToken.Category.HASHTAG);
		qt.setInputTokenType(TokenType.TOPIC_TAG);
		tokens.add(qt);
		List<QueryToken> recognizedConceptTokens = service.recognizeTerms(tokens);
		assertEquals(1, recognizedConceptTokens.size());
		QueryToken topicToken = recognizedConceptTokens.get(0);
        assertEquals(1, topicToken.getConceptList().size());
        assertThat(topicToken.getConceptList().get(0).getClass()).isEqualTo(TopicTag.class);
        assertThat(topicToken.getConceptList().get(0).getId()).isEqualTo("#medicine");
        final TopicTag tag = (TopicTag) topicToken.getConceptList().get(0);
        assertThat(tag.getWord()).isEqualTo("medicine");
    }

	@Test
	public void testWildcard() {
		IConceptRecognitionService service = registry.getService(IConceptRecognitionService.class);
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken qt = new QueryToken(0, 1);
		qt.setOriginalValue("*");
		qt.setLexerType(QueryToken.Category.WILDCARD);
		qt.setInputTokenType(TokenType.WILDCARD);
		tokens.add(qt);
		List<QueryToken> recognizedConceptTokens = service.recognizeTerms(tokens);
		assertEquals(1, recognizedConceptTokens.size());
		QueryToken topicToken = recognizedConceptTokens.get(0);
		assertEquals(1, topicToken.getConceptList().size());
		assertThat(topicToken.getConceptList().get(0).getClass()).isEqualTo(CoreConcept.class);
		assertThat(topicToken.getConceptList().get(0).getId()).isEqualTo(CORE_TERM_PREFIX + 0);
		final CoreConcept concept = (CoreConcept) topicToken.getConceptList().get(0);
		assertThat(concept.getSynonyms()).contains("any");
	}

}
