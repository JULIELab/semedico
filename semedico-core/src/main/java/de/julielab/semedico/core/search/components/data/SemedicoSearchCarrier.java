/**
 * SemedicoSearchCarrier.java
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

import java.util.List;

import de.julielab.scicopia.core.elasticsearch.legacy.SearchCarrier;
import de.julielab.semedico.core.AbstractUserInterfaceState;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.query.QueryToken;

/**
 * @author faessler
 */

public class SemedicoSearchCarrier extends SearchCarrier {
	
	
	private List<QueryToken> userQuery;

	private SemedicoSearchCommand searchCommand;
	private SearchState searchState;
	private AbstractUserInterfaceState uiState;
	private LegacySemedicoSearchResult result;

	public SemedicoSearchCarrier(String chainName) {
		super(chainName);
	}

	@Override
	public void setElapsedTime() {
		if (null != result) {
			result.setElapsedTime(sw.getTime());
		}
		sw.stop();
	}
	
	public List<QueryToken> getUserQuery() {
		return userQuery;
	}
	
	public void setUserQuery(List<QueryToken> userQuery) {
		this.userQuery = userQuery;
	}
	
	public SemedicoSearchCommand getSearchCommand() {
		return searchCommand;
	}
	
	public void setSearchCommand(SemedicoSearchCommand searchCommand) {
		this.searchCommand = searchCommand;
	}
	
	public SearchState getSearchState() {
		return searchState;
	}
	
	public void setSearchState(SearchState searchState) {
		this.searchState = searchState;
	}
	
	public AbstractUserInterfaceState getUiState() {
		return uiState;
	}
	
	public void setUiState(AbstractUserInterfaceState uiState) {
		this.uiState = uiState;
	}
	
	public LegacySemedicoSearchResult getResult() {
		return result;
	}
	
	public void setResult(LegacySemedicoSearchResult result) {
		this.result = result;
	}
}
