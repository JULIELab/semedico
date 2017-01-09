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

package de.julielab.semedico.core.lingpipe;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.slf4j.LoggerFactory;

import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.TrieDictionary;

import de.julielab.semedico.core.lingpipe.DictionaryReaderService;
import de.julielab.semedico.core.services.TermNeo4jService;
import de.julielab.semedico.core.services.interfaces.ICacheService;

public class DictionaryReaderServiceTest extends TestCase {

	public static final String READ_DICTIONARY_FILE_PATH = "src/test/resources/read_test.dic";
	private DictionaryReaderService dictionaryReaderService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ICacheService cacheService = EasyMock.createNiceMock(ICacheService.class);
		EasyMock.replay(cacheService);
		TermNeo4jService termService = new TermNeo4jService(null, cacheService, null, null, null);
		dictionaryReaderService = new DictionaryReaderService(LoggerFactory.getLogger(DictionaryReaderService.class),
				termService);
	}

	public void testReadDictionary() throws Exception {

		MapDictionary<String> dictionary = new MapDictionary<String>();
		dictionaryReaderService.readDictionary(dictionary, READ_DICTIONARY_FILE_PATH);
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

	/**
	 * In this test, the special terms from the term service are included. Those
	 * are ambiguous, so there are multiple entries for "*" for example. This
	 * could cause this test to fail because sometimes the one and sometimes the
	 * other dictionary entry will be included in "sortedEntries". If this
	 * happens, the test must be extended for ambiguity.
	 * 
	 * @throws Exception
	 */
	public void testGetMapDictionary() throws Exception {
		MapDictionary<String> dictionary = dictionaryReaderService.getMapDictionary(READ_DICTIONARY_FILE_PATH);
		assertEquals(20, dictionary.size());

		Map<String, String> sortedEntries = new TreeMap<String, String>();
		for (DictionaryEntry<String> entry : dictionary) {
			sortedEntries.put(entry.phrase(), entry.category());
		}

		// This generates the code for the test according to the current
		// dictionary. Of course you have to check whether
		// the generated code reflects the CORRECT dictionary!!
		// for (String key : sortedEntries.keySet()) {
		// System.out.println("key = iterator.next(); assertEquals(\"" + key +
		// "\", key);assertEquals(\""
		// + sortedEntries.get(key) + "\", sortedEntries.get(key));");
		// }

		Iterator<String> iterator = sortedEntries.keySet().iterator();
		String key;

		key = iterator.next();
		assertEquals("*", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("?", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("Any term", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("all", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("any", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("any concept", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("anyterm", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("anything", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("hmhmhm", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("möp", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
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
		key = iterator.next();
		assertEquals("what", key);
		assertEquals("ctid0", sortedEntries.get(key));

		assertEquals(dictionary, dictionaryReaderService.getMapDictionary(READ_DICTIONARY_FILE_PATH));
	}

	/**
	 * In this test, the special terms from the term service are included. Those
	 * are ambiguous, so there are multiple entries for "*" for example. This
	 * could cause this test to fail because sometimes the one and sometimes the
	 * other dictionary entry will be included in "sortedEntries". If this
	 * happens, the test must be extended for ambiguity.
	 * 
	 * @throws Exception
	 */
	public void testGetTrieDictionary() throws Exception {
		TrieDictionary<String> dictionary = dictionaryReaderService.getTrieDictionary(READ_DICTIONARY_FILE_PATH);
		assertEquals(20, dictionary.size());

		Map<String, String> sortedEntries = new TreeMap<String, String>();
		for (DictionaryEntry<String> entry : dictionary)
			sortedEntries.put(entry.phrase(), entry.category());
		Iterator<String> iterator = sortedEntries.keySet().iterator();

		// This generates the code for the test according to the current
		// dictionary. Of course you have to check whether
		// the generated code reflects the CORRECT dictionary!!
		// for (String key : sortedEntries.keySet()) {
		// System.out.println("key = iterator.next(); assertEquals(\"" + key +
		// "\", key);assertEquals(\"" +
		// sortedEntries.get(key) + "\", sortedEntries.get(key));");
		// }

		String key;

		key = iterator.next();
		assertEquals("*", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("?", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("Any term", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("all", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("any", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("any concept", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("anyterm", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("anything", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("hmhmhm", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
		assertEquals("möp", key);
		assertEquals("ctid0", sortedEntries.get(key));
		key = iterator.next();
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
		key = iterator.next();
		assertEquals("what", key);
		assertEquals("ctid0", sortedEntries.get(key));

		assertEquals(dictionary, dictionaryReaderService.getTrieDictionary(READ_DICTIONARY_FILE_PATH));
	}
}
