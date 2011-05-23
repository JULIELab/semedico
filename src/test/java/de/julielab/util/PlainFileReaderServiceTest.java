/** 
 * PlainFileReaderServiceTest.java
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
 * Creation date: 30.10.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;


public class PlainFileReaderServiceTest {

	public static final String FILE_PATH = "src/test/resources/test_stopwords.txt";
	@Test
	public void testReadStopWords() throws Exception{
		IPlainFileReaderService readerService = new PlainFileReaderService();
		Set<String> stopWords = readerService.readFile(FILE_PATH);
		assertEquals(3, stopWords.size());
		assertTrue(stopWords.contains("na"));
		assertTrue(stopWords.contains("nu"));
		assertTrue(stopWords.contains("und"));
	}

}
