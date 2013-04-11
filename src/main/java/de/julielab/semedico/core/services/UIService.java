/**
 * UIService.java
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
package de.julielab.semedico.core.services;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.services.interfaces.IUIService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;

/**
 * @author faessler
 * 
 */
public class UIService implements IUIService {

	private final Logger log;

	public UIService(Logger log) {
		this.log = log;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.IUIService#
	 * storeUnknownChildrenOfDisplayedTerms(de.julielab.semedico.core.UIFacet,
	 * com.google.common.collect.Multimap, de.julielab.semedico.core.LabelStore)
	 */
	public void storeUnknownChildrenOfDisplayedTerms(UIFacet uiFacet,
			Multimap<String, String> termsToUpdate, LabelStore labelStore) {

		if (uiFacet.isFlat())
			return;

		Set<Label> fullyUpdatedLabelSet = labelStore.fullyUpdatedLabelSets
				.get(uiFacet);

		if (fullyUpdatedLabelSet == null) {
			fullyUpdatedLabelSet = new HashSet<Label>();
			labelStore.fullyUpdatedLabelSets.put(uiFacet, fullyUpdatedLabelSet);
		}

		List<Label> displayedLabels = uiFacet.getLabelDisplayGroup()
				.getDisplayedObjects();
		for (Label label : displayedLabels) {
			if (!fullyUpdatedLabelSet.contains(label)) {
				IFacetTerm term = ((TermLabel) label).getTerm();
				// Only prepare up to 10 (TODO!! MN...) children. E.g. organic
				// chemicals has 688 children which is a bit much to query
				// one-by-one (it works but slows things down).
				for (int i = 0; i < 10 && i < term.getNumberOfChildren(); i++) {
					IFacetTerm child = term.getChild(i);
					boolean childInLabelsHierarchical = labelStore.labelsHierarchical
							.containsKey(child.getId());
					boolean childInFacet = child.isContainedInFacet(uiFacet);
					if (!childInLabelsHierarchical && childInFacet)
						termsToUpdate.put(uiFacet.getSource().getName(),
								child.getId());
				}
				fullyUpdatedLabelSet.add(label);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.IUIService#
	 * getDisplayedTermsFacetGroup(de.julielab.semedico.core.FacetGroup)
	 */
	@Override
	public Multimap<UIFacet, IFacetTerm> getDisplayedTermsInFacetGroup(
			FacetGroup<UIFacet> facetGroup) {
		Multimap<UIFacet, IFacetTerm> displayedTermsByFacet = HashMultimap
				.create();
		for (UIFacet facetConfiguration : facetGroup.getTaxonomicalElements()) {

			addDisplayedTermsInFacet(displayedTermsByFacet, facetConfiguration);
		}
		return displayedTermsByFacet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.IUIService#
	 * addDisplayedTermsInFacet(java.util.Map,
	 * de.julielab.semedico.core.UIFacet)
	 */
	@Override
	public void addDisplayedTermsInFacet(
			Multimap<UIFacet, IFacetTerm> displayedTermsByFacet, UIFacet uiFacet) {
		if (uiFacet.isFlat())
			return;

		Collection<IFacetTerm> terms = uiFacet
				.getRootTermsForCurrentlySelectedSubTree();

		// TODO Magic number
		if (terms.size() > 100) {
			log.debug("Forcing facet \"" + uiFacet.getName() + "\" (ID: "
					+ uiFacet.getId() + ") to flat facet counts.");
			uiFacet.setForcedToFlatFacetCounts(true);
			return;
		}
		displayedTermsByFacet.putAll(uiFacet, terms);

	}

}
