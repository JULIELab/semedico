/**
 * UserInterfaceState.java
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
 * Creation date: 15.08.2011
 **/

/**
 * 
 */
package de.julielab.semedico.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.ITermService;

/**
 * @author faessler
 * 
 */
public class UserInterfaceState {
	/**
	 * @return the facetGroups
	 */
	public List<FacetGroup> getFacetGroups() {
		return facetGroups;
	}

	// This map allows us to retrieve the facetConfiguration associated with a
	// particular facet.
	private final Map<Facet, FacetConfiguration> facetConfigurations;
	// The existing facet groups (BioMed, Immunology, ...). These belong to the
	// state of a sessions because they can carry information about facet order
	// and such things.
	private final List<FacetGroup> facetGroups;
	private final FacetHit facetHit;
	private int selectedFacetGroupIndex;
	private FacetGroup selectedFacetGroup;
	private final ITermService termService;
	private final Map<Facet, Set<String>> displayedTermIds;

	public UserInterfaceState(ITermService termService,
			Map<Facet, FacetConfiguration> facetConfigurations,
			List<FacetGroup> facetGroups, FacetHit facetHit) {
		this.termService = termService;
		this.facetConfigurations = facetConfigurations;
		this.facetGroups = facetGroups;
		this.facetHit = facetHit;
		this.selectedFacetGroupIndex = 0;
		this.selectedFacetGroup = facetGroups.get(0);
		this.displayedTermIds = new HashMap<Facet, Set<String>>();
		for (FacetGroup facetGroup : facetGroups) {
			for (Facet facet : facetGroup)
				// Non-hierarchical facets have no need to manage any displayed
				// term IDs. For flat facets, term are just ordered linearly
				// according to their frequencies.
				if (facet.isHierarchical())
					this.displayedTermIds.put(facet, new HashSet<String>());
		}
	}

	/**
	 * @return the selectedFacetGroup
	 */
	public FacetGroup getSelectedFacetGroup() {
		return selectedFacetGroup;
	}

	/**
	 * @param selectedFacetGroup
	 *            the selectedFacetGroup to set
	 */
	public void setSelectedFacetGroup(FacetGroup selectedFacetGroup) {
		this.selectedFacetGroup = selectedFacetGroup;
		for (int i = 0; i < facetGroups.size(); i++) {
			if (facetGroups.get(i) == selectedFacetGroup)
				selectedFacetGroupIndex = i;
		}
	}

	/**
	 * @return the selectedFacetGroupIndex
	 */
	public int getSelectedFacetGroupIndex() {
		return selectedFacetGroupIndex;
	}

	/**
	 * @param selectedFacetGroupIndex
	 *            the selectedFacetGroupIndex to set
	 */
	public void setSelectedFacetGroupIndex(int selectedFacetGroupIndex) {
		this.selectedFacetGroupIndex = selectedFacetGroupIndex;
		this.selectedFacetGroup = facetGroups.get(selectedFacetGroupIndex);
	}

	public void updateLabels(FacetGroup facetGroup) {
		Map<Facet, Set<String>> allIds = getDisplayedTermIdsForCurrentFacetGroup();
		facetHit.updateLabels(allIds);
	}

	public void updateLabels(Facet facet) {
		Map<Facet, Set<String>> displayedTermIds = this.displayedTermIds;
		Map<Facet, Set<String>> allIds = getDisplayedTermIdsForFacet(facet,
				displayedTermIds);
		facetHit.updateLabels(allIds);
	}

	/**
	 * @return the facetHit
	 */
	public FacetHit getFacetHit() {
		return facetHit;
	}

	/**
	 * @return the facetConfigurations
	 */
	public Map<Facet, FacetConfiguration> getFacetConfigurations() {
		return facetConfigurations;
	}

	/**
	 * Returns the IDs of all terms contained in hierarchical facets where the
	 * terms are located on the level currently selected by the user.
	 * <p>
	 * Thus, all term IDs for terms for which Labels must be present for
	 * displaying purposes (frequency ordering and actual display) are returned.
	 * </p>
	 * 
	 * @return All IDs of currently viewable hierarchical terms.
	 */
	public Map<Facet, Set<String>> getDisplayedTermIdsForCurrentFacetGroup() {
		Map<Facet, Set<String>> displayedTermIds = this.displayedTermIds;
		for (Facet facet : selectedFacetGroup) {

			getDisplayedTermIdsForFacet(facet, displayedTermIds);
		}
		return displayedTermIds;
	}

	/**
	 * Returns the IDs of terms contained in <code>Facet</code> which lie on the
	 * currently selected hierarchical level of this facet. If
	 * <code>Facet</code> is flat (i.e. non-hierarchical), the exact same map
	 * <code>displayedTermIds</code> is returned which has been passed to the
	 * method.
	 * <p>
	 * Displayed terms are determined in the following way:
	 * <ul>
	 * <li>If the facet <code>Facet</code> is not drilled down, i.e. the user
	 * did not select any term of this facet and did not enter a search term
	 * associated with the facet, the facet root IDs are added to
	 * <code>displayedTermIds</code>.<br>
	 * <li>If the facet is drilled down, i.e. there is a path of length greater
	 * than zero from a root term of the facet, the IDs of child terms of the
	 * last term on this path are added.
	 * </ul>
	 * </p>
	 * 
	 * @param facet
	 *            The facet of which to return IDs of currently displayed terms.
	 * @param displayedTermIds
	 *            The map which associates the facet which its displayed terms'
	 *            IDs.
	 * @return
	 */
	private Map<Facet, Set<String>> getDisplayedTermIdsForFacet(Facet facet,
			Map<Facet, Set<String>> displayedTermIds) {
		// Flat facets do not need to bother with term IDs as their labels are
		// just linearly ordered by frequency.
		if (facet.isFlat())
			return displayedTermIds;

		Set<String> idSet = displayedTermIds.get(facet);
		FacetConfiguration facetConfiguration = facetConfigurations.get(facet);
		if (facetConfiguration.isDrilledDown()) {
			IFacetTerm lastPathTerm = facetConfiguration.getLastPathElement();
			IFacetTerm term = termService.getNode(lastPathTerm.getId());
			Iterator<IFacetTerm> childIt = term.childIterator();
			while (childIt.hasNext())
				idSet.add(childIt.next().getId());

		} else {
			Iterator<IFacetTerm> rootIt = termService.getFacetRoots(
					facetConfiguration.getFacet()).iterator();
			while (rootIt.hasNext())
				idSet.add(rootIt.next().getId());
		}
		displayedTermIds.put(facet, idSet);
		return displayedTermIds;
	}

	public void reset() {
		for (FacetConfiguration configuration : facetConfigurations.values())
			configuration.reset();
		for (Set<String> idSet : displayedTermIds.values())
			idSet.clear();
		selectedFacetGroupIndex = 0;
		selectedFacetGroup = facetGroups.get(0);
		facetHit.clear();
	}
}
