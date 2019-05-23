/**
 * IUIService.java
 *
 * Copyright (c) 2013, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 06.04.2013
 **/

/**
 * 
 */
package de.julielab.semedico.core.services.interfaces;

import java.util.List;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.search.components.data.LabelStore;

/**
 * @author faessler
 * 
 */
public interface IUIService {

	/**
	 * Returns all terms contained in hierarchical facets which will be
	 * displayed in the currently rendered FacetBox components. May force
	 * hierarchical <tt>UIFacets</tt> to flat facet counts, if there are too
	 * many terms displayed in that facet.
	 * <p>
	 * Thus, all terms for which Labels must be present for displaying purposes
	 * (frequency ordering and actual display) are returned.
	 * </p>
	 * <p>
	 * The displayed terms are determined as follows:
	 * <ul>
	 * <li>If the facet is not drilled down, i.e. the user did not select any
	 * term of this facet and did not enter a search term associated with the
	 * facet, the facet root IDs are returned for this facet.<br>
	 * <li>If <code>facetConfiguration</code> is drilled down, i.e. there is a
	 * path of length greater than zero from a root term of the facet to a
	 * user-selected inner term, the root terms of the user-selected subtree are
	 * returned.
	 * </ul>
	 * </p>
	 * 
	 * @return All currently viewable terms, associated with their corresponding
	 *         facetConfiguration.
	 */
	public Multimap<UIFacet, String> getDisplayedTermsInFacetGroup(
			List<UIFacet> facetGroup);
	
	public void sortLabelsIntoFacet(LabelStore labelStore, UIFacet uiFacet);
	
	public void sortLabelsIntoFacets(LabelStore labelStore, Iterable<UIFacet> uiFacets);

}
