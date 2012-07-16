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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
public class FacetGroup<T extends StructuralStateExposing> extends ArrayList<T>
		implements Comparable<FacetGroup<T>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Determines the display position of this <code>FacetGroup</code> on the
	 * front end.
	 */
	private final int position;

	private final String name;
	
	private final Map<String, T> facetsBySourceName;

	private final boolean showForBTerms;

	public FacetGroup(String name, int position, boolean showForBTerms) {
		this.name = name;
		this.position = position;
		this.showForBTerms = showForBTerms;
		facetsBySourceName = new HashMap<String, T>();
	}

	public T getElementsBySourceName(String srcName) {
		T cachedFacet = facetsBySourceName.get(srcName);
		if (cachedFacet == null) {
			for (T facet : this) {
				if (facet.getSource().getName().equals(srcName)) {
					facetsBySourceName.put(srcName, facet);
					return facet;
				}
			}
		}
		return cachedFacet;
	}

	public Collection<T> getTaxonomicalElements() {
		Collection<T> facets = new HashSet<T>();
		for (T facet : this)
			if (facet.isHierarchical())
				facets.add(facet);
		return facets;
	}
	
	public Collection<T> getFlatElements() {
		Collection<T> facets = new HashSet<T>();
		for (T facet : this)
			if (facet.isFlat())
				facets.add(facet);
		return facets;
	}
	
//	public Collection<T> getFacetsBySourceType(Facet.SourceType sourceType) {
//		Collection<T> facets = new HashSet<T>();
//		for (T facet : this)
//			if (facet.getStructureState() == sourceType)
//				facets.add(facet);
//		return facets;
//	}

	public <E extends StructuralStateExposing> FacetGroup<E> copyFacetGroup() {
		return new FacetGroup<E>(name, position, showForBTerms);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FacetGroup<T> facetGroup) {
		return this.position - facetGroup.position;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the showForBTerms
	 */
	public boolean showForBTerms() {
		return showForBTerms;
	}

}
