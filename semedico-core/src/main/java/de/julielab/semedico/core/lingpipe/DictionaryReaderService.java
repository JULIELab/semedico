/** 
 * DictionaryReaderService.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;

import com.aliasi.dict.AbstractDictionary;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.TrieDictionary;

import de.julielab.java.utilities.FileUtilities;

public class DictionaryReaderService implements IDictionaryReaderService {

	private static final String SEPARATOR = "\t";
	// TODO perhaps, preferred names should get a higher score? Or give a score
	// according to term frequence in Medline?
	private static final double CHUNK_SCORE = 1.0;
	private ConcurrentHashMap<String, AbstractDictionary<String>> cache;
	private Logger logger;
//	private IConceptService conceptService;

	public DictionaryReaderService(Logger logger) {
		super();
		this.logger = logger;
//		this.conceptService = conceptService;
		this.cache = new ConcurrentHashMap<>();
	}

	protected void readDictionary(AbstractDictionary<String> dictionary, String filePath)
			throws IOException {
		logger.info("Reading query disambiguation dictionary from {}", filePath);
		BufferedReader reader = FileUtilities.getReaderFromFile(new File(filePath));

		String line = reader.readLine();
		while (line != null) {
			String[] split = line.split(SEPARATOR);
			dictionary.addEntry(new DictionaryEntry<String>(split[0], split[1], CHUNK_SCORE));
			line = reader.readLine();
		}
		reader.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.lingpipe.IDictionaryReaderService#getMapDictionary()
	 */
	public MapDictionary<String> getMapDictionary(String dictionaryFilePath, final Collection<de.julielab.semedico.core.services.DictionaryEntry> configuration) throws IOException {
		MapDictionary<String> dictionary = new MapDictionary<String>();
		readDictionary(dictionary, dictionaryFilePath);
		if (null != configuration) {
		for (de.julielab.semedico.core.services.DictionaryEntry dictEntry : configuration)
			dictionary.addEntry(new DictionaryEntry<>(dictEntry.name, dictEntry.category));
		}
		return dictionary;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.lingpipe.IDictionaryReaderService#getTrieDictionary()
	 */
	public TrieDictionary<String> getTrieDictionary(String dictionaryFilePath) {
//		TrieDictionary<String> dictionary = new TrieDictionary<String>();
//		try {
//			readDictionary(dictionary, dictionaryFilePath);
//			Map<String, CoreTerm> coreTerms = conceptService.getCoreConcepts();
//			for (CoreTerm concept : coreTerms.values()) {
//				for (String occurrence : concept.getOccurrences())
//					dictionary.addEntry(new DictionaryEntry<String>(occurrence, concept.getId()));
//			}
//			cache.put(dictionaryFilePath, dictionary);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return dictionary;
		throw new NotImplementedException();
	}

}
