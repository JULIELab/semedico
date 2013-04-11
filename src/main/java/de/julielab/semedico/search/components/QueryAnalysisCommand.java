/**
 * QueryAnalysisCommand.java
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

/**
 * @author faessler
 *
 */
public class QueryAnalysisCommand {
	public String userQuery;
	public String selectedTermId;
	public int facetIdForSelectedTerm;

	public QueryAnalysisCommand() {
		facetIdForSelectedTerm = Integer.MIN_VALUE;
	}
}

