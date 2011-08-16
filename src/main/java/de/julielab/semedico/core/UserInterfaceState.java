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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	
	public UserInterfaceState(ITermService termService, Map<Facet, FacetConfiguration> facetConfigurations, List<FacetGroup> facetGroups, FacetHit facetHit) {
		this.termService = termService;
		this.facetConfigurations = facetConfigurations;
		this.facetGroups = facetGroups;
		this.facetHit = facetHit;
		this.selectedFacetGroupIndex = 0;
		this.selectedFacetGroup = facetGroups.get(0);
	}
	
	/**
	 * @return the selectedFacetGroup
	 */
	public FacetGroup getSelectedFacetGroup() {
		return selectedFacetGroup;
	}

	/**
	 * @param selectedFacetGroup the selectedFacetGroup to set
	 */
	public void setSelectedFacetGroup(FacetGroup selectedFacetGroup) {
		this.selectedFacetGroup = selectedFacetGroup;
		for (int i = 0; i < facetGroups.size(); i++) {
			if(facetGroups.get(i) == selectedFacetGroup)
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
	 * @param selectedFacetGroupIndex the selectedFacetGroupIndex to set
	 */
	public void setSelectedFacetGroupIndex(int selectedFacetGroupIndex) {
		this.selectedFacetGroupIndex = selectedFacetGroupIndex;
		this.selectedFacetGroup = facetGroups.get(selectedFacetGroupIndex);
	}
	
	public void updateLabels() {
		List<String> allIds = getDisplayedTermIds();
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

	private List<String> getDisplayedTermIds() {
		List<String> displayedTermIds = new ArrayList<String>();
		for (Facet facet : selectedFacetGroup) {
			getDisplayedTermIdsForFacet(displayedTermIds, facet);
		}
		return displayedTermIds;
	}

	private void getDisplayedTermIdsForFacet(List<String> displayedTermIds,
			Facet facet) {
		FacetConfiguration facetConfiguration = facetConfigurations.get(facet);
		if (facetConfiguration.isDrilledDown()) {
			IFacetTerm lastPathTerm = facetConfiguration.getLastPathElement();
			IFacetTerm term = termService.getNode(lastPathTerm.getId());
			Iterator<IFacetTerm> childIt = term.childIterator();
			while (childIt.hasNext())
				displayedTermIds.add(childIt.next().getId());

		} else {
			Iterator<IFacetTerm> rootIt = termService.getFacetRoots(
					facetConfiguration.getFacet()).iterator();
			while (rootIt.hasNext())
				displayedTermIds.add(rootIt.next().getId());
		}
	}
	
	public void reset() {
		for (FacetConfiguration configuration : facetConfigurations.values())
			configuration.reset();
		selectedFacetGroupIndex = 0;
		selectedFacetGroup = facetGroups.get(0);
		facetHit.clear();
	}
}

