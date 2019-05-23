package de.julielab.semedico.core.services.query;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Multimap;

import de.julielab.scicopia.core.parsing.DisambiguatingRangeChunker;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facetterms.SyncFacetTerm;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.services.DictionaryReaderService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

public class TermRecognitionServiceTest {

	static Multimap<String, String> dict;
	static TermRecognitionService service;
	static ITermService termService;

	@BeforeClass()
	public static void setup() throws IOException {
		DictionaryReaderService reader = new DictionaryReaderService();
		dict = reader.readDictionary("src/test/resources/recognize.dic");
		DisambiguatingRangeChunker chunker = new DisambiguatingRangeChunker(dict);
		termService = mock(ITermService.class);
		service = new TermRecognitionService(chunker, termService);
	}

	@Test
	public void testKeyword() throws Exception {
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken token = new QueryToken("Trochiten");
		token.setType(QueryToken.Category.ALPHA);
		tokens.add(token);
		tokens = service.recognizeTerms(tokens);
		assertEquals(ITokenInputService.TokenType.KEYWORD, tokens.get(0).getInputTokenType());		
	}
	
	@Test
	public void testConcept() throws Exception {
		Concept concept = new SyncFacetTerm();
		concept.setId("tid3");
		concept.setPreferredName("Bone and Bones");
		concept.setSynonyms(Arrays.asList("Condyle", "Bones", "Bone"));
		Facet facet = new Facet("fid0");
		concept.setFacets(Arrays.asList(facet));
		when(termService.getTermSynchronously("tid3")).thenReturn(concept);
		
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken token = new QueryToken("Bone");
		token.setType(QueryToken.Category.ALPHA);
		tokens.add(token);
		tokens = service.recognizeTerms(tokens);
		assertEquals(ITokenInputService.TokenType.CONCEPT, tokens.get(0).getInputTokenType());		
	}

	@Test
	public void testAmbiguousConcept() throws Exception {
		Concept concept1 = new SyncFacetTerm();
		concept1.setId("tid11325");
		concept1.setPreferredName("Mice");
		concept1.setSynonyms(Arrays.asList("Mus", "Mus musculus", "Mice", "Mouse","Swiss Mice"));
		Facet facet1 = new Facet("fid1");
		concept1.setFacets(Arrays.asList(facet1));
		when(termService.getTermSynchronously("tid11325")).thenReturn(concept1);
		
		Concept concept2 = new SyncFacetTerm();
		concept2.setId("tid276438");
		concept2.setPreferredName("Mus musculus");
		concept2.setSynonyms(Arrays.asList("Mice", "Mouse"));
		Facet facet2 = new Facet("fid1");
		concept2.setFacets(Arrays.asList(facet2));
		when(termService.getTermSynchronously("tid276438")).thenReturn(concept2);
		
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken token = new QueryToken("Mouse");
		token.setType(QueryToken.Category.ALPHA);
		tokens.add(token);
		tokens = service.recognizeTerms(tokens);
		assertEquals(1, tokens.size());
		assertEquals(ITokenInputService.TokenType.AMBIGUOUS_CONCEPT, tokens.get(0).getInputTokenType());		
	}

	@Ignore
	@Test
	public void testBestOccurrence() throws Exception {
		List<QueryToken> tokens = new ArrayList<>();
		Method m = TermRecognitionService.class.getDeclaredMethod("recognizeWithDictionary", String.class,
				Collection.class, int.class);
		m.setAccessible(true);
		m.invoke(service, "frap", tokens, 0);
		assertEquals("FRAP", tokens.get(0).getMatchedSynonym());
	}

	@Ignore
	@Test
	public void testConceptRecognition() throws Exception {
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken qt = new QueryToken(0, 4);
		qt.setOriginalValue("frap");
		qt.setType(QueryToken.Category.ALPHANUM);
		qt.setInputTokenType(TokenType.FREETEXT);
		tokens.add(qt);
		List<QueryToken> recognizeTerms = service.recognizeTerms(tokens);
		assertEquals("FRAP", recognizeTerms.get(0).getMatchedSynonym());
	}

	@Ignore
	@Test
	public void testPhrase() throws IOException {
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken qt = new QueryToken(0, 4);
		qt.setOriginalValue("mtor");
//		qt.setType(QueryTokenizerImpl.PHRASE);
		qt.setInputTokenType(TokenType.FREETEXT);
		tokens.add(qt);
		List<QueryToken> recognizeTerms = service.recognizeTerms(tokens);
		assertEquals(1, recognizeTerms.size());
		QueryToken phraseToken = recognizeTerms.get(0);
		assertEquals("Wrong input token type", TokenType.KEYWORD, phraseToken.getInputTokenType());
	}
	
	@Ignore
	@Test
	public void testDash() throws IOException {
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken qt = new QueryToken(0, 11);
		qt.setOriginalValue("water-level");
		qt.setType(QueryToken.Category.DASH);
		qt.setInputTokenType(TokenType.FREETEXT);
		tokens.add(qt);
		List<QueryToken> recognizeTerms = service.recognizeTerms(tokens);
		System.out.println(recognizeTerms);
		assertEquals(1, recognizeTerms.size());
		QueryToken phraseToken = recognizeTerms.get(0);
		assertEquals("Wrong input token type", TokenType.KEYWORD, phraseToken.getInputTokenType());
	}
}
