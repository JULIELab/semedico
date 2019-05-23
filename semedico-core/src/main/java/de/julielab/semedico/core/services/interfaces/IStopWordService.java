/**
 * IStopWordService.java
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

package de.julielab.semedico.core.services.interfaces;

import java.util.List;
import java.util.Set;

import de.julielab.semedico.core.query.QueryToken;

/**
 * Service for delivering stop words.
 * @author faessler/kampe
 */
public interface IStopWordService {
	public Set<String> getStopWords();
	public void loadStopWords();
	
	public List<QueryToken> filterStopTokens(List<QueryToken> queryTokens);

	public boolean isStopWord(String word);
}
