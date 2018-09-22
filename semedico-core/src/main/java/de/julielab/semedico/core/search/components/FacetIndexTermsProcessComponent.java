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
import java.util.List;

import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerRequest;
import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.elastic.query.components.data.aggregation.ITermsAggregationUnit;
import de.julielab.elastic.query.components.data.aggregation.TermsAggregationResult;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCommand;

/**
 * @author faessler
 * 
 */
// TODO delete or make resultCollector
public class FacetIndexTermsProcessComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FacetIndexTermsProcess {
		//
	}


	public FacetIndexTermsProcessComponent(Logger log) {
		super(log);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.search.components.ISearchComponent#process(de.julielab
	 * .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoESSearchCarrier semCarrier = (SemedicoESSearchCarrier) searchCarrier;
		SearchServerRequest serverCmd = semCarrier.getSingleSearchServerRequest();
		SemedicoSearchCommand searchCmd = semCarrier.searchCmd;
		ISearchServerResponse serverResponse = semCarrier.getSingleSearchServerResponse();
		if (null == serverResponse)
			throw new IllegalArgumentException("The solr response must not be null, but it is.");
		List<String> termIds = new ArrayList<>();
		for (Facet facet : searchCmd.facetsToGetAllIndexTerms) {
			AggregationRequest aggCmd = serverCmd.aggregationRequests.get(FacetIndexTermsRetrievalComponent.NAME_PREFIX + facet.getSource().getName());
			// TODO repair? perhaps just use the existing FacetTermsResultCollector and delete this
			TermsAggregationResult aggregationResult = null;//(TermsAggregationResult) serverResponse.getAggregationResult(aggCmd);
			// Only take those facets into account that were meant for index term retrieval; actually there shouldn't be
			// other facets present when this component is employed, but you never know...
//			if (!ff.getName().startsWith(FacetIndexTermsRetrievalComponent.NAME_PREFIX))
//				continue;
			List<ITermsAggregationUnit> units = aggregationResult.getAggregationUnits();
			for(ITermsAggregationUnit unit : units) {
				termIds.add(String.valueOf(unit.getTerm()));
			}
		}
		// TODO works different now
//		semCarrier.result = new LegacySemedicoSearchResult(semCarrier.searchCmd.semedicoQuery);
//		((LegacySemedicoSearchResult)semCarrier.result).facetIndexTerms = termIds;

		return false;
	}
}
