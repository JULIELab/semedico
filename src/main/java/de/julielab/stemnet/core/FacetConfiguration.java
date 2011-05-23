/** 
 * FacetConfiguration.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: landefeld
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: 28.04.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.stemnet.core;

import java.util.ArrayList;
import java.util.List;

public class FacetConfiguration {

	private Facet facet;
	private boolean hidden;
	private boolean collapsed;
	private boolean expanded;
	private boolean hierarchicMode;

	/**
	 * The list of Terms on the path from the root (inclusive) to the currently
	 * selected term (inclusive) of this facet. E.g. Lipids -> Fatty Acids ->
	 * Fatty Acids, Unsaturated".
	 * In the front end, the children of
	 * "Fatty Acids, Unsaturated" will be displayed with their count. The Terms
	 * on the path do not show a count.
	 */
	private List<FacetTerm> currentPath;

	/**
	 * Called from the FacetConfigurationsStateCreator in the front end.
	 * 
	 * @param facet
	 */
	public FacetConfiguration(Facet facet) {
		super();
		this.facet = facet;
		if (this.facet.getType() == Facet.BIO_MED
				|| this.facet.getType() == Facet.IMMUNOLOGY)
			hierarchicMode = true;

		this.currentPath = new ArrayList<FacetTerm>();
	}

	public Facet getFacet() {
		return facet;
	}

	public void setFacet(Facet facet) {
		this.facet = facet;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public boolean isHierarchicMode() {
		return hierarchicMode;
	}

	public void setHierarchicMode(boolean hierarchicMode) {
		this.hierarchicMode = hierarchicMode;
	}

	public List<FacetTerm> getCurrentPath() {
		return currentPath;
	}

	// Currently unused.
	public void setCurrentPath(List<FacetTerm> currentPath) {
		this.currentPath = currentPath;
	}

	@Override
	public String toString() {
		String string = "{ facet: " + facet + " currentPath: " + currentPath
				+ " hidden: " + hidden + " collapsed: " + collapsed
				+ " expanded: " + expanded + " hierarchicMode: "
				+ hierarchicMode + "}";
		return string;
	}

	public void reset() {
		hidden = false;
		collapsed = false;
		expanded = false;
		hierarchicMode = true;
		currentPath.clear();
	}
}
