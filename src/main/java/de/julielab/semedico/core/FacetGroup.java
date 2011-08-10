/**
 * FacetGroup.java
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
 * Creation date: 02.08.2011
 **/

/**
 * 
 */
package de.julielab.semedico.core;

import java.util.ArrayList;

/**
 * A class to assembly all <code>Facet</code> objects belonging to the same
 * display group.
 * <p>
 * This grouping serves the sole purpose to reflect which facets to display
 * together (e.g. by selecting "BioMed", "Immunology" etc. in the front end) and
 * in which order displaying should occur.
 * </p>
 * <p>
 * The display order is determined by the positions of <code>Facet</code>
 * objects within the <code>FacetGroup</code> (which is derived from
 * <code>ArrayList&lt;Facet&gt;</code> and thus ordered).
 * </p>
 * 
 * @author faessler
 * 
 */
public class FacetGroup extends ArrayList<Facet> implements Comparable<FacetGroup> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Determines the display position of this <code>FacetGroup</code> on the front end.
	 */
	private final int position;

	private final String name;

	public FacetGroup(String name, int position) {
		this.name = name;
		this.position = position;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FacetGroup facetGroup) {
		return this.position - facetGroup.position;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

}
