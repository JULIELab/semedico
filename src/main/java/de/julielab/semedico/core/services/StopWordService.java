/**
 * StopWordService.java
 *
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 07.06.2011
 **/

package de.julielab.semedico.core.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import de.julielab.semedico.core.services.interfaces.IStopWordService;

/**
 * Reads stop words from file.
 * 
 * @author faessler
 */
public class StopWordService implements IStopWordService {

	private Logger logger;

	private Set<String> stopWordSet;
	private List<String> stopWordList;
	private String[] stopWordArray;

	private File stopWordFile;

	public StopWordService(
			Logger logger,
			@Inject @Symbol(SemedicoSymbolConstants.STOP_WORDS_FILE) String fileName) {
		this.logger = logger;
		stopWordFile = new File(fileName);
		if (!stopWordFile.exists())
			logger.warn("StopWord file \"" + fileName
					+ "\" does not exist.");
	}

	@Override
	public Set<String> getAsSet() {
		if (stopWordSet != null)
			return stopWordSet;

		stopWordSet = new HashSet<String>();
		if (stopWordList != null)
			stopWordSet.addAll(stopWordList);
		else
			readStopWords(stopWordSet);
		return stopWordSet;
	}

	@Override
	public List<String> getAsList() {
		stopWordList = new ArrayList<String>();
		if (stopWordSet != null)
			stopWordList.addAll(stopWordSet);
		else
			readStopWords(stopWordList);
		return stopWordList;
	}

	@Override
	public String[] getAsArray() {
		if (stopWordArray != null)
			return stopWordArray;

		if (stopWordList == null)
			getAsList();

		stopWordArray = new String[stopWordList.size()];
		stopWordList.toArray(stopWordArray);
		return stopWordArray;
	}

	private <T extends Collection<String>> void readStopWords(T stopWords) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(stopWordFile));
			String line;
			while ((line = br.readLine()) != null)
				stopWords.add(line);
		} catch (FileNotFoundException e) {
			logger.error("File {} could not be found: {}",
					stopWordFile.getAbsoluteFile(), e);
		} catch (IOException e) {
			logger.error(
					"An IO Exception occured while reading from file {}: {}",
					stopWordFile.getAbsoluteFile(), e);
		}
	}

}
