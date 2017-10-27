/**
 * NewSearchUIPreparationComponent.java
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
 * Creation date: 09.04.2013
 **/

/**
 * 
 */
package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.semedico.core.AbstractUserInterfaceState;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;

/**
 * @author faessler
 * 
 */
public class NewSearchUIPreparationComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface NewSearchUIPreparation {
		//
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.components.ISearchComponent#process(de.julielab
	 * .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		AbstractUserInterfaceState uiState = semCarrier.uiState;
		if (null == uiState)
			throw new IllegalArgumentException(
					"The UI state is null but is required to be reset.");
		uiState.reset();
		
		SearchState searchState = semCarrier.searchState;
		if (null == searchState)
			throw new IllegalArgumentException(
					"The search state is null but is required to be reset.");
		searchState.clear();
		return false;
	}

}
