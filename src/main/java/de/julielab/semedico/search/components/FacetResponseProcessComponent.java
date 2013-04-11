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
package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.slf4j.Logger;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.search.interfaces.ILabelCacheService;
import de.julielab.util.AbstractPairStream.PairTransformer;
import de.julielab.util.PairStream;
import de.julielab.util.PairTransformationStream;

/**
 * @author faessler
 * 
 */
public class FacetResponseProcessComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FacetResponseProcess {
	}

	private final ApplicationStateManager asm;
	private final ITermService termService;
	private final IFacetService facetService;
	private final ILabelCacheService labelCacheService;
	private final Logger log;

	public FacetResponseProcessComponent(Logger log, ApplicationStateManager asm,
			ITermService termService, IFacetService facetService,
			ILabelCacheService labelCacheService) {
		this.log = log;
		this.asm = asm;
		this.termService = termService;
		this.facetService = facetService;
		this.labelCacheService = labelCacheService;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.components.ISearchComponent#process(de.julielab
	 * .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean process(SearchCarrier searchCarrier) {
		QueryResponse solrResponse = searchCarrier.solrResponse;
		if (null == solrResponse)
			throw new IllegalArgumentException(
					"The solr response must not be null, but it is.");
		if (null == solrResponse.getFacetFields()) {
			log.warn("The Solr response does not contain facet counts for fields.");
			return false;
		}

		UserInterfaceState uiState = asm.get(UserInterfaceState.class);
		LabelStore labelStore = uiState.getLabelStore();

		// Extract the facet counts from Solr's response and store them to
		// the
		// user's interface state object.
		storeHitFacetTermLabels(solrResponse, labelStore);
		// Store the total counts for each facet (not individual facet/term
		// counts but the counts of all hit terms of each facet).
		storeTotalFacetCounts(solrResponse, labelStore);
		
		for (UIFacet uiFacet : uiState.getSelectedFacetGroup())
			labelStore.sortLabelsIntoFacet(uiFacet);
		
		return false;
	}

	private void storeTotalFacetCounts(QueryResponse queryResponse,
			LabelStore labelStore) {

		// TODO Won't work until the statistics component is fixed in solrj to
		// work with string fields.
		// See remark in adjustQueryForFacetCountsInFacet
		// Map<String, FieldStatsInfo> fieldStatsInfo = queryResponse
		// .getFieldStatsInfo();
		// FacetGroup<FacetConfiguration> selectedFacetGroup =
		// applicationStateManager
		// .get(SearchSessionState.class).getUiState()
		// .getSelectedFacetGroup();
		//
		// for (FacetConfiguration facetConfiguration : selectedFacetGroup) {
		// FieldStatsInfo fieldStats = fieldStatsInfo.get(facetConfiguration
		// .getSource().getName());
		// facetHit.setTotalFacetCount(facetConfiguration.getFacet(),
		// fieldStats.getCount());
		// }

		if (queryResponse.getResults().getNumFound() == 0) {
			for (Facet facet : facetService.getFacets())
				labelStore.setTotalFacetCount(facet, 0);
		}

		for (FacetField field : queryResponse.getFacetFields()) {
			// This field has no hit facets. When no documents were found,
			// no field will have any hits.
			if (field.getValues() == null)
				continue;
			// The facet category counts, e.g. for "Proteins and Genes".
			else if (facetService.isTotalFacetCountField(field.getName())) {
				// Iterate over the actual facet counts.
				for (Count count : field.getValues()) {
					Facet facet = facetService.getFacetById(Integer
							.parseInt(count.getName()));
					labelStore.setTotalFacetCount(facet, count.getCount());
				}
			}
		}
	}

	/**
	 * Stores all facet counts from <code>FacetField</code> values into the
	 * label store and marks the parents of hierarchical child terms, that have
	 * been hit, as having child hits.
	 * 
	 * @param queryResponse
	 * @param labelStore
	 */
	private void storeHitFacetTermLabels(QueryResponse queryResponse,
			LabelStore labelStore) {
		
		FacetGroup<UIFacet> selectedFacetGroup = asm.get(
				UserInterfaceState.class).getSelectedFacetGroup();

		Map<Integer, List<Count>> authorCounts = new HashMap<Integer, List<Count>>();
		Map<Integer, PairStream<IFacetTerm, Long>> otherCounts = new HashMap<Integer, PairStream<IFacetTerm, Long>>();
		for (FacetField facetField : queryResponse.getFacetFields()) {
			final UIFacet facetConfiguration = selectedFacetGroup
					.getElementsBySourceName(facetField.getName());

			// Happens when we come over a Solr facet field which does not serve
			// a particular Semedico facet. This could be the field for total
			// facet counts, for
			// example.
			if (facetConfiguration == null)
				continue;

			final List<Count> facetValues = facetField.getValues();
			// Happens when no terms for the field are returned (e.g. when
			// there are no terms found for the facet and facet.mincount is
			// set to 1 or higher).
			if (facetValues == null)
				continue;

			final Integer facetId = facetConfiguration.getId();
			if (facetService.isAnyAuthorFacetId(facetId))
				authorCounts.put(facetId, facetValues);
			else {
				PairStream<IFacetTerm, Long> otherTermCounts = new PairTransformationStream<Count, Collection<Count>, IFacetTerm, Long>(
						facetValues,
						new PairTransformer<Count, IFacetTerm, Long>() {

							@Override
							public IFacetTerm transformLeft(Count sourceElement) {
								IFacetTerm term = null;
								if (facetConfiguration.isFlat())
									term = termService
											.getTermObjectForStringTerm(
													sourceElement.getName(),
													facetId);
								else
									term = termService.getNode(sourceElement
											.getName());
								return term;
							}

							@Override
							public Long transformRight(Count sourceElement) {
								return sourceElement.getCount();
							}
						});
				otherCounts.put(facetId, otherTermCounts);
			}
		}

		Map<Integer, PairStream<IFacetTerm, Long>> normalizedAuthorCounts = termService
				.getTermCountsForAuthorFacets(authorCounts);
		createLabels(labelStore, normalizedAuthorCounts);
		createLabels(labelStore, otherCounts);

	}

	private void createLabels(LabelStore labelStore,
			Map<Integer, PairStream<IFacetTerm, Long>> termCountsByFacetId) {
		UserInterfaceState uiState = asm.get(UserInterfaceState.class);
		Map<Facet, UIFacet> uiFacets = uiState.getFacetConfigurations();

		// One single Map to associate with each queried term id its facet
		// count.
		Map<String, TermLabel> labelsHierarchical = labelStore
				.getLabelsHierarchical();

		for (Integer facetId : termCountsByFacetId.keySet()) {
			PairStream<IFacetTerm, Long> termCounts = termCountsByFacetId
					.get(facetId);

			Facet facet = facetService.getFacetById(facetId);
			UIFacet uiFacet = uiFacets.get(facet);

			while (termCounts.incrementTuple()) {
				IFacetTerm term = termCounts.getLeft();
				long count = termCounts.getRight();
				
				// May happen when we query specific terms via LocalParams
				if (count == 0)
					continue;

				Label label = null;
				if (uiFacet.isInFlatViewMode()) {
					label = labelCacheService.getCachedLabel(term);
					labelStore.addLabelForFacet(label, facetId);
					labelStore.sortFlatLabelsForFacet(facetId);
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
				}
				label.setCount(count);
			}
		}
		labelStore.resolveChildHitsRecursively();
	}
}
