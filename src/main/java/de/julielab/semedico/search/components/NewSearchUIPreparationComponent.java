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
package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.tapestry5.services.ApplicationStateManager;

import de.julielab.semedico.core.UserInterfaceState;

/**
 * @author faessler
 * 
 */
public class NewSearchUIPreparationComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface NewSearchUIPreparation{}
	
	private final ApplicationStateManager asm;

	public NewSearchUIPreparationComponent(ApplicationStateManager asm) {
		this.asm = asm;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.components.ISearchComponent#process(de.julielab
	 * .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean process(SearchCarrier searchCarrier) {
		UserInterfaceState uiState = asm.get(UserInterfaceState.class);
		uiState.reset();
		return false;
	}

}
