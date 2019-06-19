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

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.semedico.core.entities.state.AbstractUserInterfaceState;
import de.julielab.semedico.core.entities.state.SearchState;
import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import org.slf4j.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author faessler
 * 
 */
public class NewSearchUIPreparationComponent extends AbstractSearchComponent<SemedicoESSearchCarrier> {

	public NewSearchUIPreparationComponent(Logger log) {
		super(log);
	}

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
	public boolean processSearch(SemedicoESSearchCarrier semCarrier) {
		AbstractUserInterfaceState uiState = semCarrier.getUiState();
		if (null == uiState)
			throw new IllegalArgumentException(
					"The UI state is null but is required to be reset.");
		uiState.reset();
		
		SearchState searchState = semCarrier.getSearchState();
		if (null == searchState)
			throw new IllegalArgumentException(
					"The search state is null but is required to be reset.");
		searchState.clear();
		return false;
	}

}
