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

import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.MapDictionary;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.julielab.semedico.core.concepts.CoreConcept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.services.BaseConceptService;
import de.julielab.semedico.core.services.CacheService;
import de.julielab.semedico.core.services.ConceptNeo4jService;
import de.julielab.semedico.core.services.interfaces.ICacheService;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;

import static org.testng.AssertJUnit.assertEquals;
import static org.assertj.core.api.Assertions.*;
public class DictionaryReaderServiceTest  {

	public static final String READ_DICTIONARY_FILE_PATH = "src/test/resources/read_test.dic";
	private DictionaryReaderService dictionaryReaderService;

	@BeforeClass
	protected void setUp() throws Exception {
		dictionaryReaderService = new DictionaryReaderService(LoggerFactory.getLogger(DictionaryReaderService.class));
	}

	@Test
	public void testReadDictionary() throws Exception {
		MapDictionary<String> dictionary = new MapDictionary<>();
		dictionaryReaderService.readDictionary(dictionary, READ_DICTIONARY_FILE_PATH);
		assertEquals(9, dictionary.size());

		Map<String, String> sortedEntries = new TreeMap<>();
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

    @Test
    public void testCoreConceptsDictionary() throws Exception {
        final ConceptNeo4jService conceptService = new ConceptNeo4jService(LoggerFactory.getLogger(ConceptNeo4jService.class), null, null, null, null);
        List<de.julielab.semedico.core.services.DictionaryEntry> configuration = new ArrayList<>();
        Map<String, CoreConcept> coreTerms = conceptService.getCoreConcepts();
        for (CoreConcept concept : coreTerms.values()) {
            System.out.println(concept.getPreferredName());
            for (String occurrence : concept.getOccurrences())
                configuration.add(new de.julielab.semedico.core.services.DictionaryEntry(occurrence, concept.getId()));
        }
        final MapDictionary<String> mapDictionary = dictionaryReaderService.getMapDictionary(READ_DICTIONARY_FILE_PATH, configuration);
		List<String> actualTerms = new ArrayList<>();
        for (DictionaryEntry<String> entry : mapDictionary) {
            actualTerms.add(entry.phrase() );
        }
		assertThat(actualTerms).contains("any", "all", "*", "?");
    }

}
