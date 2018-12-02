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
package de.julielab.semedico.core.entities.state;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.facets.UIFacetGroup;
import de.julielab.semedico.core.search.components.data.LabelStore;

/**
 * @author faessler
 * 
 */
public class UserInterfaceState extends AbstractUserInterfaceState {

	public UserInterfaceState(Logger logger, Map<Facet, UIFacet> uiFacets, List<UIFacetGroup> uiFacetGroups,
			LabelStore labelStore) {
		super(logger, uiFacets, uiFacetGroups, labelStore);
	}

}
