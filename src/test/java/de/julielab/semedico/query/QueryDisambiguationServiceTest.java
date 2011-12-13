/** 
 * QueryDisambiguationServiceTest.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: landefeld
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: 28.07.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.query;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import java_cup.runtime.Symbol;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliasi.chunk.Chunker;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.MapDictionary;

import de.julielab.lingpipe.DictionaryReaderService;
import de.julielab.parsing.CombiningLexer;
import de.julielab.parsing.QueryTokenizer;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.services.IStopWordService;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.core.services.SemedicoCoreModule;
import de.julielab.semedico.core.services.StopWordService;

/**
 * Bad state, most tests weren't working (according to faessler they probably never did).
 * I added tests for disambiguateQuery() and disambiguateSymbols().
 * Deleted most of landefelds code, use svn if you need to.
 * 
 * @author hellrich
 *
 */
public class QueryDisambiguationServiceTest {

	public static final String DICTIONARY_FILE_PATH = "src/test/resources/test.dic";
	public static final String TERM_INDEX_FILE_PATH = "src/test/resources/testIndex";
	public static final String STOPWORD_FILE_PATH = "src/test/resources/test_stopwords.txt";
	public static final String QUERY = "term1 term2 term3 term4 term5";
	public static final String KEYWORD_QUERY = "\"Cell Adhesion\" und Il-2 TnFa";
	public static final String JSON_TERMS = "[{id=\"TERM1\"; begin=0; end=5; value=\"term1\"},{id=\"TERM3\"; begin=12; end=17; value=\"term3\"}]";

	private QueryDisambiguationService queryDisambiguationService;
	private String[] stopWords;

	private static Logger logger = LoggerFactory
			.getLogger(QueryDisambiguationServiceTest.class);
	private ITermService termServiceMock = prepareMockTermService();
	
	private IStopWordService stopWordServiceMock = EasyMock
			.createMock(IStopWordService.class);
	private static Chunker chunker = prepareMockChunker();


	@Before
	public void setUp() throws Exception {	
		stopWords = new String[]{"na", "und", "nu"};
		expect(stopWordServiceMock.getAsArray()).andReturn(stopWords);
		replay(stopWordServiceMock);

		queryDisambiguationService = new QueryDisambiguationService(logger,
				stopWordServiceMock, termServiceMock, chunker);
	}

	/**
	 * ?
	 */
	private static ITermService prepareMockTermService() {
		FacetTerm term = new FacetTerm("mapped stuff", "name");
		//stuff below can't simply be deleted
		
		Facet facet = EasyMock.createMock(Facet.class);
		expect(facet.getId()).andReturn(1);
		expect(facet.getId()).andReturn(2);
		expect(facet.getId()).andReturn(3);
		expect(facet.getName()).andReturn("name");
		expect(facet.getName()).andReturn("name");
		expect(facet.getName()).andReturn("name");
		replay(facet);
		term.addFacet(facet);
		ITermService mock = EasyMock.createMock(ITermService.class);
		expect(mock.getTermWithInternalIdentifier("id")).andReturn(term);
		expect(mock.getNode("mockDicPhrase")).andReturn(term);
		expect(mock.getNode("mockDicPhrase")).andReturn(term);
		replay(mock);
		return mock;
	}
	
	/**
	 * ?
	 */
	private static Chunker prepareMockChunker() {
		DictionaryReaderService mockDRS = EasyMock.createMock(DictionaryReaderService.class);
		MapDictionary<String> dic = new MapDictionary<String>();
		dic.addEntry(new DictionaryEntry<String>("foo bar", "mockDicPhrase"));
		expect(mockDRS.getMapDictionary()).andReturn(dic);
		replay(mockDRS);
		return chunker = SemedicoCoreModule.buildDictionaryChunker(mockDRS);
	}

	@After
	/**
	 * ?
	 */
	public void tearDown() throws Exception {

		File dictionary = new File(DICTIONARY_FILE_PATH);
		if (dictionary.exists())
			dictionary.delete();

		File indexDirectory = new File(TERM_INDEX_FILE_PATH);
		if (indexDirectory.exists()) {
			for (File file : indexDirectory.listFiles())
				file.delete();

			indexDirectory.delete();
		}
	}
	
	@Test
	/**
	 * Tests if a simple query String was recognized as a token (mocked dictionary)
	 */
	public void testDisambiguateQuery() throws IOException {
		String query = "foo bar";
		assertTrue(queryDisambiguationService.disambiguateQuery(query, null).keySet().contains("foo bar"));
	}
	
	@Test
	/**
	 * Tests if two Symbols were merged into one (mocked dictionary)
	 */
	public void testDisambiguateSymbols() throws IOException {
		int text = QueryTokenizer.ALPHANUM;
		Symbol[] symbols = { new Symbol(text, "foo"), new Symbol(text, "bar")};
		List<Symbol> combined = (List<Symbol>) queryDisambiguationService.disambiguateSymbols(Arrays.asList(symbols));
		assertEquals(1, combined.size());
		assertEquals(QueryTokenizer.PHRASE, combined.get(0).sym);
		String[] payload = ((String[])combined.get(0).value);
		assertEquals("foo bar", payload[CombiningLexer.TEXT]);
		assertEquals("mapped stuff", payload[CombiningLexer.MAPPED_TEXT]);
	}
	
	@Test
	/**
	 * Tests if two Symbols were merged into one (mocked dictionary) even if a phrase
	 * is part of the input
	 */
	public void testDisambiguateComplexSymbols() throws IOException {
		int text = QueryTokenizer.ALPHANUM;
		int phrase = QueryTokenizer.PHRASE;
		Symbol[] symbols = {new Symbol(phrase, "test phrase"), new Symbol(text, "foo"), new Symbol(text, "bar")};
		List<Symbol> combined = (List<Symbol>) queryDisambiguationService.disambiguateSymbols(Arrays.asList(symbols));
		assertEquals(2, combined.size());
		assertEquals(phrase, combined.get(0).sym);
		assertEquals("test phrase", ((String)combined.get(0).value));
		assertEquals(phrase, combined.get(1).sym);
		String[] payload = ((String[])combined.get(1).value);
		assertEquals("foo bar", payload[QueryDisambiguationService.TEXT]);
		assertEquals("mapped stuff", payload[QueryDisambiguationService.MAPPED_TEXT]);
	}

	public static QueryDisambiguationService getMockService() throws IOException {
		String[] stopWords = new String[]{"na", "und", "nu"};
		Logger logger = LoggerFactory
		.getLogger(QueryDisambiguationServiceTest.class);
ITermService termServiceMock = prepareMockTermService();

IStopWordService stopWordServiceMock = EasyMock
		.createMock(IStopWordService.class);
Chunker chunker = prepareMockChunker();
		expect(stopWordServiceMock.getAsArray()).andReturn(stopWords);
		replay(stopWordServiceMock);

		return new QueryDisambiguationService(logger,
				stopWordServiceMock, termServiceMock, chunker);
	}

}
