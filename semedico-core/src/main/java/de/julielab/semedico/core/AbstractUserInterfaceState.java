/**
 * AbstractUserInterfaceState.java
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

import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.facets.UIFacetGroup;
import de.julielab.semedico.core.search.components.data.LabelStore;

/**
 * @author faessler
 * 
 */
public class AbstractUserInterfaceState {

	// This map allows us to retrieve the facetConfiguration associated with a
	// particular facet.
	protected final Map<Facet, UIFacet> uiFacets;
	// private final Multimap<Class<?>, FacetConfiguration>
	// facetConfigurationsBySourceType;
	// The existing facet groups (BioMed, Immunology, ...). These belong to the
	// state of a sessions because they can carry information about facet order
	// and such things.
	protected final List<UIFacetGroup> uiFacetGroups;
	protected final LabelStore labelStore;
	protected int selectedFacetGroupIndex;
	protected UIFacetGroup selectedFacetGroup;
	protected final Logger logger;

	public AbstractUserInterfaceState(Logger logger,
			Map<Facet, UIFacet> uiFacets,
			List<UIFacetGroup> uiFacetGroups,
			LabelStore labelStore) {
		this.logger = logger;
		this.uiFacets = uiFacets;
		this.uiFacetGroups = uiFacetGroups;
		this.labelStore = labelStore;
		this.selectedFacetGroupIndex = 0;
		this.selectedFacetGroup = uiFacetGroups
				.get(selectedFacetGroupIndex);
		
	}

	/**
	 * @param selectedFacetGroup
	 *            the selectedFacetGroup to set
	 */
	public void setSelectedFacetGroup(UIFacetGroup selectedFacetGroup) {
		this.selectedFacetGroup = selectedFacetGroup;
		for (int i = 0; i < uiFacetGroups.size(); i++) {
			if (uiFacetGroups.get(i) == selectedFacetGroup)
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
		this.selectedFacetGroup = uiFacetGroups
				.get(selectedFacetGroupIndex);
	}

	/**
	 * @return the labelStore
	 */
	public LabelStore getLabelStore() {
		return labelStore;
	}

	public Collection<UIFacetGroup> getFacetGroups() {
		return uiFacetGroups;
	}

	/**
	 * @return the facetConfigurations
	 */
	public Map<Facet, UIFacet> getUIFacets() {
		return uiFacets;
	}

	/**
	 * @return the selectedFacetGroup
	 */
	public UIFacetGroup getSelectedFacetGroup() {
		return selectedFacetGroup;
	}

	/**
	 * 
	 */
	public void clear() {
		labelStore.clear();
		
	}

	public void refresh() {}

	public void reset() {
		for (UIFacetGroup facetGroup : uiFacetGroups)
			facetGroup.reset();
		selectedFacetGroupIndex = 0;
		selectedFacetGroup = uiFacetGroups
				.get(selectedFacetGroupIndex);
		labelStore.reset();
	}
	

	//set facet on the top and group all other facets
	public void setFirstFacet(FacetGroup<UIFacet> group, UIFacet facet){
		for(UIFacet f: group){
			f.setPosition(f.getPosition() + 1);
		}
		facet.setPosition(0);
		Collections.sort(group);
	}
	
	public UIFacet getUIFacet(String facetId) {
		for (UIFacet facet : uiFacets.values()) {
			if (facet.getId().equals(facetId))
				return facet;
		}
		throw new IllegalArgumentException("The facet with ID " + facetId + " does not exist.");
	}

}
