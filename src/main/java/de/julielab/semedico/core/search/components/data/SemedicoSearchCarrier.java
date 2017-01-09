/**
 * SearchCarrier.java
 *
 * Copyright (c) 2013, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 06.04.2013
 **/

/**
 * 
 */
package de.julielab.semedico.core.search.components.data;

import de.julielab.semedico.core.AbstractUserInterfaceState;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.search.components.QueryAnalysisCommand;

/**
 * @author faessler
 */

public class SemedicoSearchCarrier extends de.julielab.elastic.query.components.data.SearchCarrier {
	
	
	public QueryAnalysisCommand queryAnalysisCmd;
	public SemedicoSearchCommand searchCmd;
	public SemedicoSearchResult searchResult;
	public SearchState searchState;
	public AbstractUserInterfaceState uiState;

	public SemedicoSearchCarrier(String chainName) {
		super(chainName);
	}


	public void setElapsedTime() {
		if (null != searchResult)
			searchResult.elapsedTime = sw.getTime();
		sw.stop();
	}
	
}
