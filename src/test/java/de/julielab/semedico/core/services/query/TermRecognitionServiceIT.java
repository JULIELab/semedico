package de.julielab.semedico.core.services.query;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aliasi.chunk.Chunker;

import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.services.ArraySymbolSource;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IConceptRecognitionService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

public class TermRecognitionServiceIT {

	private static Registry registry;

	@BeforeClass()
	public static void setup() {
		registry = TestUtils.createTestRegistry();
	}

	@AfterClass
	public static void shutdown() {
		registry.shutdown();
	}

	@Test
	public void testBestOccurrence() throws Exception {
		ConceptRecognitionService service = new ConceptRecognitionService(registry.getService(Chunker.class),
				registry.getService(ITermService.class), new ArraySymbolSource(SemedicoSymbolConstants.QUERY_CONCEPTS, "true"));
		List<QueryToken> tokens = new ArrayList<>();
		Method m = ConceptRecognitionService.class.getDeclaredMethod("recognizeWithDictionary", String.class,
				Collection.class, int.class, long.class);
		m.setAccessible(true);
		m.invoke(service, "frap", tokens, 0, 0);
		assertEquals("FRAP", tokens.get(0).getMatchedSynonym());
	}

	@Test
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

	@Test
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
	
	@Test
	public void testDash() throws IOException {
		IConceptRecognitionService service = registry.getService(IConceptRecognitionService.class);
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken qt = new QueryToken(0, 11);
		qt.setOriginalValue("water-level");
		qt.setType(QueryTokenizerImpl.DASH);
		qt.setInputTokenType(TokenType.FREETEXT);
		tokens.add(qt);
		List<QueryToken> recognizeTerms = service.recognizeTerms(tokens, 0);
		System.out.println(recognizeTerms);
		assertEquals(1, recognizeTerms.size());
		QueryToken phraseToken = recognizeTerms.get(0);
		assertEquals("Wrong input token type", TokenType.KEYWORD, phraseToken.getInputTokenType());
	}
}
