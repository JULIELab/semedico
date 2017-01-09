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
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;

/**
 * @author faessler
 * 
 */
public class TermSelectUIPreparationComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TermSelectUIPreparation {
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
		AbstractUserInterfaceState uiState = ((SemedicoSearchCarrier)searchCarrier).uiState;
		if (null == uiState)
			throw new IllegalArgumentException(
					"The UI state is null but it is required to reset the label store.");
		uiState.getLabelStore().reset();
		return false;
	}

}
