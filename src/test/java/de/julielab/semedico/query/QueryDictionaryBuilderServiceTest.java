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
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.services.ITermService;

public class QueryDictionaryBuilderServiceTest  {
	
	public static final String DICTIONARY_FILE_PATH = "src/test/resources/test.dic";
	public static final String TERM_INDEX_FILE_PATH = "src/test/resources/testIndex";
	public static final String QUERY = "term1 term2 term3 term4 term5";
	public static final String KEYWORD_QUERY = "\"Cell Adhesion\" und Il-2 TnFa";
	public static final String JSON_TERMS = "[{id=\"TERM1\"; begin=0; end=5; value=\"term1\"},{id=\"TERM3\"; begin=12; end=17; value=\"term3\"}]";	

	private QueryDictionaryBuilderService queryDictionaryBuilderService;
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
	private Set<String> stopWords;
	
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
		
		stopWords = new HashSet<String>();
		stopWords.add("na");
		stopWords.add("und");
		stopWords.add("nu");

		queryDictionaryBuilderService = new QueryDictionaryBuilderService();
		queryDictionaryBuilderService.setStopWords(stopWords);
	}

	@After
	public void tearDown() throws Exception {
	
		File dictionary = new File(DICTIONARY_FILE_PATH);
		if( dictionary.exists() )
			dictionary.delete();
		
	}
	
	@Test
	public void testCreateTermDictionary() throws Exception {
		ITermService termService = EasyMock.createMock(ITermService.class);
		expect(termService.readOccurrencesForTerm(term1)).andReturn(term1Occurences);
		expect(termService.readOccurrencesForTerm(term2)).andReturn(term2Occurences);
		expect(termService.readOccurrencesForTerm(term3)).andReturn(term3Occurences);
		replay(termService);
		
		queryDictionaryBuilderService.setTermService(termService);
		queryDictionaryBuilderService.createTermDictionary(terms, DICTIONARY_FILE_PATH);
		
		verify(termService);
		
		Collection<String> lines = readFile(DICTIONARY_FILE_PATH);
		Iterator<String> lineIterator = lines.iterator();
		assertEquals(18, lines.size());
		
		assertEquals("term1 phrase1\tTERM1", lineIterator.next());
		assertEquals("term1-phrase1\tTERM1", lineIterator.next());		
		assertEquals("term1 phrase2\tTERM1", lineIterator.next());
		assertEquals("term1-phrase2\tTERM1", lineIterator.next());		
		assertEquals("term1 phrase3\tTERM1", lineIterator.next());
		assertEquals("term1-phrase3\tTERM1", lineIterator.next());

		assertEquals("term2 phrase1\tTERM2", lineIterator.next());
		assertEquals("term2-phrase1\tTERM2", lineIterator.next());		
		assertEquals("term2 phrase2\tTERM2", lineIterator.next());
		assertEquals("term2-phrase2\tTERM2", lineIterator.next());		
		assertEquals("term2 phrase3\tTERM2", lineIterator.next());
		assertEquals("term2-phrase3\tTERM2", lineIterator.next());		

		assertEquals("term3 phrase1\tTERM3", lineIterator.next());
		assertEquals("term3-phrase1\tTERM3", lineIterator.next());		
		assertEquals("term3 phrase2\tTERM3", lineIterator.next());
		assertEquals("term3-phrase2\tTERM3", lineIterator.next());		
		assertEquals("term3 phrase3\tTERM3", lineIterator.next());
		assertEquals("term3-phrase3\tTERM3", lineIterator.next());
		
		assertFalse(lineIterator.hasNext());
	}
	
	public Collection<String> readFile(String filePath) throws IOException{
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
	
}

