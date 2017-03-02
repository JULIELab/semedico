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

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.search.components.data.LabelStore;

/**
 * @author faessler
 * 
 */
public interface IUIService {

	/**
	 * Stores all children of terms associated with the labels in
	 * <code>displayedLabels</code> which have not yet been counted.
	 * 
	 * @param uiFacet
	 * @param displayedLabels
	 * @deprecated Not used any more because of performance issues
	 */
	@Deprecated
	public void storeUnknownChildrenOfDisplayedTerms(UIFacet uiFacet,
			Multimap<String, String> termsToUpdate, LabelStore labelStore);

	/**
	 * Returns all terms contained in hierarchical facets which will be
	 * displayed in the currently rendered FacetBox components. May force
	 * hierarchical <tt>UIFacets</tt> to flat facet counts, if there are too
	 * many terms displayed in that facet (see {@link
	 * #addDisplayedTermsInFacet(Multimap, UIFacet)} for more information).
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
	 * @see #addDisplayedTermsInFacet(Multimap, UIFacet)
	 */
	public Multimap<UIFacet, String> getDisplayedTermsInFacetGroup(
			List<UIFacet> facetGroup);

//	/**
//	 * <p>
//	 * Stores all terms contained in the facet of
//	 * <code>facetConfiguration</code> which will be displayed in the associated
//	 * FacetBox component in <code>displayedTermsByFacet</code>, if this facet
//	 * is hierarchical.
//	 * </p>
//	 * <p>
//	 * However, if there are too many terms to display and thus too many terms
//	 * to query Solr for (http header restriction and data transfer time), no
//	 * terms are stored and <code>facetConfiguration</code> is set to
//	 * 'forcedToFlatFacetCounts'.<br/>
//	 * The FacetBox component will still display a hierarchy but only terms
//	 * which are to be displayed and have been included in the top N frequency
//	 * term list returned by Solr will actually be rendered.
//	 * </p>
//	 * 
//	 * @param displayedTermsByFacet
//	 * @param facetConfiguration
//	 */
//	public void addDisplayedTermsInFacet(
//			Multimap<UIFacet, String> displayedTermsByFacet,
//			UIFacet facetConfiguration);
	
	/**
	 * <p>
	 * Retrieves the labels which should currently be ready to display for the
	 * <code>FacetBox</code> component associated with
	 * <code>facetConfiguration</code> and puts them into the
	 * <code>displayGroup</code> object associated with this
	 * <code>FacetBox</code>.
	 * </p>
	 * <p>
	 * For facets in hierarchical mode, these labels are the roots of the
	 * currently selected term-subtree in this facet. That is, the children of
	 * the last term on the drill-down-path of <code>facetConfiguration</code>
	 * or the facet roots if the facet is not drilled down at all.
	 * </p>
	 * <p>
	 * For facets in flat mode, these labels are given by the sorted list of
	 * labels retrieved from the facet's source (the top N terms in the Solr
	 * field associated with the facet).
	 * </p>
	 * 
	 * @param facetConfiguration
	 *            The <code>facetConfiguration</code> for whose
	 *            <code>FacetBox</code> the correct labels are to be determined
	 *            and filled into the <code>DisplayGroup</code> meant for this
	 *            <code>FacetBox</code>.
	 */
	public void sortLabelsIntoFacet(LabelStore labelStore, UIFacet uiFacet);
	
	@Deprecated
	public void resolveChildHitsRecursively(LabelStore labelStore);

	public void sortLabelsIntoFacets(LabelStore labelStore, Iterable<UIFacet> uiFacets);

	/**
	 * Get the field value filter expression for <tt>concept</tt> in association with facet <tt>facet</tt>. For example,
	 * if <tt>concept</tt> is the <em>phosphorylation</em> event type term and <tt>facet</tt> is the event facet, we
	 * want a filter expression to get back all phosphorylation events. If <tt>facet</tt> is the <em>Gene Ontology</em>,
	 * we don't want to retrieve the actual events but the ontology class children of the phosophorylation class. TODO:
	 * this is currently extremely hard coded, perhaps it can be made more configurable and more general
	 */
	String getFlatFieldValueFilterExpression(IConcept concept, Facet facet);

	/**
	 * Returns field names and filter expression for them to only return those terms that are necessary to build the
	 * required term children
	 */
	Multimap<UIFacet, String> getFacetFilterExpressions(List<UIFacet> facets);
}
