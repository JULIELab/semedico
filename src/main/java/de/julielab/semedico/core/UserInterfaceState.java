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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

/**
 * @author faessler
 * 
 */
public class UserInterfaceState {

	// This map allows us to retrieve the facetConfiguration associated with a
	// particular facet.
	protected final Map<Facet, UIFacet> facetConfigurations;
	// private final Multimap<Class<?>, FacetConfiguration>
	// facetConfigurationsBySourceType;
	// The existing facet groups (BioMed, Immunology, ...). These belong to the
	// state of a sessions because they can carry information about facet order
	// and such things.
	protected final List<FacetGroup<UIFacet>> facetConfigurationGroups;
	protected final LabelStore labelStore;
	protected int selectedFacetGroupIndex;
	protected FacetGroup<UIFacet> selectedFacetGroup;
	protected final SearchState searchState;
	protected final Logger logger;

	public UserInterfaceState(Logger logger,
			Map<Facet, UIFacet> facetConfigurations,
			List<FacetGroup<UIFacet>> facetConfigurationGroups,
			LabelStore labelStore, SearchState searchState) {
		this.logger = logger;
		this.facetConfigurations = facetConfigurations;
		this.facetConfigurationGroups = facetConfigurationGroups;
		this.labelStore = labelStore;
		this.searchState = searchState;
		this.selectedFacetGroupIndex = 0;
		this.selectedFacetGroup = facetConfigurationGroups
				.get(selectedFacetGroupIndex);
		
	}

	/**
	 * @param selectedFacetGroup
	 *            the selectedFacetGroup to set
	 */
	public void setSelectedFacetGroup(FacetGroup<UIFacet> selectedFacetGroup) {
		this.selectedFacetGroup = selectedFacetGroup;
		for (int i = 0; i < facetConfigurationGroups.size(); i++) {
			if (facetConfigurationGroups.get(i) == selectedFacetGroup)
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
		this.selectedFacetGroup = facetConfigurationGroups
				.get(selectedFacetGroupIndex);
	}

	/**
	 * @return the labelStore
	 */
	public LabelStore getLabelStore() {
		return labelStore;
	}

	public Collection<FacetGroup<UIFacet>> getFacetGroups() {
		return facetConfigurationGroups;
	}

	/**
	 * @return the facetConfigurations
	 */
	public Map<Facet, UIFacet> getFacetConfigurations() {
		return facetConfigurations;
	}

	/**
	 * @return the selectedFacetGroup
	 */
	public FacetGroup<UIFacet> getSelectedFacetGroup() {
		return selectedFacetGroup;
	}

	/**
	 * 
	 */
	public void clear() {
		labelStore.clear();
		
	}

	public void refresh() {
	}

	public void reset() {
		for (UIFacet configuration : facetConfigurations.values())
			configuration.reset();
		selectedFacetGroupIndex = 0;
		selectedFacetGroup = facetConfigurationGroups
				.get(selectedFacetGroupIndex);
		labelStore.reset();
	}
	

	//set facet on the top and group all other facets
	public void setFirstFacet(FacetGroup<UIFacet> group, UIFacet facet){
		for(UIFacet f: group){
			f.setPosition(f.position + 1);
		}
		facet.setPosition(0);
		Collections.sort(group);
	}


	

}
