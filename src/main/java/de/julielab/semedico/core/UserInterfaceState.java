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
		this.selectedFacetGroup = facetConfigurationGroups.get(0);

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
	 * Computes all information necessary to display the facet terms contained
	 * in all facets of the currently selected facet group appropriately to the
	 * user.
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
		Map<FacetConfiguration, Set<IFacetTerm>> allDisplayedTerms = getAllTermsOnCurrentFacetLevelsInSelectedFacetGroup();
		searchService.queryAndStoreFacetCountsInSelectedFacetGroup(
				allDisplayedTerms, facetHit);
	}

	/**
	 * <p>
	 * Computes new, necessary information about the subtree roots of the
	 * currently selected facet. This includes label creation for exposed terms
	 * as well as for their children in order to indicate whether the roots have
	 * child hits.
	 * </p>
	 * <p>
	 * First, determines whether the currently selected facet subtree roots
	 * include terms for which we don't have facet counts yet. Then computes
	 * facet counts for new terms not counted before (e.g. in other facets or
	 * because the user had been visiting the current subtree before).
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
		if (facetConfiguration.isHierarchical()) {

			Multimap<FacetConfiguration, IFacetTerm> newTerms = HashMultimap
					.create();
			HashMap<FacetConfiguration, Set<IFacetTerm>> displayedTerms = new HashMap<FacetConfiguration, Set<IFacetTerm>>();

			getRootTermsForCurrentlySelectedSubTree(facetConfiguration,
					displayedTerms);

			Map<String, TermLabel> labelsHierarchical = facetHit
					.getLabelsHierarchical();
			for (IFacetTerm term : displayedTerms.get(facetConfiguration)) {
				if (!labelsHierarchical.containsKey(term.getId()))
					newTerms.put(facetConfiguration, term);
			}
			if (newTerms.size() > 0)
				searchService.queryAndStoreHierarchichalFacetCounts(newTerms,
						facetHit);
		} else {
			List<Label> labels = facetHit.getLabelsFlat().get(
					facetConfiguration.getFacet().getId());
			if (labels == null)
				searchService.queryAndStoreFlatFacetCounts(
						Lists.newArrayList(facetConfiguration), facetHit);
		}
		prepareLabelsForFacet(facetConfiguration);
	}

	/**
	 * Returns the IDs of all terms contained in hierarchical facets where the
	 * terms are located on the level currently selected by the user.
	 * <p>
	 * Thus, all term IDs for terms for which Labels must be present for
	 * displaying purposes (frequency ordering and actual display) are returned.
	 * </p>
	 * 
	 * @return All IDs of currently viewable hierarchical terms.
	 */
	public Map<FacetConfiguration, Set<IFacetTerm>> getAllTermsOnCurrentFacetLevelsInSelectedFacetGroup() {
		Map<FacetConfiguration, Set<IFacetTerm>> displayedTermIds = new HashMap<FacetConfiguration, Set<IFacetTerm>>();
		for (FacetConfiguration facetConfiguration : selectedFacetGroup
				.getFacetsBySourceType(Facet.FIELD_HIERARCHICAL)) {
			getRootTermsForCurrentlySelectedSubTree(facetConfiguration,
					displayedTermIds);
		}
		return displayedTermIds;
	}

	/**
	 * <p>
	 * Computes information necessary to indicate whether terms currently
	 * exposed to the user have child hits or not.
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
	public void prepareLabelsForSelectedFacetGroup() {

		for (FacetConfiguration facetConfiguration : selectedFacetGroup)
			facetHit.sortLabelsIntoFacet(facetConfiguration);

		Multimap<FacetConfiguration, IFacetTerm> termsToUpdate = HashMultimap
				.create();
		for (FacetConfiguration facetConfiguration : selectedFacetGroup)
			facetHit.storeUnknownChildrenOfDisplayedTerms(facetConfiguration,
					termsToUpdate);
		searchService.queryAndStoreHierarchichalFacetCounts(termsToUpdate,
				facetHit);
	}

	/**
	 * Determines the children of roots actually exposed to the user in the
	 * currently selected term-subtree of <code>facetConfiguration</code> and
	 * computes the children's facet counts for which we not yet know these
	 * counts. This step is necessary to be able to indicate whether the root
	 * terms have child hits in their facet or not.
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
	 * Called from FacetBox components for individual hierarchical facets when
	 * changes to the set of displayed labels occur. For new labels, child hits
	 * must be checked to be able to render a triangle when a label (or term)
	 * has child hits.
	 * 
	 * @param facetConfiguration
	 */
	// public void updateLabels(FacetConfiguration facetConfiguration) {
	// if (facetConfiguration.isFlat())
	// return;
	//
	// Multimap<FacetConfiguration, IFacetTerm> termsToUpdate = HashMultimap
	// .create();
	// facetHit.storeUnknownChildrenOfDisplayedTerms(facetConfiguration,
	// termsToUpdate);
	// searchService.queryAndStoreHierarchichalFacetCounts(termsToUpdate,
	// facetHit);
	// }

	/**
	 * Returns the terms contained in the facet of
	 * <code>facetConfiguration</code> which lie on the currently selected
	 * hierarchical level of this facet. If <code>facetConfiguration</code> is
	 * in flat state (i.e. non-hierarchical), this method returns immediately.
	 * Otherwise, the terms are stored into <code>displayedTermsCache</code>
	 * with <code>facetConfiguration</code> as key.
	 * <p>
	 * The terms on the current level are determined in the following way:
	 * <ul>
	 * <li>If the facet is not drilled down, i.e. the user did not select any
	 * term of this facet and did not enter a search term associated with the
	 * facet, the facet root IDs are added to
	 * <code>termStorageByFacetConfiguration</code>.<br>
	 * <li>If <code>facetConfiguration</code> is drilled down, i.e. there is a
	 * path of length greater than zero from a root term of the facet to a
	 * user-selected inner term, the root terms of the user-selected subtree are
	 * added to <code>termStorageByFacetConfiguration</code>.
	 * </ul>
	 * </p>
	 * 
	 * @param facetConfiguration
	 *            The facet of which to return all terms on the currently
	 *            selected hierarchy level.
	 * @param termStorageByFacetConfiguration
	 *            The map which associates the facetConfiguration with the root
	 *            terms of its currently selected subtree.
	 * @return
	 */
	private void getRootTermsForCurrentlySelectedSubTree(
			FacetConfiguration facetConfiguration,
			Map<FacetConfiguration, Set<IFacetTerm>> termStorageByFacetConfiguration) {
		// Flat facets do not need to bother with term IDs as their labels are
		// just linearly ordered by frequency.
		if (facetConfiguration.getFacet().isFlat())
			return;

		Set<IFacetTerm> termSet = new HashSet<IFacetTerm>();
		termStorageByFacetConfiguration.put(facetConfiguration, termSet);
		if (facetConfiguration.isDrilledDown()) {
			IFacetTerm lastPathTerm = facetConfiguration.getLastPathElement();
			for (IFacetTerm child : lastPathTerm.getAllChildren()) {
				if (child.isContainedInFacet(facetConfiguration.getFacet())) {
					termSet.add(child);
				}
			}

		} else {
			Iterator<IFacetTerm> rootIt = termService.getFacetRoots(
					facetConfiguration.getFacet()).iterator();
			while (rootIt.hasNext()) {
				IFacetTerm root = rootIt.next();
				termSet.add(root);
			}
		}
	}

	// public List<IFacetTerm> getAllDisplayedTermsInHierarchicalFacets() {
	// Collection<FacetConfiguration> facetConfigurationsInHierarchicalState =
	// selectedFacetGroup.getFacetsBySourceType(Facet.FIELD_HIERARCHICAL);
	// List<IFacetTerm> allDisplayedTerms = new ArrayList<IFacetTerm>();
	// for (FacetConfiguration facetConfiguration :
	// facetConfigurationsInHierarchicalState) {
	// List<Label> labels =
	// facetHit.getLabelsForFacetOnCurrentLevel(facetConfiguration);
	// for (int i = 0; i < numberOfLabelsToDisplay; i++) {
	// allDisplayedTerms.add(((TermLabel) labels.get(i)).getTerm());
	// }
	// }
	// return allDisplayedTerms;
	// }

	// public Collection<FacetConfiguration>
	// getFacetConfigurationBySourceType(Class<? extends Facet.SourceType>
	// clazz) {
	// return facetConfigurationsBySourceType.get(clazz);
	// }

	public void reset() {
		for (FacetConfiguration configuration : facetConfigurations.values())
			configuration.reset();
		selectedFacetGroupIndex = 0;
		selectedFacetGroup = facetConfigurationGroups.get(0);
		facetHit.clear();
	}

}
