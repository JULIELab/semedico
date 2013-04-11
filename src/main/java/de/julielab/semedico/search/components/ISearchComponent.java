/**
 * SearchComponent.java
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
package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author faessler
 * 
 */
public interface ISearchComponent {
	public boolean process(SearchCarrier searchCarrier);

	
	// Full chains
	@Retention(RetentionPolicy.RUNTIME)
	public @interface DocumentChain {}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface FacetCountChain {}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface TermSelectChain {}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface SwitchSearchNodeChain {}
	
	// Sub-chains which assembly search component sequences commonly used in full chains.
	@Retention(RetentionPolicy.RUNTIME)
	public @interface FacetedDocumentSearchSubchain {}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface SearchNodeTermCountsSubchain {}
}
