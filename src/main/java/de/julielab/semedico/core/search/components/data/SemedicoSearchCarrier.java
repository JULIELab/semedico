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

import java.util.EnumSet;
import java.util.List;

import de.julielab.semedico.core.AbstractUserInterfaceState;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.search.components.QueryAnalysisCommand;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.services.SearchService.SearchOption;

/**
 * @author faessler
 */

public class SemedicoSearchCarrier extends de.julielab.elastic.query.components.data.SearchCarrier {
	
	
	public QueryAnalysisCommand queryAnalysisCmd;
	/**
	 * Get rid of general-purpose objects and go for stronger typing to make connections clear
	 */
	@Deprecated
	public SemedicoSearchCommand searchCmd;
	public SearchState searchState;
	public AbstractUserInterfaceState uiState;
	public List<ISemedicoQuery> queries;
	public List<EnumSet<SearchOption>> searchOptions;

	public SemedicoSearchCarrier(String chainName) {
		super(chainName);
	}

	public void setElapsedTime() {
//		if (null != result)
//			result.setElapsedTime(sw.getTime());
		sw.stop();
	}
	
}
