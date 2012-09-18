/**
 * BTermUserInterfaceState.java
 *
 * Copyright (c) 2012, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 10.07.2012
 **/

/**
 * 
 */
package de.julielab.semedico.core;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import de.julielab.semedico.search.interfaces.IFacetedSearchService;

/**
 * Mainly a type of its own to allow Tapestry to manage to UserInterface SSOs
 * (because these are identified by type).
 * 
 * @author faessler
 * 
 */
public class BTermUserInterfaceState extends UserInterfaceState {
	public BTermUserInterfaceState(Logger logger, IFacetedSearchService searchService,
			Map<Facet, FacetConfiguration> facetConfigurations,
			List<FacetGroup<FacetConfiguration>> facetConfigurationGroups,
			LabelStore labelStore, SearchState searchState) {
		super(logger, searchService, facetConfigurations, facetConfigurationGroups,
				labelStore, searchState);
	}

	/* (non-Javadoc)
	 * @see de.julielab.semedico.core.UserInterfaceState#createLabelsForFacet(de.julielab.semedico.core.FacetConfiguration)
	 */
	@Override
	public void createLabelsForFacet(FacetConfiguration facetConfiguration) {
		if (facetConfiguration.isFlat()) {
			logger.trace("Facet \"{}\" is flat, no new labels are created.", facetConfiguration.getFacet().getName());
			return;
		}
		
		logger.trace("Creating new labels for facet {}.", facetConfiguration.getFacet().getName());
		labelStore.sortLabelsIntoFacet(facetConfiguration);
		
	}
}
