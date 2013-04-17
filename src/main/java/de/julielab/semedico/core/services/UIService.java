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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.IUIService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.util.DisplayGroup;

/**
 * @author faessler
 * 
 */
public class UIService implements IUIService {

	private final Logger log;
	private final ITermService termService;

	public UIService(Logger log, ITermService termService) {
		this.log = log;
		this.termService = termService;

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

	@Override
	public void sortLabelsIntoFacet(LabelStore labelStore,
			UIFacet uiFacet) {
		Map<Integer, List<Label>> labelsFlat = labelStore.getFlatLabels();
		DisplayGroup<Label> displayGroup = uiFacet
				.getLabelDisplayGroup();

		List<Label> labelsForFacet = null;
		if (uiFacet.isInHierarchicViewMode()) {
			if (uiFacet.isDrilledDown()) {
				labelsForFacet = getLabelsForHitChildren(labelStore,
						uiFacet.getLastPathElement(),
						uiFacet);
			} else {
				labelsForFacet = getLabelsForHitFacetRoots(labelStore,
						uiFacet);
			}
		} else {
			labelsForFacet = labelsFlat.get(uiFacet.getId());
		}
		displayGroup.setAllObjects(labelsForFacet);
		displayGroup.displayBatch(1);
	}

	/**
	 * Returns the labels corresponding to the children of <code>term</code>
	 * with respect to <code>facet</code>.
	 * <p>
	 * <code>term</code> should be contained in <code>facet</code> in order to
	 * achieve meaningful results.<br>
	 * Only labels of <code>term</code>'s children which are also contained in
	 * <code>facet</code> are returned, thus delivering a filter mechanism for
	 * facets which exclude particular terms (like the aging facets which are a
	 * subset of MeSH but exclude most terms).
	 * </p>
	 * 
	 * @param term
	 *            The term for whose children labels should be returned.
	 * @param facet
	 *            The facet which constrains the children returned to those
	 *            which are also included in <code>facet</code>.
	 * @return
	 */
	private List<Label> getLabelsForHitChildren(LabelStore labelStore,
			IFacetTerm term, Facet facet) {

		Map<String, TermLabel> labelsHierarchical = labelStore
				.getLabelsHierarchical();

		List<Label> retLabels = new ArrayList<Label>();
		Iterator<IFacetTerm> childIt = term.childIterator();
		while (childIt.hasNext()) {
			IFacetTerm child = childIt.next();
			if (!child.isContainedInFacet(facet))
				continue;
			TermLabel l = labelsHierarchical.get(child.getId());
			// The label can be null when the facet is hierarchical but was
			// forced to flat facet counts due to too high node degree.
			// In this case the terms for which we don't have any counts are
			// left out.
			if (l != null && l.getCount() > 0)
				retLabels.add(l);
		}
		Collections.sort(retLabels);
		return retLabels;
	}

	/**
	 * @param facet
	 * @return
	 */
	private List<Label> getLabelsForHitFacetRoots(LabelStore labelStore,
			Facet facet) {
		Map<String, TermLabel> labelsHierarchical = labelStore
				.getLabelsHierarchical();

		List<Label> retLabels = new ArrayList<Label>();
		Collection<IFacetTerm> facetRoots = facet.getFacetRoots();

		// Security check...
		if (facetRoots == null) {
			List<IFacetTerm> termsForFacet = termService
					.getTermsForFacet(facet);
			if (termsForFacet == null || termsForFacet.size() == 0)
				throw new IllegalStateException("Facet '" + facet.getName()
						+ "' (ID " + facet.getId() + ") has no terms");
		}

		Iterator<IFacetTerm> rootIt = facetRoots.iterator();
		while (rootIt.hasNext()) {
			TermLabel l = labelsHierarchical.get(rootIt.next().getId());
			// The label can be null when the facet is hierarchical but was
			// forced to flat facet counts due to too high node degree.
			// In this case the terms for which we don't have any counts are
			// left out.
			if (l != null && l.getCount() > 0)
				retLabels.add(l);
		}
		Collections.sort(retLabels);
		return retLabels;
	}

	@Override
	public void resolveChildHitsRecursively(LabelStore labelStore) {
		Map<String, TermLabel> labelsHierarchical = labelStore
				.getLabelsHierarchical();
		for (TermLabel label : labelsHierarchical.values()) {
			IFacetTerm term = label.getTerm();
			for (IFacetTerm parent : term.getAllParents()) {
				for (Facet facet : term.getFacets()) {
					if (parent.isContainedInFacet(facet)) {
						TermLabel parentLabel = labelsHierarchical.get(parent
								.getId());
						// When the parent label is null, this means this parent
						// is of another facet in a not-displayed facet group.
						// Example for this to happen:
						// Child: { internalIdentifier:D011694; name: Purpura,
						// Hyperglobulinemic; facet:Diseases, Diseases /
						// Pathological Processes }
						// Parent: { internalIdentifier:D013568; name:
						// Pathological Conditions, Signs and Symptoms;
						// facet:Diseases }
						// "Diseases" is in the Ageing facet group,
						// "Diseases / Pathological Processes" in BioMed.
						if (parentLabel != null) {
							parentLabel.setHasChildHitsInFacet(facet);
						}
					}
				}
			}
		}

	}

}
