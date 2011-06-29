/** 
 * ExactDictionaryChunkerFactoryTest.java
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


import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

public class ExactDictionaryChunkerFactoryTest {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testCreateChunker() throws Exception{
		IDictionaryReaderService dictionaryReaderService = createMock(DictionaryReaderService.class);
		MapDictionary<String> dictionary = new MapDictionary<String>();
		dictionary.add(new DictionaryEntry<String>("phrase1", "category1"));
		dictionary.add(new DictionaryEntry<String>("phrase2", "category2"));
		dictionary.add(new DictionaryEntry<String>("phrase3", "category3"));
		
		expect(dictionaryReaderService.getMapDictionary()).andReturn(dictionary);
		replay(dictionaryReaderService);
		
		ExactDictionaryChunkerFactory chunkerFactory = new ExactDictionaryChunkerFactory();
		chunkerFactory.setDictionaryReaderService(dictionaryReaderService);
		
		ExactDictionaryChunker chunker = (ExactDictionaryChunker) chunkerFactory.createChunker(true);
		verify(dictionaryReaderService);
		
		assertTrue(chunker.caseSensitive());
		assertEquals(IndoEuropeanTokenizerFactory.FACTORY, chunker.tokenizerFactory());
	}
}
