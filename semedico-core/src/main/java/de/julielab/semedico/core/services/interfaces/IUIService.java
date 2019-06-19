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

import com.google.common.collect.Multimap;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.search.components.data.LabelStore;

import java.util.List;

/**
 * @author faessler
 * 
 */
public interface IUIService {


	/**
	 * Returns all IDs of concepts contained in hierarchical facets which will be
	 * displayed in the currently rendered FacetBox components. May force
	 * hierarchical <tt>UIFacets</tt> to flat facet counts, if there are too
	 * many terms displayed in that facet.
	 * <p>
	 * Thus, all concepts for which labels must be present for displaying purposes
	 * (frequency ordering and actual display) are returned.
	 * </p>
	 * <p>
	 * The displayed concepts are determined as follows:
	 * <ul>
	 * <li>If the facet is not drilled down, i.e. the user did not select any
	 * concept of this facet and did not enter a search concept associated with the
	 * facet, the facet root IDs are returned for this facet.<br>
	 * <li>If <code>uiFacet</code> is drilled down, i.e. there is a
	 * path of length greater than zero from a root concept of the facet to a
	 * user-selected inner concept, the root concepts of the user-selected subtree are
	 * returned.
	 * </ul>
	 * </p>
	 * 
	 * @return All currently viewable concepts, associated with their corresponding
	 *         uiFacet.
	 */
	public Multimap<UIFacet, String> getDisplayedConceptIdsInFacetGroup(
			List<UIFacet> facetGroup);


	/**
	 * <p>
	 * Retrieves the labels which should currently be ready to display for the
	 * <code>FacetBox</code> component associated with
	 * <code>uiFacet</code> and puts them into the
	 * <code>displayGroup</code> object associated with this
	 * <code>FacetBox</code>.
	 * </p>
	 * <p>
	 * For facets in hierarchical mode, these labels are the roots of the
	 * currently selected concept-subtree in this facet. That is, the children of
	 * the last concept on the drill-down-path of <code>uiFacet</code>
	 * or the facet roots if the facet is not drilled down at all.
	 * </p>
	 * <p>
	 * For facets in flat mode, these labels are given by the sorted list of
	 * labels retrieved from the facet's facetSource (the top N terms in the Solr
	 * field associated with the facet).
	 * </p>
	 * 
	 * @param uiFacet
	 *            The <code>uiFacet</code> for whose
	 *            <code>FacetBox</code> the correct labels are to be determined
	 *            and filled into the <code>DisplayGroup</code> meant for this
	 *            <code>FacetBox</code>.
	 */
	public void sortLabelsIntoFacet(LabelStore labelStore, UIFacet uiFacet);
	
	public void sortLabelsIntoFacets(LabelStore labelStore, Iterable<UIFacet> uiFacets);

}
