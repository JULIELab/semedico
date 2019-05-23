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
package de.julielab.scicopia.core.elasticsearch.legacy;

import java.util.Collection;

public class FacetCommand {
	private String field;
	private int mincount = 1;
	public int limit = Integer.MIN_VALUE;
	public Collection<String> terms;
	public SortOrder sort;
	public String name;
	public String filterExpression;

	public enum SortOrder {
		COUNT, TERM, REVERSE_COUNT, REVERSE_TERM, DOC_SCORE, REVERSE_DOC_SCORE
	}

	public String getField() {
		return field;
	}
	
	public void setField(String field) {
		this.field = field;
	}
	
	public int getMinCount() {
		return mincount;
	}
	
	public void setMinCount(int mincount) {
		this.mincount = mincount;
	}

	@Override
	public String toString() {
		return "FacetCommand [field=" + field + ", terms=" + terms
				+ ", name=" + name + "]";
	}

}
