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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.ioc.annotations.Symbol;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.search.IFacetedSearchService;

/**
 * @author faessler
 * 
 */
public class UserInterfaceState {

	// This map allows us to retrieve the facetConfiguration associated with a
	// particular facet.
	private final Map<Facet, FacetConfiguration> facetConfigurations;
	// private final Multimap<Class<?>, FacetConfiguration>
	// facetConfigurationsBySourceType;
	// The existing facet groups (BioMed, Immunology, ...). These belong to the
	// state of a sessions because they can carry information about facet order
	// and such things.
	private final List<FacetGroup<FacetConfiguration>> facetConfigurationGroups;
	private final FacetHit facetHit;
	private int selectedFacetGroupIndex;
	private FacetGroup<FacetConfiguration> selectedFacetGroup;
	private final ITermService termService;
	private final IFacetedSearchService searchService;

	public UserInterfaceState(ITermService termService,
			IFacetedSearchService searchService,
			Map<Facet, FacetConfiguration> facetConfigurations,
			List<FacetGroup<FacetConfiguration>> facetConfigurationGroups,
			FacetHit facetHit) {
		this.termService = termService;
		this.searchService = searchService;
		this.facetConfigurations = facetConfigurations;
		this.facetConfigurationGroups = facetConfigurationGroups;
		this.facetHit = facetHit;
		this.selectedFacetGroupIndex = 0;
		this.selectedFacetGroup = facetConfigurationGroups
				.get(selectedFacetGroupIndex);

	}

	/**
	 * @return the selectedFacetGroup
	 */
	public FacetGroup<FacetConfiguration> getSelectedFacetGroup() {
		return selectedFacetGroup;
	}

	/**
	 * @param selectedFacetGroup
	 *            the selectedFacetGroup to set
	 */
	public void setSelectedFacetGroup(
			FacetGroup<FacetConfiguration> selectedFacetGroup) {
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
	 * @return the facetHit
	 */
	public FacetHit getFacetHit() {
		return facetHit;
	}

	public Collection<FacetGroup<FacetConfiguration>> getFacetGroups() {
		return facetConfigurationGroups;
	}

	/**
	 * @return the facetConfigurations
	 */
	public Map<Facet, FacetConfiguration> getFacetConfigurations() {
		return facetConfigurations;
	}

	/**
	 * <p>
	 * Makes labels for the terms displayed in the FacetBox components rendered
	 * for the selected FacetGroup. Already existing labels with up-to-date
	 * count values are re-used.
	 * </p>
	 * <p>
	 * This is accomplished by calling
	 * </code>createLabelsForFacet(FacetConfiguration)</code> for all facet
	 * configurations in the currently selected facet group.
	 * </p>
	 * 
	 * @see {@link #createLabelsForFacet(FacetConfiguration)}
	 */
	public void createLabelsForSelectedFacetGroup() {
		Map<FacetConfiguration, Collection<IFacetTerm>> allDisplayedTerms = getDisplayedTermsInSelectedFacetGroup();
		searchService.queryAndStoreFacetCountsInSelectedFacetGroup(
				allDisplayedTerms, facetHit);
	}

	/**
	 * <p>
	 * Makes labels for the FacetBox component associated with
	 * facetConfiguration. Already existing labels with up-to-date count values
	 * are re-used.
	 * </p>
	 * <p>
	 * First, determines whether the children of the currently selected term in
	 * the face include terms for which we don't have facet counts yet. Then
	 * computes facet counts for new terms not counted before (e.g. in other
	 * facets or because the user had been visiting the current subtree before).
	 * </p>
	 * <p>
	 * This method is used when refreshing a <code>FacetBox</code>. E.g. when
	 * drilling up/down or selecting a term which has been selected before
	 * without triggering a new search (e.g. after drilling up).
	 * </p>
	 * 
	 * @param facetConfiguration
	 *            The facetConfiguration whose displayed term set has been
	 *            changed, e.g. by a drill-up.
	 */
	public void createLabelsForFacet(FacetConfiguration facetConfiguration) {
		HashMap<FacetConfiguration, Collection<IFacetTerm>> displayedTerms = new HashMap<FacetConfiguration, Collection<IFacetTerm>>();
		// 'getDisplayedTermsInFacet' might set facetConfiguration to
		// 'forcedToFlatFacetCounts'.
		addDisplayedTermsInFacet(displayedTerms, facetConfiguration);
		if (facetConfiguration.isHierarchical()
				&& !facetConfiguration.isForcedToFlatFacetCounts()) {

			Multimap<FacetConfiguration, IFacetTerm> newTerms = HashMultimap
					.create();

			Map<String, TermLabel> labelsHierarchical = facetHit
					.getLabelsHierarchical();
			for (IFacetTerm term : displayedTerms.get(facetConfiguration)) {
				if (!labelsHierarchical.containsKey(term.getId()))
					newTerms.put(facetConfiguration, term);
			}
			if (newTerms.size() > 0)
				searchService.queryAndStoreHierarchichalFacetCounts(newTerms,
						facetHit);
			prepareLabelsForFacet(facetConfiguration);
		} else {
			List<Label> labels = facetHit.getLabelsFlat().get(
					facetConfiguration.getFacet().getId());
			if (labels == null)
				searchService.queryAndStoreFlatFacetCounts(
						Lists.newArrayList(facetConfiguration), facetHit);
			facetHit.sortLabelsIntoFacet(facetConfiguration);
		}
	}

	/**
	 * Returns all terms contained in hierarchical facets which will be
	 * displayed in the currently rendered FacetBox components.
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
	public Map<FacetConfiguration, Collection<IFacetTerm>> getDisplayedTermsInSelectedFacetGroup() {
		Map<FacetConfiguration, Collection<IFacetTerm>> displayedTermsByFacet = new HashMap<FacetConfiguration, Collection<IFacetTerm>>();
		for (FacetConfiguration facetConfiguration : selectedFacetGroup
				.getTaxonomicalElements()) {

			addDisplayedTermsInFacet(displayedTermsByFacet, facetConfiguration);
		}
		return displayedTermsByFacet;
	}

	/**
	 * <p>
	 * Stores all terms contained in the facet of
	 * <code>facetConfiguration</code> which will be displayed in the associated
	 * FacetBox component in <code>displayedTermsByFacet</code>, if this facet
	 * is hierarchical.
	 * </p>
	 * <p>
	 * However, if there are too many terms to display and thus too many terms
	 * to query Solr for (http header restriction and data transfer time), no
	 * terms are stored and <code>facetConfiguration</code> is set to
	 * 'forcedToFlatFacetCounts'.<br/>
	 * The FacetBox component will still display a hierarchy but only terms
	 * which are to be displayed and have been included in the top N frequency
	 * term list returned by Solr will actually be rendered.
	 * </p>
	 * 
	 * @param displayedTermsByFacet
	 * @param facetConfiguration
	 */
	private void addDisplayedTermsInFacet(
			Map<FacetConfiguration, Collection<IFacetTerm>> displayedTermsByFacet,
			FacetConfiguration facetConfiguration) {
		if (facetConfiguration.getFacet().isFlat())
			return;

		Collection<IFacetTerm> terms = facetConfiguration
				.getRootTermsForCurrentlySelectedSubTree();

		if (terms.size() > 100) {
			facetConfiguration.setForcedToFlatFacetCounts(true);
			return;
		}
		displayedTermsByFacet.put(facetConfiguration, terms);
	}

	/**
	 * <p>
	 * Makes labels for the children of currently displayed terms in the
	 * FacetBox components of the selected FacetGroup and puts them into the
	 * FacetBox components. Existing labels with up-to-date count values are
	 * re-used.
	 * </p>
	 * <p>
	 * This is necessary to indicate whether terms currently exposed to the user
	 * have child hits or not.
	 * </p>
	 * Firstly, the children of roots actually exposed to the user in the
	 * currently selected term-subtrees of all <code>facetConfiguration</code>s
	 * in the currently selected facet group are determined. Then, these
	 * children's facet counts for which we not yet know these counts are
	 * computed. This step is necessary to be able to indicate whether the root
	 * terms have child hits in their facet or not.
	 * <p>
	 * The roots themselves must have been queried before.
	 * </p>
	 */
	public boolean prepareLabelsForSelectedFacetGroup() {

		// Until now, there are Labels for the facet roots but they have not yet
		// sorted into the DisplayGroups. Do it now so we can determine which
		// terms are actually seen.
		for (FacetConfiguration facetConfiguration : selectedFacetGroup)
			facetHit.sortLabelsIntoFacet(facetConfiguration);

		Multimap<FacetConfiguration, IFacetTerm> termsToUpdate = HashMultimap
				.create();
		for (FacetConfiguration facetConfiguration : selectedFacetGroup)
			facetHit.storeUnknownChildrenOfDisplayedTerms(facetConfiguration,
					termsToUpdate);
		
		if (termsToUpdate.size() > 0) {
			searchService.queryAndStoreHierarchichalFacetCounts(termsToUpdate,
					facetHit);
			return true;
		}
		return false;
	}

	/**
	 * <p>
	 * Makes labels for the children of currently displayed terms in the
	 * FacetBox component related to <code>facetConfiguration</code>. Existing
	 * labels with up-to-date count values are re-used.
	 * </p>
	 * <p>
	 * Determines the children of roots actually exposed to the user in the
	 * currently selected term-subtree of <code>facetConfiguration</code> and
	 * computes the children's facet counts for which we not yet know these
	 * counts. This step is necessary to be able to indicate whether the root
	 * terms have child hits in their facet or not.
	 * </p>
	 * 
	 * @param facetConfiguration
	 *            The facet configuration for whose currently selected subtree
	 *            the child counts shall be computed.
	 */
	private void prepareLabelsForFacet(FacetConfiguration facetConfiguration) {
		facetHit.sortLabelsIntoFacet(facetConfiguration);
		Multimap<FacetConfiguration, IFacetTerm> termsToUpdate = HashMultimap
				.create();
		facetHit.storeUnknownChildrenOfDisplayedTerms(facetConfiguration,
				termsToUpdate);
		if (termsToUpdate.size() > 0)
			searchService.queryAndStoreHierarchichalFacetCounts(termsToUpdate,
					facetHit);
	}

	/**
	 * 
	 */
	public void clear() {
		facetHit.clear();
	}

	public void refresh() {
		for (FacetConfiguration configuration : facetConfigurations.values()) {
			configuration.refresh();
		}
	}

	public void reset() {
		for (FacetConfiguration configuration : facetConfigurations.values())
			configuration.reset();
		selectedFacetGroupIndex = 0;
		selectedFacetGroup = facetConfigurationGroups
				.get(selectedFacetGroupIndex);
		facetHit.reset();
	}

}
