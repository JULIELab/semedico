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

package de.julielab.semedico.core;

import de.julielab.semedico.core.Facet.SourceType;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.Taxonomy.IPath;
import de.julielab.semedico.core.Taxonomy.Path;

public class FacetConfiguration implements StructuralStateExposing,
		Comparable<FacetConfiguration> {

	private Facet facet;
	private boolean hidden;
	private boolean collapsed;
	private boolean expanded;
	private Facet.SourceType currentStructureState;

	/**
	 * The list of Terms on the path from the root (inclusive) to the currently
	 * selected term (inclusive) of this facet. E.g. Lipids -> Fatty Acids ->
	 * Fatty Acids, Unsaturated". In the front end, the children of
	 * "Fatty Acids, Unsaturated" will be displayed with their frequency count.
	 * The Terms on the path do not show a count.
	 */
	private IPath currentPath;
	// The FacetGroup this facetConfiguration belongs to. A reference is
	// required
	// to inform the FacetGroup over switching between flat and hierarchical
	// state.
	private final FacetGroup<FacetConfiguration> facetGroup;

	/**
	 * Called from the FacetConfigurationsStateCreator in the front end.
	 * 
	 * @param facet
	 * @param facetConfigurationGroup
	 */
	public FacetConfiguration(Facet facet,
			FacetGroup<FacetConfiguration> facetConfigurationGroup) {
		super();
		this.facet = facet;
		this.facetGroup = facetConfigurationGroup;
		// TODO deal later with that.
		// if (this.facet.getType() != Facet.BIBLIOGRAPHY)
		currentStructureState = facet.getSource().getType();

		this.currentPath = new Path();
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

	public boolean isHierarchical() {
		return currentStructureState instanceof Facet.HierarchicalFieldSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.StructuralStateExposing#isFlat()
	 */
	@Override
	public boolean isFlat() {
		return !isHierarchical();
	}

	public void switchStructureMode() {
		if (currentStructureState instanceof Facet.FieldSource) {
			if (currentStructureState == Facet.FIELD_HIERARCHICAL)
				currentStructureState = Facet.FIELD_FLAT;
			else
				currentStructureState = Facet.FIELD_HIERARCHICAL;
		} // else if... for the case source types other then index fields will
			// be introduced.
	}

	public Facet.Source getSource() {
		return facet.getSource();
	}

	public IPath getCurrentPath() {
		return currentPath;
	}

	/**
	 * Returns the last - i.e. the deepest - element of the current root-to-term
	 * path of this <code>FacetConfiguration</code>.
	 * 
	 * @return The last element on the current root-to-term path or
	 *         <code>null</code> if the path is empty.
	 */
	public IFacetTerm getLastPathElement() {
		if (currentPath.length() > 0)
			return currentPath.getLastNode();
		return null;
	}

	/**
	 * Appends <code>term</code> to the current path from a root to the
	 * currently selected term for this configuration's facet.
	 * 
	 * @param term
	 *            The term to append to the current path.
	 */
	public void expandPath(FacetTerm term) {
		currentPath.appendNode(term);
	}

	public void setCurrentPath(IPath currentPath) {
		this.currentPath = currentPath;
	}

	public boolean isDrilledDown() {
		return currentPath.length() > 0;
	}

	@Override
	public String toString() {
		String string = "{ facet: " + facet + " currentPath: " + currentPath
				+ " hidden: " + hidden + " collapsed: " + collapsed
				+ " expanded: " + expanded + " hierarchicMode: "
				+ isHierarchical() + "}";
		return string;
	}

	public void reset() {
		hidden = false;
		collapsed = false;
		expanded = false;
		currentStructureState = facet.getSource().getType();
		currentPath.clear();
	}

	/**
	 * Defines an ordering for the positions of the FacetBoxes . The FacetBox
	 * components are ordered and displayed the way the FacetConfigurations are
	 * ordered.
	 */
	@Override
	public int compareTo(FacetConfiguration o) {
		return facet.getPosition() - o.getFacet().getPosition();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.StructuralStateExposing#getSourceType()
	 */
	@Override
	public SourceType getStructureState() {
		return currentStructureState;
	}
}
