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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;

import de.julielab.semedico.core.Facet.SourceType;
import de.julielab.semedico.core.taxonomy.IFacetTerm;
import de.julielab.semedico.core.taxonomy.IPath;
import de.julielab.semedico.core.taxonomy.ImmutablePathWrapper;
import de.julielab.semedico.core.taxonomy.Path;
import de.julielab.util.DisplayGroup;
import de.julielab.util.LabelFilter;

public class FacetConfiguration implements StructuralStateExposing,
		Comparable<FacetConfiguration> {

	// To which facet in the data model this facetConfiguration belongs to.
	private Facet facet;
	// Whether this facetConfiguratino shall not be displayed to the user in
	// form of a FacetBox component.
	private boolean hidden;
	// Whether the FacetBox belonging to this facetConfiguration is collapsed,
	// i.e. only the Facet name but no terms are displayed to the user.
	private boolean collapsed;
	// Whether the FacetBox belonging to this facetConfiguration is supposed to
	// show more terms than is determined by the default value.
	private boolean expanded;
	/**
	 * Indicates if this facet is forced to flat, frequency ordered term counts.
	 * 
	 * @see FacetConfiguration#isForcedToFlatFacetCounts()
	 */
	private boolean forcedToFlatFacetCounts;
	private boolean taxonomicMode;

	/**
	 * The list of Terms on the path from the root (inclusive) to the currently
	 * selected term (inclusive) of this facet. E.g. Lipids -> Fatty Acids ->
	 * Fatty Acids, Unsaturated". In the front end, the children of
	 * "Fatty Acids, Unsaturated" will be displayed with their frequency count.
	 * The Terms on the path do not show a count.
	 */
	private IPath currentPath;
	private final Collection<IFacetTerm> facetRoots;
	private final Logger logger;
	private DisplayGroup<Label> displayGroup;

	// The FacetGroup this facetConfiguration belongs to. A reference is
	// required
	// to inform the FacetGroup over switching between flat and hierarchical
	// state.

	/**
	 * Called from the FacetConfigurationsStateCreator in the front end.
	 * 
	 * @param logger
	 * 
	 * @param facet
	 * @param facetRoots
	 * @param termService
	 * @param facetConfigurationGroup
	 */
	public FacetConfiguration(Logger logger, Facet facet,
			Collection<IFacetTerm> facetRoots) {
		super();
		this.logger = logger;
		this.facet = facet;
		this.facetRoots = facetRoots;
		// TODO deal later with that.
		// if (this.facet.getType() != Facet.BIBLIOGRAPHY)
		this.taxonomicMode = facet.getSource().isHierarchical();
		this.forcedToFlatFacetCounts = false;
		this.currentPath = new Path();
		this.hidden = true;
		this.displayGroup = new DisplayGroup<Label>(new LabelFilter(), 3);
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
		return taxonomicMode;
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
		// Only when the facet source is genuinely hierarchical, the mode can be
		// switched. It doesn't make sense for terms without any structural
		// information to be organized hierarchically.
		if (facet.isFlat()) {
			logger.warn("Facet \"" + facet.getName() + "\" with genuinely flat structure was triggered to change to hierarchical display of terms which is not possible.");
			return;
		}
		taxonomicMode = !taxonomicMode;
	}

	public Facet.Source getSource() {
		return facet.getSource();
	}

	/**
	 * <p>
	 * Returns the terms which have been chosen by the user to display in this
	 * facetConfiguration's facet.
	 * </p>
	 * <p>
	 * These terms are determined as follows:
	 * <ul>
	 * <li>If the facet is not drilled down, i.e. the user did not select any
	 * term of this facet and did not enter a search term associated with the
	 * facet, the facet root IDs are returned.
	 * <li>If the <code>facetConfiguration</code> is drilled down, i.e. the user
	 * has been viewing successors of a root term, the children of the last
	 * clicked-on term - i.e. the root of the currently viewed subtree - are
	 * returned.</code>.
	 * </ul>
	 * </p>
	 * 
	 * @param facetConfiguration
	 *            The facet of which to return all terms on the currently
	 *            selected hierarchy level.
	 * @param termStorageByFacetConfiguration
	 *            The map which associates the facetConfiguration with the root
	 *            terms of its currently selected subtree.
	 * @return The children of the currently selected subtree root or
	 *         <code>null</code> if the facet associated with this
	 *         <code>facetConfiguration</code> is flat.
	 */
	public Collection<IFacetTerm> getRootTermsForCurrentlySelectedSubTree() {
		Collection<IFacetTerm> returnTerms = null;
		// Flat facets do not need to bother with term IDs as their labels are
		// just linearly ordered by frequency.
		if (this.facet.isHierarchical()) {

			if (isDrilledDown()) {
				Set<IFacetTerm> termSet = new HashSet<IFacetTerm>();
				IFacetTerm lastPathTerm = this.currentPath.getLastNode();
				for (IFacetTerm child : lastPathTerm.getAllChildren()) {
					if (child.isContainedInFacet(this.facet)) {
						termSet.add(child);
					}
				}
				returnTerms = termSet;
			} else {
				returnTerms = this.facetRoots;
			}
		}
		return returnTerms;
	}

	/**
	 * This method exists solely for Tapestry to loop over the path elements.
	 * Returns a wrapper object for the current path which is immutable.
	 * 
	 * @return An immutable wrapper for the current path.
	 */
	public IPath getCurrentPath() {
		return new ImmutablePathWrapper(currentPath);
	}

	public int getCurrentPathLength() {
		return currentPath.length();
	}

	public IFacetTerm removeLastNodeOfCurrentPath() {
		forcedToFlatFacetCounts = false;
		try {
			return currentPath.removeLastNode();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Appends <code>term</code> to the current path from a root to the
	 * currently selected term for this configuration's facet.
	 * 
	 * @param term
	 *            The term to append to the current path.
	 */
	public void appendNodeToCurrentPath(IFacetTerm term) {
		forcedToFlatFacetCounts = false;
		try {
			currentPath.appendNode(term);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void clearCurrentPath() {
		forcedToFlatFacetCounts = false;
		try {
			currentPath.clear();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public IFacetTerm getNodeOnCurrentPathAt(int i) {
		return currentPath.getNodeAt(i);
	}

	public boolean containsCurrentPathNode(IFacetTerm term) {
		return currentPath.containsNode(term);
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

	public void setCurrentPath(IPath currentPath) {
		this.currentPath = currentPath;
	}

	public boolean isDrilledDown() {
		return currentPath.length() > 0;
	}

	// @Override
	// public String toString() {
	// String string = "{ facet: " + facet + " currentPath: " + currentPath
	// + " hidden: " + hidden + " collapsed: " + collapsed
	// + " expanded: " + expanded + " hierarchicMode: "
	// + isHierarchical() + "}";
	// return string;
	// }

	public void reset() {
		hidden = true;
		collapsed = false;
		expanded = false;
		taxonomicMode = facet.getSource().isHierarchical();
		clearCurrentPath();
		displayGroup.reset();
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

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see de.julielab.semedico.core.StructuralStateExposing#getSourceType()
//	 */
//	@Override
//	public SourceType getStructureState() {
//		return currentStructureState;
//	}

	/**
	 * <p>
	 * Returns <code>true</code> if this <code>FacetConfiguration</code> is
	 * forced to retrieve facet counts from the top N counts in its field rather
	 * than querying the selected subtree roots.
	 * </p>
	 * <p>
	 * This may happen when there are too many terms to query, e.g. for the
	 * roots of the 'Proteins/Genes' facet which has approx. 25k roots.
	 * </p>
	 * 
	 * @return the forcedToFlatFacetCounts
	 */
	public boolean isForcedToFlatFacetCounts() {
		return forcedToFlatFacetCounts;
	}

	/**
	 * @param forcedToFlatFacetCounts
	 *            the forcedToFlatFacetCounts to set
	 */
	public void setForcedToFlatFacetCounts(boolean forcedToFlatFacetCounts) {
		this.forcedToFlatFacetCounts = forcedToFlatFacetCounts;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FacetConfiguration for facet '" + facet.getName() + "'"; 
	}

	/**
	 * 
	 * @return
	 */
	public DisplayGroup<Label> getLabelDisplayGroup() {
		return displayGroup;
	}

	/**
	 * 
	 */
	public void refresh() {
	}
	
	
}
