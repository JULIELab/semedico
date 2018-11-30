/**
 * FacetSearchPreparatorComponent.java
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
 * Creation date: 09.04.2013
 **/

/**
 * 
 */
package de.julielab.semedico.core.search.components;

import com.google.common.collect.Multimap;
import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerRequest;
import de.julielab.elastic.query.components.data.aggregation.SignificantTermsAggregation;
import de.julielab.elastic.query.components.data.aggregation.TermsAggregation;
import de.julielab.semedico.commons.concepts.FacetLabels;
import de.julielab.semedico.core.entities.state.AbstractUserInterfaceState;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.search.components.data.LabelStore;
import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCommand;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.IUIService;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

/**
 * @author faessler
 * @deprecated Must go into an {@link de.julielab.elastic.query.components.data.aggregation.AggregationRequest}
 */
public class FacetCountPreparationComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FacetCountPreparation {
		//
	}

	private final IUIService uiService;
	private int maxFacets;
	private IFacetService facetService;

	public FacetCountPreparationComponent(Logger log, IUIService uiService, IFacetService facetService,
			@Symbol(SemedicoSymbolConstants.MAX_DISPLAYED_FACETS) int maxFacets) {
		super(log);
		this.uiService = uiService;
		this.facetService = facetService;
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
		SemedicoESSearchCarrier semCarrier = (SemedicoESSearchCarrier) searchCarrier;
		SemedicoSearchCommand searchCmd = semCarrier.searchCmd;
		// TODO repair
		AbstractUserInterfaceState uiState = null;//semCarrier.uiState;
		SearchServerRequest serverCmd = semCarrier.getSingleSearchServerRequest();
		boolean noFacetsToCountDelivered = searchCmd == null || searchCmd.facetsToCount == null
				|| searchCmd.facetsToCount.size() == 0;

		if (null == uiState)
			throw new IllegalArgumentException(
					"The UI state is null. But it is required to determine which facets to retrieve from the search server.");
		if (null == serverCmd)
			throw new IllegalArgumentException("Non-null " + SearchServerRequest.class.getName()
					+ " object is expected which knows about the actual Solr query. However, no such object is present.");
		LabelStore labelStore = uiState.getLabelStore();
		if (noFacetsToCountDelivered && labelStore.hasFacetGroupLabels(uiState.getSelectedFacetGroup())) {
			log.debug("Terminating search chain because the selected facet group already has its facet counts.");
			return true;
		}

		// Facet-term counts. Default: count for selected facet group.
		log.debug(
				"Maximum facets to display per section: {}. Only for this number of facets term counts will be queried.",
				maxFacets);
		List<UIFacet> facetsToCount;
		if (noFacetsToCountDelivered) {
			facetsToCount = uiState.getSelectedFacetGroup().getFacetsInSections(maxFacets);
		} else {
			facetsToCount = searchCmd.facetsToCount.size() > maxFacets ? searchCmd.facetsToCount.subList(0, maxFacets)
					: searchCmd.facetsToCount;
			if (searchCmd.facetsToCount.size() > maxFacets)
				log.debug("Facets were cut to the defined maximum number of facets ({}).", maxFacets);
		}
		log.debug(
				"Facets to count: {}({})", noFacetsToCountDelivered
						? "selected FacetGroup \"" + uiState.getSelectedFacetGroup().getName() + "\"" : "",
				facetsToCount);


		Multimap<UIFacet, String> displayedTermsFacetGroup = uiService.getDisplayedConceptIdsInFacetGroup(facetsToCount);
		Set<String> fieldsBeingFacetedForParticularTerms = new HashSet<>();
		Set<String> fieldsBeingFacetedFlat = new HashSet<>();
		for (UIFacet uiFacet : facetsToCount) {
			if (uiFacet.isCollapsed() && noFacetsToCountDelivered) {
				log.debug("No facet command for facet {} will be issued since the facet is currently collapsed.",
						uiFacet);
				continue;
			}
			if (labelStore.stringFacetAlreadyQueried(uiFacet.getId())) {
				log.debug(
						"No facet command for facet {} (ID: {}) added because it is a string facet and has already been queried.",
						uiFacet.getName(), uiFacet.getId());
				continue;
			} else if (uiFacet.isFlat()) {
				labelStore.addQueriedStringFacet(uiFacet.getId());
			}
			// An "Aggregation Facet" is a facet whose terms are actually a union of terms from other facets
			if (!uiFacet.isAggregationFacet()) {
				// Special handling for the most informative concepts facet:
				// Since
				// "faceting" is long deprecated and even removed from
				// ElasticSearch, we switched to aggregations. The old code here
				// is
				// still working, however, so just keep it for now. But for the
				// most
				// informative facet we want to use the significant terms
				// aggregation which is not supported by the old code; thus we
				// introduce a special case for it
				if (uiFacet.getId() != Facet.MOST_INFORMATIVE_CONCEPTS_FACET.getId()) {
					// Get the terms we want to query for the facet group (if
					// any);
					// below they will be filtered for already
					// queried terms.
					Collection<String> facetTerms = displayedTermsFacetGroup.get(uiFacet);

					String fieldName = uiFacet.getSource().getName();
					TermsAggregation fc = new TermsAggregation();
					fc.name = fieldName;
					// TODO magic number
					fc.size = 100;
					fc.field = fieldName;

					Collection<String> termsToCount = new ArrayList<>(facetTerms.size());
					for (String termId : facetTerms) {
						String id = termId;

						if (labelStore.termIdAlreadyQueried(id))
							continue;

						labelStore.addQueriedConceptId(id);
						termsToCount.add(id);
					}
					fc.include = termsToCount;

					// Add the facet command to the list, if we need it
					addFacetCommand(serverCmd, fieldsBeingFacetedForParticularTerms, fieldsBeingFacetedFlat, uiFacet,
							fieldName, fc, termsToCount);
				} else {
					// this is the most informative concepts facet
					SignificantTermsAggregation sigAgg = new SignificantTermsAggregation();
					sigAgg.field = uiFacet.getSource().getName();
					sigAgg.name = uiFacet.getId();
					serverCmd.addAggregationCommand(sigAgg);
				}
			} else {
				// We have an aggregation facet: We have to get facets for all
				// the element facets making up the aggregate.
				log.debug("Creating aggregation facet commands for aggregation facet {}.", uiFacet);
				Set<FacetLabels.General> aggregationLabels = uiFacet.getAggregationLabels();
				if (null == aggregationLabels)
					throw new IllegalStateException("Facet " + uiFacet
							+ " is an aggregation facet but does not define any facets to aggregate over.");
				List<Facet> aggregateFacets = facetService.getFacetsByLabels(aggregationLabels);
				for (Facet facet : aggregateFacets) {
					// If the aggregation facet defines a field name, then this
					// is a field other than the default facet
					// field. It is just the trunk of the facet-specific
					// field names and we must append the facet ID to get the
					// actual field names.
					// Otherwise, we directly use the original facet field name.
					String fieldName = null != uiFacet.getSource().getName()
							? uiFacet.getSource().getName() + facet.getId() : facet.getSource().getName();
					TermsAggregation fc = new TermsAggregation();
					fc.name = fieldName;
					// TODO magic number
					fc.size = 100;
					fc.field = fieldName;

					// We just get the top facet counts
					List<String> termsToCount = Collections.<String> emptyList();
					addFacetCommand(serverCmd, fieldsBeingFacetedForParticularTerms, fieldsBeingFacetedFlat, uiFacet,
							fieldName, fc, termsToCount);
				}
			}
		}

		// Only get the number of facet hits in general if we count facet terms
		// at all
		if (serverCmd.aggregationRequests != null) {
			// Facet-global counts. Don't do when there are only particular
			// facets
			// to count because then we have an update on a few facets only and
			// no
			// new document search.
			// if (noFacetsToCountDelivered)
			{
				TermsAggregation agg = new TermsAggregation();
				// TODO this possibly doesnt make sense any more, check
				agg.field = IIndexInformationService.Indices.All.conceptlist;
				agg.name = IIndexInformationService.Indices.All.conceptlist;
				// With the ontology terms we will have over 300 facets
				// already...
				agg.size = 500;
				serverCmd.addAggregationCommand(agg);
			}
		}

		// TODO repair
//		if (serverCmd.aggregationRequests == null && semCarrier.chainName.equals(FacetCountChain.class.getSimpleName())) {
//			log.debug("Chain {} is terminated because there are no facets to count.", semCarrier.chainName);
//			return true;
//		}

		return false;
	}

	protected void addFacetCommand(SearchServerRequest serverCmd, Set<String> fieldsBeingFacetedForParticularTerms,
			Set<String> fieldsBeingFacetedFlat, UIFacet uiFacet, String fieldName, TermsAggregation fc,
			Collection<String> termsToCount) {
		if (termsToCount.size() > 0 || uiFacet.isForcedToFlatFacetCounts()
				|| uiFacet.isFlat() || uiFacet.isAggregationFacet()) {
			boolean fcDoesAlreadyExist = false;
			if (termsToCount.size() > 0) {
				if (fieldsBeingFacetedForParticularTerms.contains(fieldName))
					fcDoesAlreadyExist = true;
				fieldsBeingFacetedForParticularTerms.add(fieldName);
			} else {
				if (fieldsBeingFacetedFlat.contains(fieldName))
					fcDoesAlreadyExist = true;
				fieldsBeingFacetedFlat.add(fieldName);
			}
			if (!fcDoesAlreadyExist)
				serverCmd.addAggregationCommand(fc);
			else
				log.debug("Facet command {} was not added because it does already exist.", fc);
		} else
			log.debug(
					"No facet command for facet {} added because it is hierarchic but neither has been forced to flat counts nor are there terms for which their counts are unknown.",
					uiFacet);
	}

}
