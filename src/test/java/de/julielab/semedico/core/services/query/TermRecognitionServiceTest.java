package de.julielab.semedico.core.services.query;

import static org.junit.Assert.assertEquals;

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
import de.julielab.semedico.core.services.interfaces.ITermRecognitionService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

public class TermRecognitionServiceTest {

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
		TermRecognitionService service = new TermRecognitionService(registry.getService(Chunker.class), registry.getService(ITermService.class));
		List<QueryToken> tokens = new ArrayList<>();
		Method m = TermRecognitionService.class.getDeclaredMethod("recognizeWithDictionary", String.class, Collection.class, int.class, long.class);
		m.setAccessible(true);
		m.invoke(service, "frap", tokens, 0, 0);
		assertEquals("FRAP", tokens.get(0).getMatchedSynonym());
	}
	
	@Test
	public void testConceptRecognition() throws Exception {
		ITermRecognitionService service = registry.getService(ITermRecognitionService.class);
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken qt = new QueryToken(0, 4);
		qt.setOriginalValue("frap");
		qt.setType(QueryTokenizerImpl.ALPHANUM);
		qt.setInputTokenType(TokenType.FREETEXT);
		tokens.add(qt);
		List<QueryToken> recognizeTerms = service.recognizeTerms(tokens, 0);
		assertEquals("FRAP", recognizeTerms.get(0).getMatchedSynonym());
	}
}
