/** 
 * DictionaryReaderServiceTest.java
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
 * Creation date: 29.07.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.lingpipe;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.TrieDictionary;

public class DictionaryReaderServiceTest extends TestCase {

	public static final String READ_DICTIONARY_FILE_PATH = "src/test/resources/read_test.dic";
	private DictionaryReaderService dictionaryReaderService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		dictionaryReaderService = new DictionaryReaderService(
				LoggerFactory.getLogger(DictionaryReaderService.class),
				READ_DICTIONARY_FILE_PATH);
	}

	public void testReadDictionary() throws Exception {

		MapDictionary<String> dictionary = new MapDictionary<String>();
		dictionaryReaderService.readDictionary(dictionary,
				READ_DICTIONARY_FILE_PATH);
		assertEquals(9, dictionary.size());

		Map<String, String> sortedEntries = new TreeMap<String, String>();
		for (DictionaryEntry<String> entry : dictionary)
			sortedEntries.put(entry.phrase(), entry.category());
		Iterator<String> iterator = sortedEntries.keySet().iterator();

		String key = iterator.next();
		assertEquals("term1 phrase1", key);
		assertEquals("TERM1", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term1 phrase2", key);
		assertEquals("TERM1", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term1 phrase3", key);
		assertEquals("TERM1", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term2 phrase1", key);
		assertEquals("TERM2", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term2 phrase2", key);
		assertEquals("TERM2", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term2 phrase3", key);
		assertEquals("TERM2", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term3 phrase1", key);
		assertEquals("TERM3", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term3 phrase2", key);
		assertEquals("TERM3", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term3 phrase3", key);
		assertEquals("TERM3", sortedEntries.get(key));
	}

	public void testGetMapDictionary() throws Exception {
		MapDictionary<String> dictionary = dictionaryReaderService
				.getMapDictionary();
		assertEquals(9, dictionary.size());

		Map<String, String> sortedEntries = new TreeMap<String, String>();
		for (DictionaryEntry<String> entry : dictionary)
			sortedEntries.put(entry.phrase(), entry.category());
		Iterator<String> iterator = sortedEntries.keySet().iterator();

		String key = iterator.next();
		assertEquals("term1 phrase1", key);
		assertEquals("TERM1", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term1 phrase2", key);
		assertEquals("TERM1", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term1 phrase3", key);
		assertEquals("TERM1", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term2 phrase1", key);
		assertEquals("TERM2", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term2 phrase2", key);
		assertEquals("TERM2", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term2 phrase3", key);
		assertEquals("TERM2", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term3 phrase1", key);
		assertEquals("TERM3", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term3 phrase2", key);
		assertEquals("TERM3", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term3 phrase3", key);
		assertEquals("TERM3", sortedEntries.get(key));

		assertEquals(dictionary, dictionaryReaderService.getMapDictionary());
	}

	public void testGetTrieDictionary() throws Exception {
		TrieDictionary<String> dictionary = dictionaryReaderService
				.getTrieDictionary();
		assertEquals(9, dictionary.size());

		Map<String, String> sortedEntries = new TreeMap<String, String>();
		for (DictionaryEntry<String> entry : dictionary)
			sortedEntries.put(entry.phrase(), entry.category());
		Iterator<String> iterator = sortedEntries.keySet().iterator();

		String key = iterator.next();
		assertEquals("term1 phrase1", key);
		assertEquals("TERM1", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term1 phrase2", key);
		assertEquals("TERM1", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term1 phrase3", key);
		assertEquals("TERM1", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term2 phrase1", key);
		assertEquals("TERM2", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term2 phrase2", key);
		assertEquals("TERM2", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term2 phrase3", key);
		assertEquals("TERM2", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term3 phrase1", key);
		assertEquals("TERM3", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term3 phrase2", key);
		assertEquals("TERM3", sortedEntries.get(key));

		key = iterator.next();
		assertEquals("term3 phrase3", key);
		assertEquals("TERM3", sortedEntries.get(key));

		assertEquals(dictionary, dictionaryReaderService.getTrieDictionary());
	}
}
