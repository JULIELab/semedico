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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.exceptions.IncompatibleStructureException;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.search.interfaces.IFacetedSearchService;

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
	private int selectedFacetGroupIndex;
	private FacetGroup<UIFacet> selectedFacetGroup;
	private Set<FacetGroup<UIFacet>> facetGroupsWithLabels;
	private final IFacetedSearchService searchService;
	protected final SearchState searchState;
	protected final Logger logger;

	public UserInterfaceState(Logger logger,
			IFacetedSearchService searchService,
			Map<Facet, UIFacet> facetConfigurations,
			List<FacetGroup<UIFacet>> facetConfigurationGroups,
			LabelStore labelStore, SearchState searchState) {
		this.logger = logger;
		this.searchService = searchService;
		this.facetConfigurations = facetConfigurations;
		this.facetConfigurationGroups = facetConfigurationGroups;
		this.labelStore = labelStore;
		this.searchState = searchState;
		this.selectedFacetGroupIndex = 0;
		this.selectedFacetGroup = facetConfigurationGroups
				.get(selectedFacetGroupIndex);
		this.facetGroupsWithLabels = new HashSet<FacetGroup<UIFacet>>();
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
	 * @see {@link #createLabelsForFacet(UIFacet)}
	 */
	public void createLabelsForSelectedFacetGroup() {
		logger.trace("Creating labels for selected facet group.");
		long time = System.currentTimeMillis();
		if (!facetGroupsWithLabels.contains(selectedFacetGroup)) {
			Map<UIFacet, Collection<IFacetTerm>> allDisplayedTerms = getDisplayedTermsInSelectedFacetGroup();
			searchService.queryAndStoreFacetCountsInSelectedFacetGroup(
					searchState.getSolrQueryString(), allDisplayedTerms,
					labelStore);
			logger.info("Creating labels for selected facet group took {} ms.",
					System.currentTimeMillis() - time);
			prepareLabelsForSelectedFacetGroup();
		} else
			logger.info(
					"Labels for this facet group already exist. Passed time: {} ms.",
					System.currentTimeMillis() - time);
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
	public void createLabelsForFacet(UIFacet facetConfiguration) {
		HashMap<UIFacet, Collection<IFacetTerm>> displayedTerms = new HashMap<UIFacet, Collection<IFacetTerm>>();
		// 'getDisplayedTermsInFacet' might set facetConfiguration to
		// 'forcedToFlatFacetCounts'. Thus, it must be called before the 'if'.
		addDisplayedTermsInFacet(displayedTerms, facetConfiguration);
		if (facetConfiguration.isInHierarchicViewMode()
				&& !facetConfiguration.isForcedToFlatFacetCounts()) {

			Multimap<UIFacet, IFacetTerm> newTerms = HashMultimap.create();

			Map<String, TermLabel> labelsHierarchical = labelStore
					.getLabelsHierarchical();
			for (IFacetTerm term : displayedTerms.get(facetConfiguration)) {
				if (!labelsHierarchical.containsKey(term.getId()))
					newTerms.put(facetConfiguration, term);
			}
			if (newTerms.size() > 0)
				searchService.queryAndStoreHierarchichalFacetCounts(
						searchState.getSolrQueryString(), newTerms, labelStore);
			prepareLabelsForFacet(facetConfiguration);
		} else {
			List<Label> labels = labelStore.getFlatLabels(facetConfiguration);
			if (labels == null) {
				searchService.queryAndStoreFlatFacetCounts(
						searchState.getSolrQueryString(),
						Lists.newArrayList(facetConfiguration), labelStore);
			}
			labelStore.sortLabelsIntoFacet(facetConfiguration);
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
	public Map<UIFacet, Collection<IFacetTerm>> getDisplayedTermsInSelectedFacetGroup() {
		Map<UIFacet, Collection<IFacetTerm>> displayedTermsByFacet = new HashMap<UIFacet, Collection<IFacetTerm>>();
		for (UIFacet facetConfiguration : selectedFacetGroup
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
			Map<UIFacet, Collection<IFacetTerm>> displayedTermsByFacet,
			UIFacet facetConfiguration) {
		if (facetConfiguration.isFlat())
			return;

		Collection<IFacetTerm> terms = facetConfiguration
				.getRootTermsForCurrentlySelectedSubTree();

		// TODO Magic number
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
		logger.trace("Creating labels for children of displayed facet group terms.");
		long time = System.currentTimeMillis();
		// If document search or only facet retrieval has been done first, this
		// method is always the end of label creation for the facet group.
		facetGroupsWithLabels.add(selectedFacetGroup);
		// Until now, there are Labels for the facet roots but they have not
		// yet
		// sorted into the DisplayGroups. Do it now so we can determine
		// which
		// terms are actually seen.
		for (UIFacet facetConfiguration : selectedFacetGroup)
			labelStore.sortLabelsIntoFacet(facetConfiguration);

		Multimap<UIFacet, IFacetTerm> termsToUpdate = HashMultimap.create();
		for (UIFacet facetConfiguration : selectedFacetGroup)
			labelStore.storeUnknownChildrenOfDisplayedTerms(facetConfiguration,
					termsToUpdate);

		if (termsToUpdate.size() > 0) {
			searchService
					.queryAndStoreHierarchichalFacetCounts(
							searchState.getSolrQueryString(), termsToUpdate,
							labelStore);
			logger.info(
					"Label creation for children of displayed facet group terms took {} ms.",
					System.currentTimeMillis() - time);
			return true;
		}
		logger.info(
				"Label creation for children of displayed facet group terms: No children to create or update ({} ms).",
				System.currentTimeMillis() - time);
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
	private void prepareLabelsForFacet(UIFacet facetConfiguration) {
		labelStore.sortLabelsIntoFacet(facetConfiguration);
		Multimap<UIFacet, IFacetTerm> termsToUpdate = HashMultimap.create();
		labelStore.storeUnknownChildrenOfDisplayedTerms(facetConfiguration,
				termsToUpdate);
		if (termsToUpdate.size() > 0)
			searchService
					.queryAndStoreHierarchichalFacetCounts(
							searchState.getSolrQueryString(), termsToUpdate,
							labelStore);
	}

	/**
	 * 
	 */
	public void clear() {
		labelStore.clear();
		facetGroupsWithLabels.clear();
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
		facetGroupsWithLabels.clear();
	}
	

	//set facet on the top and group all other facets
	public void setFirstFacet(FacetGroup<UIFacet> group, UIFacet facet){
		for(UIFacet f: group){
			f.setPosition(f.position + 1);
		}
		facet.setPosition(0);
		Collections.sort(selectedFacetGroup);
	}


}
