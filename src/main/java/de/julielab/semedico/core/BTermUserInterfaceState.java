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

/**
 * Mainly a type of its own to allow Tapestry to manage to UserInterface SSOs
 * (because these are identified by type).
 * 
 * @author faessler
 * 
 */
public class BTermUserInterfaceState extends UserInterfaceState {
	public BTermUserInterfaceState(Logger logger,
			Map<Facet, UIFacet> facetConfigurations,
			List<FacetGroup<UIFacet>> facetConfigurationGroups,
			LabelStore labelStore, SearchState searchState) {
		super(logger, facetConfigurations, facetConfigurationGroups,
				labelStore, searchState);
	}

}
