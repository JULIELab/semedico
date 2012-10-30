/**
 * Label.java
 *
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 18.08.2011
 **/

/**
 * 
 */
package de.julielab.semedico.core;

import de.julielab.semedico.bterms.TermStatistics;


/**
 * @author faessler
 *
 */
public abstract class Label implements Comparable<Label> {
	
	private Long count;
	private final String id;
	private final String name;
	private TermStatistics stats;
	
	public Label(String name, String id) {
		this.name = name;
		this.id = id;
		this.count = 0L;
	}

	/**
	 * @return the count
	 */
	public Long getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(Long count) {
		this.count = count;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the stats
	 */
	public TermStatistics getStats() {
		return stats;
	}

	/**
	 * @param stats the stats to set
	 */
	public void setStats(TermStatistics stats) {
		this.stats = stats;
	}

	public int compareTo(Label label) {
		return Long.signum(label.getCount() - getCount());
	}
	
	public abstract boolean hasChildHitsInFacet(Facet facet);
	
	public void clear() {
		this.setCount(0L);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

}

