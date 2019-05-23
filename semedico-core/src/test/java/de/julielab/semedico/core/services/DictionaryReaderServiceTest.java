package de.julielab.semedico.core.services;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import junit.framework.TestCase;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.services.DictionaryReaderService;

public class DictionaryReaderServiceTest extends TestCase {

	public static final String READ_DICTIONARY_FILE_PATH = "src/test/resources/read_test.dic";
	private DictionaryReaderService dictionaryReaderService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		dictionaryReaderService = new DictionaryReaderService();
	}

	public void testReadDictionary() throws Exception {
		Multimap<String, String> dictionary = dictionaryReaderService.readDictionary(READ_DICTIONARY_FILE_PATH);
		assertEquals(9, dictionary.size());

		Map<String, String> sortedEntries = new TreeMap<>();
		for (Entry<String, String> entry : dictionary.entries()) {
			sortedEntries.put(entry.getKey(), entry.getValue());
		}
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

}
