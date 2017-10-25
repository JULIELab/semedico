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
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.FieldTermItem;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.elastic.query.components.data.aggregation.ITermsAggregationUnit;
import de.julielab.elastic.query.components.data.aggregation.MaxAggregationResult;
import de.julielab.elastic.query.components.data.aggregation.TermsAggregationResult;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.query.FieldTermsQuery;
import de.julielab.semedico.core.search.results.FieldTermsRetrievalResult;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;

/**
 * @author faessler
 * 
 */
public class FieldTermsResultComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FieldTermsProcess {
		//
	}

	public FieldTermsResultComponent(Logger log) {
		super(log);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.search.components.ISearchComponent#process(de.
	 * julielab .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier<FieldTermsQuery, SemedicoSearchResult> semCarrier = castCarrier(searchCarrier);
		Supplier<ISearchServerResponse> s1 = () -> semCarrier.getSingleSearchServerResponse();
		Supplier<Map<String, AggregationRequest>> s2 = () -> semCarrier.serverRequests.get(0).aggregationRequests;
		Supplier<AggregationRequest> s3 = () -> s2.get().get(FieldTermsRetrievalPreparationComponent.AGG_FIELD_TERMS);
		stopIfError();

		checkNotNull(s1, "Search Server Response", s2, "Aggregation Commands", s3,
				"Aggregation Command with name " + FieldTermsRetrievalPreparationComponent.AGG_FIELD_TERMS);

		AggregationRequest fieldTermsAgg = s3.get();

		TermsAggregationResult fieldTermsAggResult = (TermsAggregationResult) s1.get()
				.getAggregationResult(fieldTermsAgg);
		List<ITermsAggregationUnit> terms = fieldTermsAggResult.getAggregationUnits();
		Builder<FieldTermItem> fieldTermStreamBuilder = Stream.builder();
		for (ITermsAggregationUnit termUnit : terms) {
			FieldTermItem fieldTermItem = new FieldTermItem();
			MaxAggregationResult docScore = (MaxAggregationResult) termUnit
					.getSubaggregationResult(FieldTermsRetrievalPreparationComponent.AGG_DOC_SCORE);
			fieldTermItem.term = termUnit.getTerm();
			fieldTermItem.setValue(FieldTermItem.ValueType.COUNT, termUnit.getCount());
			fieldTermItem.setValue(FieldTermItem.ValueType.MAX_DOC_SCORE, docScore.getValue());
			fieldTermStreamBuilder.accept(fieldTermItem);
		}

		semCarrier.result = new FieldTermsRetrievalResult(fieldTermStreamBuilder.build());

		return false;
	}

}
