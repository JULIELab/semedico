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

package de.julielab.stemnet.query;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.julielab.stemnet.core.FacetTerm;
import de.julielab.stemnet.core.services.ITermService;

public class QueryIndexBuilderServiceTest  {
	
	public static final String DICTIONARY_FILE_PATH = "src/test/resources/test.dic";
	public static final String TERM_INDEX_FILE_PATH = "src/test/resources/testIndex";
	public static final String STOPWORD_FILE_PATH = "src/test/resources/test_stopwords.txt";
	public static final String QUERY = "term1 term2 term3 term4 term5";
	public static final String KEYWORD_QUERY = "\"Cell Adhesion\" und Il-2 TnFa";
	public static final String JSON_TERMS = "[{id=\"TERM1\"; begin=0; end=5; value=\"term1\"},{id=\"TERM3\"; begin=12; end=17; value=\"term3\"}]";	

	private QueryIndexBuilderService queryIndexBuilderService;
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
	
	@Before
	public void setUp() throws Exception {
		term1 = new FacetTerm();
		term1.setInternalIdentifier("TERM1");
		term2 = new FacetTerm();
		term2.setInternalIdentifier("TERM2");
		term3 = new FacetTerm();
		term3.setInternalIdentifier("TERM3");
		term4 = new FacetTerm();
		term4.setInternalIdentifier("TERM4");
		term5 = new FacetTerm();
		term5.setInternalIdentifier("TERM5");
		term6 = new FacetTerm();
		term6.setInternalIdentifier("TERM6");
		term7 = new FacetTerm();
		term7.setInternalIdentifier("TERM7");
		
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
		
		queryIndexBuilderService = new QueryIndexBuilderService();

	}

	@After
	public void tearDown() throws Exception {
		
		File indexDirectory = new File(TERM_INDEX_FILE_PATH);
		if( indexDirectory.exists() ){
			for( File file: indexDirectory.listFiles() )
				file.delete();

			indexDirectory.delete();
		}
	}
	
//	@Test
//	public void testCreateTermIndex() throws Exception{
//		ITermService termService = EasyMock.createMock(ITermService.class);
//		expect(termService.readOccurrencesForTerm(term1)).andReturn(term1Occurences);
//		expect(termService.readOccurrencesForTerm(term2)).andReturn(term2Occurences);
//		expect(termService.readOccurrencesForTerm(term3)).andReturn(term3Occurences);
//		replay(termService);
//		queryIndexBuilderService.setTermService(termService);
//		queryIndexBuilderService.createTermIndex(terms, TERM_INDEX_FILE_PATH);
//		verify(termService);
//		
//		IndexSearcher searcher = new IndexSearcher(IndexReader.open(TERM_INDEX_FILE_PATH));
//		QueryParser queryParser = new QueryParser(QueryDisambiguationService.PHRASES_INDEX_FIELD_NAME, new WhitespaceAnalyzer());
//
//		Hits hits = searcher.search(queryParser.parse("term1"));		
//		assertEquals(1, hits.length());
//		assertEquals("TERM1", hits.doc(0).get(QueryDisambiguationService.ID_INDEX_FIELD_NAME));
//
//		hits = searcher.search(queryParser.parse("term2"));		
//		assertEquals(1, hits.length());
//		assertEquals("TERM2", hits.doc(0).get(QueryDisambiguationService.ID_INDEX_FIELD_NAME));
//
//		hits = searcher.search(queryParser.parse("term3"));		
//		assertEquals(1, hits.length());
//		assertEquals("TERM3", hits.doc(0).get(QueryDisambiguationService.ID_INDEX_FIELD_NAME));
//
//		hits = searcher.search(queryParser.parse("phrase1"));		
//		assertEquals(3, hits.length());
//		assertEquals("TERM1", hits.doc(0).get(QueryDisambiguationService.ID_INDEX_FIELD_NAME));	
//		assertEquals("TERM2", hits.doc(1).get(QueryDisambiguationService.ID_INDEX_FIELD_NAME));
//		assertEquals("TERM3", hits.doc(2).get(QueryDisambiguationService.ID_INDEX_FIELD_NAME));		
//	}

}

