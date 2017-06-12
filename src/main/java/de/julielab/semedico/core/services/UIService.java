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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.time.StopWatch;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.Facet.Source;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.search.components.data.Label;
import de.julielab.semedico.core.search.components.data.LabelStore;
import de.julielab.semedico.core.search.components.data.MessageLabel;
import de.julielab.semedico.core.search.components.data.TermLabel;
import de.julielab.semedico.core.search.interfaces.ILabelCacheService;
import de.julielab.semedico.core.services.interfaces.ICacheService;
import de.julielab.semedico.core.services.interfaces.ICacheService.Region;
import de.julielab.semedico.core.services.interfaces.IUIService;
import de.julielab.semedico.core.util.DisplayGroup;

/**
 * @author faessler
 * 
 */
public class UIService implements IUIService {

	private final Logger log;
	private final int maxDisplayedFacets;
	private int showTermsMinHits;
	private ILabelCacheService labelCacheService;
	private ICacheService cacheService;
	private boolean displayFacetCount;
	private boolean displayMessageWhenNoChildrenHit;
	private boolean displayMessageWhenNoFacetRootsHit;

	public UIService(
			Logger log,
			@Symbol(MAX_DISPLAYED_FACETS) int maxDisplayedFacets,
			@Symbol(SemedicoSymbolConstants.DISPLAY_TERMS_MIN_HITS) int showTermsMinHits,
			@Symbol(SemedicoSymbolConstants.DISPLAY_FACET_COUNT) boolean displayFacetCount,
			@Symbol(SemedicoSymbolConstants.DISPLAY_MESSAGE_WHEN_NO_CHILDREN_HIT) boolean displayMessageWhenNoChildrenHit,
			@Symbol(SemedicoSymbolConstants.DISPLAY_MESSAGE_WHEN_NO_FACET_ROOTS_HIT) boolean displayMessageWhenNoFacetRootsHit,
			ILabelCacheService labelCacheService, ICacheService cacheService) {
		this.log = log;
		this.maxDisplayedFacets = maxDisplayedFacets;
		this.showTermsMinHits = showTermsMinHits;
		this.displayFacetCount = displayFacetCount;
		this.displayMessageWhenNoChildrenHit = displayMessageWhenNoChildrenHit;
		this.displayMessageWhenNoFacetRootsHit = displayMessageWhenNoFacetRootsHit;
		this.labelCacheService = labelCacheService;
		this.cacheService = cacheService;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.IUIService#
	 * storeUnknownChildrenOfDisplayedTerms(de.julielab.semedico.core.UIFacet, com.google.common.collect.Multimap,
	 * de.julielab.semedico.core.LabelStore)
	 */
	@Override
	@Deprecated
	public void storeUnknownChildrenOfDisplayedTerms(UIFacet uiFacet, Multimap<String, String> termsToUpdate,
			LabelStore labelStore) {

		if (uiFacet.isFlat())
			return;

		Set<Label> fullyUpdatedLabelSet = labelStore.fullyUpdatedLabelSets.get(uiFacet);

		if (fullyUpdatedLabelSet == null) {
			fullyUpdatedLabelSet = new HashSet<Label>();
			labelStore.fullyUpdatedLabelSets.put(uiFacet, fullyUpdatedLabelSet);
		}

		List<Label> displayedLabels = uiFacet.getLabelDisplayGroup().getDisplayedObjects();
		for (Label label : displayedLabels) {
			if (!(label instanceof TermLabel))
				continue;
			if (!fullyUpdatedLabelSet.contains(label)) {
				Concept term = ((TermLabel) label).getTerm();
				// Only prepare up to 10 (TODO!! MN...) children. E.g. organic
				// chemicals has 688 children which is a bit much to query
				// one-by-one (it works but slows things down).
				for (int i = 0; i < 10 && i < term.getNumberOfChildren(); i++) {
					Concept child = term.getChild(i);
					boolean childInLabelsHierarchical = labelStore.labelsHierarchical.containsKey(child.getId());
					boolean childInFacet = child.isContainedInFacet(uiFacet);
					if (!childInLabelsHierarchical && childInFacet)
						termsToUpdate.put(uiFacet.getSource().getName(), child.getId());
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
	// Called from FacetCountPreparationComponent to determine which terms to get counts for.
	@Override
	public Multimap<UIFacet, String> getDisplayedTermsInFacetGroup(List<UIFacet> facetGroup) {
		log.debug("Collecting displayed terms of selected sub trees for {} facets.", facetGroup.size());
		StopWatch w = new StopWatch();
		w.start();

		Multimap<UIFacet, String> displayedTermsByFacet = HashMultimap.create();

		loadRootTermsForCurrentlySelectedSubTrees(facetGroup);

		// When a facet has no terms, it just isn't displayed. Thus, restrict
		// the terms returned to
		// the desired maximum number of facets.
		// TODO set facets explicitly to 'hidden' instead? This could make the facet selection dialog more consistent.
		for (int i = 0; i < Math.min(maxDisplayedFacets, facetGroup.size()); i++) {
			UIFacet uiFacet = facetGroup.get(i);
			if (uiFacet.isFlat() || uiFacet.isForcedToFlatFacetCounts() || uiFacet.isInFlatViewMode())
				continue;
			displayedTermsByFacet.putAll(uiFacet, uiFacet.getRootTermIdsForCurrentlySelectedSubTree(true));
			// addDisplayedTermsInFacet(displayedTermsByFacet, uiFacet);
		}
		w.stop();
		log.debug("Collecting of displayed terms took {}ms ({}s).", w.getTime(), w.getTime() / 1000);
		return displayedTermsByFacet;
	}


	private void loadRootTermsForCurrentlySelectedSubTrees(Iterable<UIFacet> uiFacets) {

		List<String> facetsToGetRootsFor = new ArrayList<>();
		// List<String> termsToGetChildrenFor = new ArrayList<>();
		LoadingCache<String, List<Concept>> facetRootCache = cacheService.getCache(Region.FACET_ROOTS);
		// LoadingCache<String, Collection<IFacetTerm>> termChildrenCache = cacheService.getCache(Region.TERM_CHILDREN);
		for (UIFacet uiFacet : uiFacets) {

			Source source = uiFacet.getSource();
			if (source.isFlat())
				continue;
			if (uiFacet.isDrilledDown()) {
				Concept lastPathTerm = uiFacet.getCurrentPath().getLastNode();
				Collection<IConcept> allChildrenInFacet = lastPathTerm.getAllChildrenInFacet(uiFacet.getId());
				// TODO magic number
				if (allChildrenInFacet.size() > 200) {
					log.debug("Forcing facet \"" + uiFacet.getName()
							+ "\" (ID: "
							+ uiFacet.getId()
							+ ") to flat facet counts because the currently shown hierarchy level contains more than 200 terms.");
					uiFacet.setForcedToFlatFacetCounts(true);
					// if (null == termChildrenCache.getIfPresent(lastPathTerm.getId()))
					// termsToGetChildrenFor.add(lastPathTerm.getId());
				}
			} else {
				// TODO magic number
				// Do not load facet roots when there are too many roots. Genes and Proteins is quite flat and has
				// around 450k roots.
				if (uiFacet.getNumRootsInDB() > 200) {
					log.debug("Forcing facet \"" + uiFacet.getName()
							+ "\" (ID: "
							+ uiFacet.getId()
							+ ") to flat facet counts because the currently shown hierarchy level (facet roots) contains more than 200 terms.");
					uiFacet.setForcedToFlatFacetCounts(true);
					continue;
				}
				if (null == facetRootCache.getIfPresent(uiFacet.getId()))
					facetsToGetRootsFor.add(uiFacet.getId());
			}
		}

		try {
			facetRootCache.getAll(facetsToGetRootsFor);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sortLabelsIntoFacet(LabelStore labelStore, UIFacet uiFacet) {
		sortLabelsIntoFacets(labelStore, Lists.newArrayList(uiFacet));
		// DisplayGroup<Label> displayGroup = uiFacet.getLabelDisplayGroup();
		//
		// List<Label> labelsForFacet = getLabelsForFacet(labelStore, uiFacet);
		// displayGroup.setAllObjects(labelsForFacet);
		// displayGroup.displayBatch(1);
	}

	// private List<Label> getLabelsForFacet(LabelStore labelStore, UIFacet uiFacet) {
	// List<Label> labelsForFacet = null;
	// if (uiFacet.isInHierarchicViewMode()) {
	// if (uiFacet.isDrilledDown()) {
	// labelsForFacet = getLabelsForHitChildren(labelStore,
	// uiFacet.getLastPathElement(), uiFacet);
	// } else {
	// labelsForFacet = getLabelsForHitFacetRoots(labelStore, uiFacet);
	// }
	// } else {
	// labelsForFacet = labelStore.getFlatLabels().get(uiFacet.getId());
	// }
	// return labelsForFacet;
	// }

	/**
	 * Returns the labels corresponding to the children of <code>term</code> with respect to <code>facet</code>.
	 * <p>
	 * <code>term</code> should be contained in <code>facet</code> in order to achieve meaningful results.<br>
	 * Only labels of <code>term</code>'s children which are also contained in <code>facet</code> are returned, thus
	 * delivering a filter mechanism for facets which exclude particular terms (like the aging facets which are a subset
	 * of MeSH but exclude most terms).
	 * </p>
	 * 
	 * @param term
	 *            The term for whose children labels should be returned.
	 * @param facet
	 *            The facet which constrains the children returned to those which are also included in
	 *            <code>facet</code>.
	 * @return
	 */
	private List<Label> getLabelsForHitChildren(LabelStore labelStore, Concept term, Facet facet) {

		Map<String, TermLabel> labelsHierarchical = labelStore.getLabelsHierarchical();

		List<Label> retLabels = new ArrayList<Label>();
		Iterator<Concept> childIt = term.childIterator(facet.getId());
		boolean noChildWasHit = true;
		while (childIt.hasNext()) {
			Concept child = childIt.next();
			TermLabel l = labelsHierarchical.get(child.getId());
			if (!displayFacetCount)
				l.setShowRankScore(false);
			// The label can be null when the facet is hierarchical but was
			// forced to flat facet counts due to too high node degree.
			// In this case the terms for which we don't have any counts are
			// left out.
			if (l != null && l.getCount() >= showTermsMinHits) {
				noChildWasHit = false;
				retLabels.add(l);
			} else if (l == null && showTermsMinHits == 0 && !displayMessageWhenNoChildrenHit) {
				log.trace("Creating 0-hit label for child term {} (ID: {})", child.getPreferredName(), child.getId());
				Label label = labelCacheService.getCachedLabel(child.getId());
				label.setCount(0L);
				retLabels.add(label);
			}
		}
		if (noChildWasHit)
			log.trace("No child of term {} (ID: {}) was hit.", term.getPreferredName(), term.getId());
		if (noChildWasHit && displayMessageWhenNoChildrenHit) {
			MessageLabel noHitsLabel =
					new MessageLabel("No hits in subcategories",
							"No subterms of the currently selected term occur in the currently displayed search results.");
			noHitsLabel.setShowRankScore(false);
			noHitsLabel.setCount(Long.MAX_VALUE);
			retLabels.add(noHitsLabel);
		}
		// Collections.sort(retLabels);
		return retLabels;
	}

	/**
	 * @param facet
	 * @return
	 */
	private List<Label> getLabelsForHitFacetRoots(LabelStore labelStore, Facet facet) {
		Map<String, TermLabel> labelsHierarchical = labelStore.getLabelsHierarchical();

		List<Label> retLabels = new ArrayList<Label>();
		Collection<Concept> facetRoots = facet.getFacetRoots();

		// Security check...
		if (facetRoots == null) {
			throw new IllegalStateException("Facet '" + facet.getName()
					+ "' (ID "
					+ facet.getId()
					+ ") has zero facet roots.");
		}

		boolean noRootWasHit = true;
		Iterator<Concept> rootIt = facetRoots.iterator();
		while (rootIt.hasNext()) {
			String rootId = rootIt.next().getId();
			TermLabel l = labelsHierarchical.get(rootId);

			if (!displayFacetCount)
				l.setShowRankScore(false);
			// The label can be null when the facet is hierarchical but was
			// forced to flat facet counts due to too high node degree.
			// In this case the terms for which we don't have any counts are
			// left out.
			if (l != null && l.getCount() >= showTermsMinHits) {
				noRootWasHit = false;
				retLabels.add(l);
			} else if (l == null && showTermsMinHits == 0 && !displayMessageWhenNoFacetRootsHit) {
				Label label = labelCacheService.getCachedLabel(rootId);
				label.setCount(0L);
				retLabels.add(label);
			}
		}
		if (noRootWasHit && displayMessageWhenNoFacetRootsHit) {
			MessageLabel noHitsLabel =
					new MessageLabel("No hits in this facet",
							"No terms contained in this facet occur in the currently displayed search results.");
			noHitsLabel.setShowRankScore(false);
			noHitsLabel.setCount(Long.MAX_VALUE);
			retLabels.add(noHitsLabel);
		}
		// Collections.sort(retLabels);
		return retLabels;
	}

	@Override
	public void resolveChildHitsRecursively(LabelStore labelStore) {
		Map<String, TermLabel> labelsHierarchical = labelStore.getLabelsHierarchical();
		for (TermLabel label : labelsHierarchical.values()) {
			Concept term = label.getTerm();
			for (Concept parent : term.getAllParents()) {
				for (Facet facet : term.getFacets()) {
					if (parent.isContainedInFacet(facet)) {
						TermLabel parentLabel = labelsHierarchical.get(parent.getId());
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

	@Override
	public void sortLabelsIntoFacets(LabelStore labelStore, Iterable<UIFacet> uiFacets) {
		Map<UIFacet, List<Label>> facetLabelMap = new HashMap<>();
		loadRootTermsForCurrentlySelectedSubTrees(uiFacets);

		// Now go on and create labels and sort them.
		for (UIFacet uiFacet : uiFacets) {
			List<Label> labelsForFacet = null;
			log.trace("Sorting labels into facet {} (ID: {}).", uiFacet.getName(), uiFacet.getId());
			if (uiFacet.isInHierarchicViewMode()) {
				log.trace("Facet is in hierarchic view mode.");
				if (uiFacet.isDrilledDown()) {
					log.trace("Facet is drilled down to term {} (ID: {}), its children are to be displayed.", uiFacet
							.getLastPathElement().getPreferredName(), uiFacet.getLastPathElement().getId());
					labelsForFacet = getLabelsForHitChildren(labelStore, uiFacet.getLastPathElement(), uiFacet);
				} else {
					log.trace("Facet is drilled up, i.e. its roots are displayed.");
					labelsForFacet = getLabelsForHitFacetRoots(labelStore, uiFacet);
				}
			} else {
				labelsForFacet = labelStore.getFlatLabels().get(uiFacet.getId());
				log.trace("Facet is flat or has been forced to flat counts, sorting in a list of {} labels.",
						null != labelsForFacet ? labelsForFacet.size() : null);
			}
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
