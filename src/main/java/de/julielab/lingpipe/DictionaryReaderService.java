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

package de.julielab.lingpipe;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import com.aliasi.dict.AbstractDictionary;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.TrieDictionary;

import de.julielab.semedico.core.services.SemedicoSymbolProvider;

public class DictionaryReaderService implements IDictionaryReaderService {

	private static final String SEPARATOR = "\t";
	private static final double CHUNK_SCORE = 1.0;
	private String dictionaryFilePath;
	private Logger logger;

	public DictionaryReaderService(
			Logger logger,
			@Inject @Symbol(SemedicoSymbolProvider.DISAMBIGUATION_DICT_FILE) String dictionaryFilePath) {
		super();
		this.logger = logger;
		this.dictionaryFilePath = dictionaryFilePath;
	}

	protected void readDictionary(AbstractDictionary<String> dictionary,
			String filePath) throws IOException {
		logger.info("Reading query disambiguation dictionary from {}", filePath);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));

		String line = reader.readLine();
		while (line != null) {
			String[] split = line.split(SEPARATOR);
			dictionary.addEntry(new DictionaryEntry<String>(split[0], split[1],
					CHUNK_SCORE));
			line = reader.readLine();
		}
		reader.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.lingpipe.IDictionaryReaderService#getMapDictionary()
	 */
	public MapDictionary<String> getMapDictionary() {
		MapDictionary<String> dictionary = new MapDictionary<String>();
		try {
			readDictionary(dictionary, dictionaryFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return dictionary;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.lingpipe.IDictionaryReaderService#getTrieDictionary()
	 */
	public TrieDictionary<String> getTrieDictionary() {
		TrieDictionary<String> dictionary = new TrieDictionary<String>();
		try {
			readDictionary(dictionary, dictionaryFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return dictionary;
	}

}
