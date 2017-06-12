/**
 * FacetResponseProcessComponent.java
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
package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerCommand;
import de.julielab.elastic.query.components.data.aggregation.AggregationCommand;
import de.julielab.elastic.query.components.data.aggregation.IAggregationResult;
import de.julielab.elastic.query.components.data.aggregation.ISignificantTermsAggregationUnit;
import de.julielab.elastic.query.components.data.aggregation.ITermsAggregationUnit;
import de.julielab.elastic.query.components.data.aggregation.SignificantTermsAggregationResult;
import de.julielab.elastic.query.components.data.aggregation.TermsAggregationResult;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.elastic.query.util.TermCountCursor;
import de.julielab.semedico.core.AbstractUserInterfaceState;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.search.SearchRuntimeException;
import de.julielab.semedico.core.search.components.data.Label;
import de.julielab.semedico.core.search.components.data.LabelStore;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.TermLabel;
import de.julielab.semedico.core.search.interfaces.ILabelCacheService;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.IUIService;
import de.julielab.semedico.core.util.PairStream;

/**
 * @author faessler
 * 
 */
public class FacetResponseProcessComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FacetResponseProcess {
		//
	}

	private final ITermService termService;
	private final IFacetService facetService;
	private final ILabelCacheService labelCacheService;
	private final Logger log;
	private final IUIService uiService;
	private int maxFacets;

	public FacetResponseProcessComponent(Logger log, ITermService termService, IFacetService facetService,
			ILabelCacheService labelCacheService, IUIService uiService,
			@Symbol(SemedicoSymbolConstants.MAX_DISPLAYED_FACETS) int maxFacets) {
		this.log = log;
		this.termService = termService;
		this.facetService = facetService;
		this.labelCacheService = labelCacheService;
		this.uiService = uiService;
		this.maxFacets = maxFacets;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.search.components.ISearchComponent#process(de.
	 * julielab .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		try {
			SearchServerCommand serverCmd = semCarrier.getSingleSearchServerCommand();
			ISearchServerResponse searchResponse = semCarrier.getSingleSearchServerResponse();
			AbstractUserInterfaceState uiState = semCarrier.uiState;
			if (null == searchResponse)
				throw new IllegalArgumentException("The solr response must not be null, but it is.");
			if (null == uiState)
				throw new IllegalArgumentException("The UI state is null but it is required to store the facets.");

			LabelStore labelStore = uiState.getLabelStore();

			// Extract the facet counts from Solr's response and store them to
			// the
			// user's interface state object.
			storeHitFacetTermLabels(searchResponse, serverCmd, uiState);
			// Store the total counts for each facet (not individual facet/term
			// counts but the counts of all hit terms of each facet).
			storeTotalFacetCounts(serverCmd, searchResponse, labelStore);

			// for (UIFacet uiFacet : uiState.getSelectedFacetGroup())
			log.info("This is the selected facet group: {}", uiState.getSelectedFacetGroup());
			uiService.sortLabelsIntoFacets(labelStore, uiState.getSelectedFacetGroup().getFacetsInSections(maxFacets));

			return false;
		} catch (RuntimeException e) {
			throw new SearchRuntimeException("RuntimeException while searching. Search state: " + semCarrier, e);
		}
	}

	private void storeTotalFacetCounts(SearchServerCommand serverCmd, ISearchServerResponse searchServerResponse, LabelStore labelStore) {

		if (searchServerResponse.getNumFound() == 0) {
			for (Facet facet : facetService.getFacets())
				labelStore.setTotalFacetCount(facet, 0);
		}

		for (AggregationCommand aggCmd : serverCmd.aggregationCmds.values()) {
			IAggregationResult aggResult = searchServerResponse.getAggregationResult(aggCmd);
			TermsAggregationResult termsAggResult;
			if (aggResult instanceof TermsAggregationResult)
				termsAggResult = (TermsAggregationResult) aggResult;
			else
				continue;
			// This field has no hit facets. When no documents were found,
			// no field will have any hits.
			if (termsAggResult.getAggregationUnits() == null)
				continue;
			// The facet category counts, e.g. for "Proteins and Genes".
			// TODO check if that is the correct name (probably not)
			else if (facetService.isTotalFacetCountField(aggCmd.name)) {
				// Iterate over the actual facet counts.
				for(ITermsAggregationUnit unit : termsAggResult.getAggregationUnits()) {
					Facet facet = facetService.getFacetById(String.valueOf(unit.getTerm()));
					labelStore.setTotalFacetCount(facet, unit.getCount());
				}
			}
		}
	}

	/**
	 * Stores all facet counts from <code>FacetField</code> values into the
	 * label store and marks the parents of hierarchical child terms, that have
	 * been hit, as having child hits.
	 * 
	 * @param serverResponse
	 * @param serverCmd
	 * @param labelStore
	 */
	private void storeHitFacetTermLabels(ISearchServerResponse serverResponse, SearchServerCommand serverCmd,
			AbstractUserInterfaceState uiState) {

		FacetGroup<UIFacet> selectedFacetGroup = uiState.getSelectedFacetGroup();
		Map<Facet, UIFacet> uiFacets = uiState.getUIFacets();
		LabelStore labelStore = uiState.getLabelStore();

		Map<String, TermCountCursor> authorCounts = new HashMap<>();
		Map<String, PairStream<Concept, Long>> otherCounts = new HashMap<>();
		for (AggregationCommand aggCmd : serverCmd.aggregationCmds.values()) {
			Collection<UIFacet> uiFacetsWithSrcName = selectedFacetGroup.getElementsBySourceName(aggCmd.name);
			for (final UIFacet uiFacet : uiFacetsWithSrcName) {
				// Happens when we come over a Solr facet field which does not
				// serve
				// a particular Semedico facet. This could be the field for
				// total
				// facet counts, for
				// example.
				if (uiFacet == null)
					continue;

				final TermsAggregationResult termsAggResult =(TermsAggregationResult) serverResponse.getAggregationResult(aggCmd);
				final List<ITermsAggregationUnit> cursor = termsAggResult.getAggregationUnits();
				// Happens when no terms for the field are returned (e.g. when
				// there are no terms found for the facet and facet.mincount is
				// set to 1 or higher).
				if (cursor == null) {
					log.trace("No terms returned for facet {} (ID: {}).", uiFacet.getName(), uiFacet.getId());
					continue;
				}
				if (uiFacet.getSource().isStringTermSource())
					log.trace(
							"Facet {} with ID {} has a string source, retrieved index terms are used verbatim as string terms.",
							uiFacet.getName(), uiFacet.getId());
				else if (uiFacet.getSource().isDatabaseTermSource())
					log.trace(
							"Facet {} with ID {} has a database term source, retrieved index terms are used as database term identifiers.",
							uiFacet.getName(), uiFacet.getId());
				else
					log.warn("Facet {} with ID {} has an unhandeled source type: {}", uiFacet.getName(),
							uiFacet.getSource().getType());

				final String facetId = uiFacet.getId();
				// Authors are special in that they may be post-processed using
				// the
				// name normalization algorithm that tries
				// to recognize if one name is a variant of another (initials vs
				// full names).
				// TODO after author normalization works again (see TODO below),
				// get
				// this back in
				// if (uiFacet.isAnyAuthorFacet())
				// authorCounts.put(uiFacet.getId(), cursor);
				// else {
				PairStream<Concept, Long> otherTermCounts = new PairStream<Concept, Long>() {

					private int pos = 0;
					
					@Override
					public Concept getLeft() {
						Concept term = null;
						if (uiFacet.getSource().isStringTermSource())
							term = termService.getTermObjectForStringTerm(termsAggResult.getName(), facetId);
						else
							term = (Concept) termService.getTerm(termsAggResult.getName());
						return term;
					}

					@Override
					public Long getRight() {
						return cursor.get(pos).getCount();
					}

					@Override
					public boolean incrementTuple() {
						return ++pos < cursor.size();
					}

					@Override
					public void reset() {
						pos = 0;
					}
				};
				otherCounts.put(facetId, otherTermCounts);
				// }
			}
		}
		// hackish solution to integrate the significant terms facet quickly
		if (null != serverCmd.aggregationCmds
				&& null != serverCmd.aggregationCmds.get(Facet.MOST_INFORMATIVE_CONCEPTS_FACET.getId())) {
			AggregationCommand sigAggCmd = serverCmd.aggregationCmds.get(Facet.MOST_INFORMATIVE_CONCEPTS_FACET.getId());
			SignificantTermsAggregationResult sigAggRes = (SignificantTermsAggregationResult) serverResponse
					.getAggregationResult(sigAggCmd);
			List<ISignificantTermsAggregationUnit> units = sigAggRes.getAggregationUnits();
			if (null != units) {
				final Iterator<ISignificantTermsAggregationUnit> unitIt = units.iterator();
				PairStream<Concept, Long> otherTermCounts = new PairStream<Concept, Long>() {

					private ISignificantTermsAggregationUnit unit;

					@Override
					public Concept getLeft() {
						return (Concept) termService.getTerm(unit.getTerm());
					}

					@Override
					public Long getRight() {
						return unit.getDocCount();
					}

					@Override
					public boolean incrementTuple() {
						if (unitIt.hasNext()) {
							unit = unitIt.next();
							return true;
						}
						return false;
					}

					@Override
					public void reset() {
						throw new NotImplementedException();
					}
				};
				otherCounts.put(Facet.MOST_INFORMATIVE_CONCEPTS_FACET.getId(), otherTermCounts);
			}
		}

		// TODO check author normalization, throws errors
		// Map<String, PairStream<Concept, Long>> normalizedAuthorCounts =
		// termService.getTermCountsForAuthorFacets(authorCounts,
		// searchState.getId());
		// createLabels(labelStore, normalizedAuthorCounts, uiFacets);
		createLabels(labelStore, otherCounts, uiFacets);

	}

	private void createLabels(LabelStore labelStore, Map<String, PairStream<Concept, Long>> termCountsByFacetId,
			Map<Facet, UIFacet> uiFacets) {

		// One single Map to associate with each queried term id its facet
		// count.
		Map<String, TermLabel> labelsHierarchical = labelStore.getLabelsHierarchical();

		for (String facetId : termCountsByFacetId.keySet()) {
			PairStream<Concept, Long> termCounts = termCountsByFacetId.get(facetId);

			Facet facet = facetService.getFacetById(facetId);
			UIFacet uiFacet = uiFacets.get(facet);

			log.trace("Generating labels for facet {} (ID: {})", facet.getName(), facet.getId());
			if (uiFacet.isInFlatViewMode())
				log.trace("Facet is in list mode, its labels will be stored as a single list.");
			else
				log.trace("Facet is in hierarchical mode, labels we be stored in the labels <id, label> map.");

			List<String> hierarchicalTermIdsForThisFacet = new ArrayList<>();
			int termCount = 0;
			while (termCounts.incrementTuple()) {
				Concept term = termCounts.getLeft();
				long count = termCounts.getRight();

				// May happen when we query specific terms via LocalParams
				if (count == 0)
					continue;

				termCount++;

				Label label = null;
				if (uiFacet.isInFlatViewMode()) {
					label = labelCacheService.getCachedLabel(term);
					labelStore.addLabelForFacet(label, facetId);
					// labelStore.sortFlatLabelsForFacet(facetId);
				} else {
					// If we have a facet which genuinely contains
					// (hierarchical) terms but is set to flat state, we do
					// not only get a label and put in the list.
					// Additionally we put it into the hierarchical labels
					// map and resolve child term hits for parents.
					label = labelsHierarchical.get(term);
					if (label == null) {
						label = labelCacheService.getCachedLabel(term);
						labelsHierarchical.put(term.getId(), (TermLabel) label);
					}
					hierarchicalTermIdsForThisFacet.add(term.getId());
				}
				label.setCount(count);
			}
			log.trace("Facet {} (ID: {}) received {} labels with >0 count.",
					new Object[] { facet.getName(), facet.getId(), termCount });
			// Check if we have to load roots or child terms for this facet.
			// This can happen when the roots/child terms
			// are
			// so numerous that we didn't want to load them all in UIService
			// when we determined which terms are
			// currently
			// for display. Then, we do not facet for the exact terms of the
			// currently shown facet subtree but we issue
			// a
			// request for the top-N terms in the field. Note that this does
			// work best for facet roots since for
			// taxonomic
			// trees, the roots are always among the most common terms. This
			// does not really work well for inner terms,
			// so
			// this is a TODO (one might first check what the maximum out-degree
			// of nodes in our term database is and
			// how
			// often this problem occurs)
			// As we notice, this actually does ONLY work for roots at the
			// moment. But this at least fixes the
			// genes/proteins issue where the facet has around 450k roots
			if (!uiFacet.isFlat() && !uiFacet.allDBRootsLoaded())
				log.debug("Loading facet roots of facet {} (ID: {}) as given by the following list of term IDs: {}",
						new Object[] { uiFacet.getName(), uiFacet.getId(), hierarchicalTermIdsForThisFacet });
			uiFacet.getFacetRoots(hierarchicalTermIdsForThisFacet);
		}
		for (String facetId : labelStore.getFlatLabels().keySet())
			labelStore.sortFlatLabelsForFacet(facetId);

		// uiService.resolveChildHitsRecursively(labelStore);
	}
}
