/** 
 * IFacetRecommenderService.java
 * 
 * Copyright (c) 2014, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 *
 * Author: matthies
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: Sep 30, 2014 
 * 
 * This interface provides three methods for getting a sorted list of Facets.
 **/

package de.julielab.semedico.core.services.interfaces;

import java.util.List;


public interface IFacetRecommenderService {

	/**
	 * Gets a list of all facets that are somehow related to the terms (<em>tids</em>)
	 * in decreasing order of an internal weighting system.
	 * 
	 * @param tids - <code>List</code> of term id Strings
	 * @return <code>List</code> of sorted facet id Strings 
	 */
	public List<String> getSortedFacets(List<String> tids);
	
	/**
	 * The same as {@link #getSortedFacets} but can specify a range.
	 * 
	 * @param tids - <code>List</code> of term id Strings
	 * @param start - <code>Integer</code>
	 * @param end - <code>Integer</code>
	 * @return a portion of the <code>List</code> returned by {@link #getSortedFacets}.
	 */
	public List<String> getSortedFacetsByRange(List<String> tids, int start, int end);
	
	/**
	 * A convenience wrapper for {@link #getSortedFacetsByRange} with <em>start</em> being 0.
	 * 
	 * @param tids - <code>List</code> of term id Strings
	 * @param quantity - <code>Integer</code>
	 * @return <code>List</code> of the first <em>quantity</em> elements returned by {@link #getSortedFacets}.
	 */
	public List<String> getSortedFacetsByQuantity(List<String> tids, int quantity);
	
}
