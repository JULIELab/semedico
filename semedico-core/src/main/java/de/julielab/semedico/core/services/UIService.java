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

import static de.julielab.semedico.core.services.SemedicoSymbolConstants.MAX_DISPLAYED_FACETS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.time.StopWatch;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.search.components.data.Label;
import de.julielab.semedico.core.search.components.data.LabelStore;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.services.interfaces.ICacheService;
import de.julielab.semedico.core.services.interfaces.ICacheService.Region;
import de.julielab.semedico.core.util.DisplayGroup;
import de.julielab.semedico.core.services.interfaces.IUIService;

/**
 * @author faessler
 * 
 */
public class UIService implements IUIService {

	private final Logger log;
	private final int maxDisplayedFacets;
	private ICacheService cacheService;

	public UIService(
			Logger log,
			@Symbol(MAX_DISPLAYED_FACETS) int maxDisplayedFacets,
			ICacheService cacheService) {
		this.log = log;
		this.maxDisplayedFacets = maxDisplayedFacets;
		this.cacheService = cacheService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.IUIService#
	 * getDisplayedTermsFacetGroup(de.julielab.semedico.core.FacetGroup)
	 */
	// Called from FacetCountPreparationComponent to determine which terms to get counts for.
	@Override
	public Multimap<UIFacet, String> getDisplayedTermsInFacetGroup(List<UIFacet> facetGroup) {
		log.debug("Collecting displayed terms of selected sub trees for {} facets.", facetGroup.size());
		StopWatch w = new StopWatch();
		w.start();

		Multimap<UIFacet, String> displayedTermsByFacet = HashMultimap.create();

		loadRootTermsForCurrentlySelectedSubTrees();

		// When a facet has no terms, it just isn't displayed. Thus, restrict
		// the terms returned to
		// the desired maximum number of facets.
		// TODO set facets explicitly to 'hidden' instead? This could make the facet selection dialog more consistent.
		for (int i = 0; i < Math.min(maxDisplayedFacets, facetGroup.size()); i++) {
			UIFacet uiFacet = facetGroup.get(i);
			if (uiFacet.isFlat() || uiFacet.isForcedToFlatFacetCounts())
				continue;
			displayedTermsByFacet.putAll(uiFacet, uiFacet.getRootTermIdsForCurrentlySelectedSubTree(true));
		}
		w.stop();
		log.debug("Collecting of displayed terms took {}ms ({}s).", w.getTime(), w.getTime() / 1000);
		return displayedTermsByFacet;
	}

	private void loadRootTermsForCurrentlySelectedSubTrees() {

		List<String> facetsToGetRootsFor = new ArrayList<>();
		LoadingCache<String, List<Concept>> facetRootCache = cacheService.getCache(Region.FACET_ROOTS);
		
		try {
			facetRootCache.getAll(facetsToGetRootsFor);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sortLabelsIntoFacet(LabelStore labelStore, UIFacet uiFacet) {
		sortLabelsIntoFacets(labelStore, Lists.newArrayList(uiFacet));
	}

	@Override
	public void sortLabelsIntoFacets(LabelStore labelStore, Iterable<UIFacet> uiFacets) {
		Map<UIFacet, List<Label>> facetLabelMap = new HashMap<>();
		loadRootTermsForCurrentlySelectedSubTrees();

		// Now go on and create labels and sort them.
		for (UIFacet uiFacet : uiFacets) {
			List<Label> labelsForFacet = null;
			log.trace("Sorting labels into facet {} (ID: {}).", uiFacet.getName(), uiFacet.getId());

			labelsForFacet = labelStore.getFlatLabels().get(uiFacet.getId());
			log.trace("Facet is flat or has been forced to flat counts, sorting in a list of {} labels.",
					null != labelsForFacet ? labelsForFacet.size() : null);
			
			facetLabelMap.put(uiFacet, labelsForFacet);
		}
		// Sorting is the last thing we do after we have requested all terms. For sorting, the term's preferred names
		// will
		// be required and thus synchronization must happen.
		for (UIFacet uiFacet : uiFacets) {
			List<Label> labelsForFacet = facetLabelMap.get(uiFacet);

			if (null == labelsForFacet)
				labelsForFacet = Collections.emptyList();

			DisplayGroup<Label> displayGroup = uiFacet.getLabelDisplayGroup();

			Collections.sort(labelsForFacet);

			displayGroup.setAllObjects(labelsForFacet);
			displayGroup.displayBatch(1);
		}

	}

}
