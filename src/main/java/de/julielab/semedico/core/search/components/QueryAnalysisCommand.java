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
package de.julielab.semedico.core.search.components;

import java.util.List;

import de.julielab.semedico.core.query.InputEventQuery;
import de.julielab.semedico.core.query.UserQuery;

/**
 * @author faessler
 * 
 */
public class QueryAnalysisCommand {
	public UserQuery userQuery;
	/**
	 * @see #userQuery
	 */
	@Deprecated
	public String selectedTermId;
	/**
	 * @see #userQuery
	 */
	@Deprecated
	public String facetIdForSelectedTerm;
	/**
	 * @see #userQuery
	 */
	@Deprecated
	public List<InputEventQuery> eventQueries;

	public QueryAnalysisCommand() {
	}

}
