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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.ITermService;
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
	private final Map<FacetConfiguration, Set<IFacetTerm>> displayedTermsCache;
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

		// facetConfigurationsBySourceType = HashMultimap.create();
		// for (Entry<Facet, FacetConfiguration> entry : facetConfigurations
		// .entrySet()) {
		// Facet facet = entry.getKey();
		// FacetConfiguration facetConfiguration = entry.getValue();
		//
		// Class<?> facetSourceClass = facet.getSource().getClass();
		// while (facetSourceClass != null
		// && !facetSourceClass.getName().equals("java.lang.Object")) {
		// facetConfigurationsBySourceType.put(facetSourceClass,
		// facetConfiguration);
		// facetSourceClass = facetSourceClass.getSuperclass();
		// }
		// }

		this.displayedTermsCache = new HashMap<FacetConfiguration, Set<IFacetTerm>>();
		for (List<FacetConfiguration> facetConfigurationGroup : facetConfigurationGroups) {
			for (FacetConfiguration facetConfiguration : facetConfigurationGroup) {
				// Non-hierarchical facets have no need to manage any displayed
				// term IDs. For flat facets, term are just ordered linearly
				// according to their frequencies.
				if (facetConfiguration.getFacet().isHierarchical())
					this.displayedTermsCache.put(facetConfiguration,
							new HashSet<IFacetTerm>());
			}
		}
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

	public void createLabelsForSelectedFacetGroup() {
		Map<FacetConfiguration, Set<IFacetTerm>> allDisplayedTerms = getDisplayedHierarchicalTermIdsForCurrentFacetGroup();
		searchService.queryAndStoreFacetCountsInSelectedFacetGroup(allDisplayedTerms, facetHit);
	}

	/**
	 * Used when refreshing a FacetBox. E.g. when drilling up/down.
	 * 
	 * @param facetConfiguration
	 */
	public void createLabelsForFacet(
			FacetConfiguration facetConfiguration) {

		if (facetConfiguration.isHierarchical()) {
			Multimap<FacetConfiguration, IFacetTerm> newIds = HashMultimap
					.create();
			
			Map<FacetConfiguration, Set<IFacetTerm>> displayedTerms = getDisplayedTermsForFacet(
					facetConfiguration, displayedTermsCache);
			Map<String, Label> labelsHierarchical = facetHit
					.getLabelsHierarchical();

			for (IFacetTerm id : displayedTerms.get(facetConfiguration)) {
				if (!labelsHierarchical.containsKey(id))
					newIds.put(facetConfiguration, id);
			}
			if (newIds.size() > 0)
				searchService.queryAndStoreHierarchichalFacetCounts(newIds,
						facetHit);
		}
		else {
			List<Label> labels = facetHit.getLabelsFlat().get(facetConfiguration.getFacet().getId());
			if (labels == null)
				searchService.queryAndStoreFlatFacetCounts(Lists.newArrayList(facetConfiguration), facetHit);
			
		}
	}

	/**
	 * @return the facetHit
	 */
	public FacetHit getFacetHit() {
		return facetHit;
	}

	/**
	 * @return the facetConfigurations
	 */
	public Map<Facet, FacetConfiguration> getFacetConfigurations() {
		return facetConfigurations;
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
	public Map<FacetConfiguration, Set<IFacetTerm>> getDisplayedHierarchicalTermIdsForCurrentFacetGroup() {
		Map<FacetConfiguration, Set<IFacetTerm>> displayedTermIds = this.displayedTermsCache;
		for (FacetConfiguration facetConfiguration : selectedFacetGroup
				.getFacetsBySourceType(Facet.FIELD_HIERARCHICAL)) {
			getDisplayedTermsForFacet(facetConfiguration, displayedTermIds);
		}
		return displayedTermIds;
	}

	/**
	 * Returns the IDs of terms contained in <code>Facet</code> which lie on the
	 * currently selected hierarchical level of this facet. If
	 * <code>Facet</code> is flat (i.e. non-hierarchical), the exact same map
	 * <code>displayedTermIds</code> is returned which has been passed to the
	 * method.
	 * <p>
	 * Displayed terms are determined in the following way:
	 * <ul>
	 * <li>If the facet <code>Facet</code> is not drilled down, i.e. the user
	 * did not select any term of this facet and did not enter a search term
	 * associated with the facet, the facet root IDs are added to
	 * <code>displayedTermIds</code>.<br>
	 * <li>If the facet is drilled down, i.e. there is a path of length greater
	 * than zero from a root term of the facet, the IDs of child terms of the
	 * last term on this path are added.
	 * </ul>
	 * </p>
	 * 
	 * @param facet
	 *            The facet of which to return IDs of currently displayed terms.
	 * @param displayedTerms
	 *            The map which associates the facet which its displayed terms'
	 *            IDs.
	 * @return
	 */
	private Map<FacetConfiguration, Set<IFacetTerm>> getDisplayedTermsForFacet(
			FacetConfiguration facetConfiguration,
			Map<FacetConfiguration, Set<IFacetTerm>> displayedTerms) {
		// Flat facets do not need to bother with term IDs as their labels are
		// just linearly ordered by frequency.
		if (facetConfiguration.getFacet().isFlat())
			return displayedTerms;

		Set<IFacetTerm> idSet = displayedTerms.get(facetConfiguration);
		if (facetConfiguration.isDrilledDown()) {
			IFacetTerm lastPathTerm = facetConfiguration.getLastPathElement();
			Iterator<IFacetTerm> childIt = lastPathTerm.childIterator();
			while (childIt.hasNext()) {
				IFacetTerm child = childIt.next();
				if (child.isContainedInFacet(facetConfiguration.getFacet()))
					idSet.add(child);
			}

		} else {
			Iterator<IFacetTerm> rootIt = termService.getFacetRoots(
					facetConfiguration.getFacet()).iterator();
			while (rootIt.hasNext()) {
				IFacetTerm root = rootIt.next();
				if (root.isContainedInFacet(facetConfiguration.getFacet()))
					idSet.add(root);
			}
		}
		return displayedTerms;
	}

	// public Collection<FacetConfiguration>
	// getFacetConfigurationBySourceType(Class<? extends Facet.SourceType>
	// clazz) {
	// return facetConfigurationsBySourceType.get(clazz);
	// }

	public void reset() {
		for (FacetConfiguration configuration : facetConfigurations.values())
			configuration.reset();
		for (Set<IFacetTerm> idSet : displayedTermsCache.values())
			idSet.clear();
		selectedFacetGroupIndex = 0;
		selectedFacetGroup = facetConfigurationGroups.get(0);
		facetHit.clear();
	}
}
