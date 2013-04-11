/**
 * FacetCommand.java
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
 * Creation date: 08.04.2013
 **/

/**
 * 
 */
package de.julielab.semedico.search.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FacetCommand {
	public List<String> fields = new ArrayList<String>();
	public int mincount = Integer.MIN_VALUE;
	public int limit = Integer.MIN_VALUE;
	public Collection<String> terms;
	public String sort;

	public void addFacetField(String field) {
		fields.add(field);
	}
}
