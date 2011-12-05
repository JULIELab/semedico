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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java_cup.runtime.Symbol;

import org.apache.tapestry5.internal.antlr.PropertyExpressionParser.expression_return;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.dict.Dictionary;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.MapDictionary;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import de.julielab.Parsing.QueryTokenizer;
import de.julielab.lingpipe.DictionaryReaderService;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.QueryToken;
import de.julielab.semedico.core.services.FacetService;
import de.julielab.semedico.core.services.IStopWordService;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.core.services.SemedicoCoreModule;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.TermService;

public class QueryDisambiguationServiceTest {

	public static final String DICTIONARY_FILE_PATH = "src/test/resources/test.dic";
	public static final String TERM_INDEX_FILE_PATH = "src/test/resources/testIndex";
	public static final String STOPWORD_FILE_PATH = "src/test/resources/test_stopwords.txt";
	public static final String QUERY = "term1 term2 term3 term4 term5";
	public static final String KEYWORD_QUERY = "\"Cell Adhesion\" und Il-2 TnFa";
	public static final String JSON_TERMS = "[{id=\"TERM1\"; begin=0; end=5; value=\"term1\"},{id=\"TERM3\"; begin=12; end=17; value=\"term3\"}]";

	private QueryDisambiguationService queryDisambiguationService;
	private FacetTerm term1;
	private FacetTerm term2;
	private FacetTerm term3;
	private FacetTerm term4;
	private FacetTerm term5;
	private FacetTerm term6;
	private FacetTerm term7;
	private List<String> term1Occurences;
	private List<String> term2Occurences;
	private List<String> term3Occurences;
	private Collection<FacetTerm> terms;
	private String[] stopWords;

	private static Logger logger = LoggerFactory
			.getLogger(QueryDisambiguationServiceTest.class);
	private ITermService termServiceMock = prepareMockTermService();
	
	private IStopWordService stopWordServiceMock = EasyMock
			.createMock(IStopWordService.class);
	private Chunker chunker = prepareMockChunker();


	@Before
	public void setUp() throws Exception {
		term1 = new FacetTerm("TERM1", "name");
		term2 = new FacetTerm("TERM2", "name");
		term3 = new FacetTerm("TERM3", "name");
		term4 = new FacetTerm("TERM4", "name");
		term5 = new FacetTerm("TERM5", "name");
		term6 = new FacetTerm("TERM6", "name");
		term7 = new FacetTerm("TERM7", "name");

		term1Occurences = new ArrayList<String>();
		term1Occurences.add("term1 phrase1");
		term1Occurences.add("term1 phrase2");
		term1Occurences.add("term1 phrase3");
		term1Occurences.add("und");

		term2Occurences = new ArrayList<String>();
		term2Occurences.add("term2 phrase1");
		term2Occurences.add("term2 phrase2");
		term2Occurences.add("term2 phrase3");
		term2Occurences.add("und");

		term3Occurences = new ArrayList<String>();
		term3Occurences.add("term3 phrase1");
		term3Occurences.add("term3 phrase2");
		term3Occurences.add("term3 phrase3");
		term3Occurences.add("und");

		terms = new ArrayList<FacetTerm>();
		terms.add(term1);
		terms.add(term2);
		terms.add(term3);

		stopWords = new String[]{"na", "und", "nu"};
		expect(stopWordServiceMock.getAsArray()).andReturn(stopWords);
		replay(stopWordServiceMock);

		queryDisambiguationService = new QueryDisambiguationService(logger,
				stopWordServiceMock, termServiceMock, chunker);
	}

	private ITermService prepareMockTermService() {
		FacetTerm term = new FacetTerm("TERM", "name");
		
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

	private Chunker prepareMockChunker() {
		DictionaryReaderService mockDRS = EasyMock.createMock(DictionaryReaderService.class);
		MapDictionary<String> dic = new MapDictionary<String>();
		dic.addEntry(new DictionaryEntry<String>("foo bar", "mockDicPhrase"));
		expect(mockDRS.getMapDictionary()).andReturn(dic);
		replay(mockDRS);
		return chunker = SemedicoCoreModule.buildDictionaryChunker(mockDRS);
	}

	@After
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
	public void testDisambiguateQuery() throws IOException {
		String query = "foo bar";
		assertTrue(queryDisambiguationService.disambiguateQuery(query, "id").keySet().contains("foo bar"));
	}
	
	@Test
	public void testDisambiguateSymbols() throws IOException {
		int text = QueryTokenizer.ALPHANUM;
		Symbol[] symbols = { new Symbol(text, "foo"), new Symbol(text, "bar")};
		assertTrue(queryDisambiguationService.disambiguateSymbols("id", symbols).keySet().contains("foo bar"));
	}

	/*
	@Test
	public void testMapDictionaryMatches() throws Exception {
		Chunker chunker = createMock(Chunker.class);
		ChunkingImpl chunking = new ChunkingImpl(QUERY);
		chunking.add(ChunkFactory.createChunk(0, 5, "TERM1"));
		chunking.add(ChunkFactory.createChunk(12, 17, "TERM3", 1.0));
		chunking.add(ChunkFactory.createChunk(12, 17, "TERM6", 0.9));
		chunking.add(ChunkFactory.createChunk(12, 17, "TERM7", 0.7));
		chunking.add(ChunkFactory.createChunk(18, 23, "TERM5"));

		expect(chunker.chunk(QUERY)).andReturn(chunking);
		replay(chunker);
		queryDisambiguationService.setMaxAmbigueTerms(2);
		queryDisambiguationService.setChunker(chunker);
		ITermService termService = EasyMock.createMock(ITermService.class);
		expect(termService.getTermWithInternalIdentifier("TERM1")).andReturn(
				term1);
		expect(termService.getTermWithInternalIdentifier("TERM3")).andReturn(
				term3);
		expect(termService.getTermWithInternalIdentifier("TERM6")).andReturn(
				term6);
		expect(termService.getTermWithInternalIdentifier("TERM7")).andReturn(
				term7);
		replay(termService);
		queryDisambiguationService.setTermService(termService);

		List<QueryToken> queryTokens = new ArrayList<QueryToken>();
		QueryToken term4Token = new QueryToken(18, 23, "term4");
		queryTokens.add(term4Token);

		queryDisambiguationService.mapDictionaryMatches(QUERY, queryTokens,
				null);
		verify(chunker);
		verify(termService);
		Collections.sort(queryTokens, Ordering.compound(Lists.newArrayList(
				QueryDisambiguationService.BEGINN_OFFSET_COMPARATOR,
				QueryDisambiguationService.SCORE_COMPARATOR)));

		assertEquals(4, queryTokens.size());
		Iterator<QueryToken> tokenIterator = queryTokens.iterator();

		QueryToken token = tokenIterator.next();
		assertEquals("TERM1", token.getTerm().getInternalIdentifier());
		assertEquals("term1", token.getValue());
		assertEquals(0, token.getBeginOffset());
		assertEquals(5, token.getEndOffset());

		token = tokenIterator.next();
		assertEquals("TERM3", token.getTerm().getInternalIdentifier());
		assertEquals("term3", token.getValue());
		assertEquals(12, token.getBeginOffset());
		assertEquals(17, token.getEndOffset());

		token = tokenIterator.next();
		assertEquals("TERM6", token.getTerm().getInternalIdentifier());
		assertEquals("term3", token.getValue());
		assertEquals(12, token.getBeginOffset());
		assertEquals(17, token.getEndOffset());

		token = tokenIterator.next();
		assertEquals(term4Token, token);
	}

	// @Test
	// public void testMapJSONTerms(){
	// ITermService termService = createMock(ITermService.class);
	// expect(termService.getTermWithInternalIdentifier("TERM1",
	// null)).andReturn(term1);
	// expect(termService.getTermWithInternalIdentifier("TERM3",
	// null)).andReturn(term3);
	// replay(termService);
	// queryDisambiguationService.setTermService(termService);
	//
	// List<QueryToken> queryTokens = new ArrayList<QueryToken>();
	//
	// queryDisambiguationService.mapJSONTerms(QUERY, JSON_TERMS, queryTokens);
	//
	// verify(termService);
	//
	// assertEquals(2, queryTokens.size());
	// Iterator<QueryToken> tokenIterator = queryTokens.iterator();
	//
	// QueryToken token = tokenIterator.next();
	// assertEquals("TERM1", token.getTerm().getInternalIdentifier());
	// assertEquals("term1", token.getValue());
	// assertEquals(0, token.getBeginOffset());
	// assertEquals(5, token.getEndOffset());
	//
	// token = tokenIterator.next();
	// assertEquals("TERM3", token.getTerm().getInternalIdentifier());
	// assertEquals("term3", token.getValue());
	// assertEquals(12, token.getBeginOffset());
	// assertEquals(17, token.getEndOffset());
	// }

	@Test
	public void testMapKeywords() throws IOException {
		List<QueryToken> queryTokens = new ArrayList<QueryToken>();
		QueryToken tnfa = new QueryToken(25, 29, "tnfa");
		queryTokens.add(tnfa);

		queryDisambiguationService
				.mapKeywords(KEYWORD_QUERY, queryTokens, null);
		Collections.sort(queryTokens, Ordering.compound(Lists.newArrayList(
				QueryDisambiguationService.BEGINN_OFFSET_COMPARATOR,
				QueryDisambiguationService.SCORE_COMPARATOR)));
		assertEquals(3, queryTokens.size());
		Iterator<QueryToken> tokenIterator = queryTokens.iterator();

		QueryToken token = tokenIterator.next();
		FacetTerm term = token.getTerm();
		assertEquals("cell adhes", term.getInternalIdentifier());
		assertEquals(FacetService.KEYWORD_FACET, term.getFirstFacet());
		assertEquals("text", term.getIndexNames().iterator().next());
		assertEquals("Cell Adhesion", token.getTerm().getName());
		assertEquals("cell adhes", token.getValue());
		assertEquals(0, token.getBeginOffset());
		assertEquals(15, token.getEndOffset());

		token = tokenIterator.next();
		assertEquals("il-2", token.getTerm().getInternalIdentifier());
		assertEquals("Il-2", token.getTerm().getName());
		assertEquals("il-2", token.getValue());
		assertEquals(20, token.getBeginOffset());
		assertEquals(24, token.getEndOffset());

		token = tokenIterator.next();
		assertEquals(tnfa, token);
	}

	public Collection<String> readFile(String filePath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		List<String> lines = new ArrayList<String>();

		String line = reader.readLine();
		while (line != null) {
			lines.add(line);
			line = reader.readLine();
		}
		reader.close();

		return lines;
	}

	public void testCase() {

	}
	*/
}
