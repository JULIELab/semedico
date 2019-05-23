/**
 * FieldTermsProcessComponent.java
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
import java.util.Map;

import de.julielab.scicopia.core.elasticsearch.legacy.AbstractSearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.FieldTermItem;
import de.julielab.scicopia.core.elasticsearch.legacy.AggregationCommand;
import de.julielab.scicopia.core.elasticsearch.legacy.ITermsAggregationUnit;
import de.julielab.scicopia.core.elasticsearch.legacy.MaxAggregationResult;
import de.julielab.scicopia.core.elasticsearch.legacy.TermsAggregationResult;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchServerResponse;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchCarrier;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;

/**
 * @author faessler
 * 
 */
public class FieldTermsProcessComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FieldTermsProcess {
		//
	}

	public FieldTermsProcessComponent() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.search.components.ISearchComponent#process(de.julielab
	 * .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		ISearchServerResponse serverResponse = semCarrier.getSingleSearchServerResponse();
		if (null == serverResponse) {
			throw new IllegalArgumentException("The search server response must not be null, but it is.");
		}
		
		Map<String, AggregationCommand> aggCmds = semCarrier.serverCmds.get(0).aggregationCmds;
		if (null == aggCmds || null == aggCmds.get(FieldTermsRetrievalPreparationComponent.AGG_FIELD_TERMS)) {
			throw new IllegalArgumentException(
					"No field terms process aggregation command found, thus cannot get results.");
		}
		
		AggregationCommand fieldTermsAgg = aggCmds.get(FieldTermsRetrievalPreparationComponent.AGG_FIELD_TERMS);
		if (null == fieldTermsAgg) {
			throw new IllegalArgumentException(
					"Search server response did not contain results for field terms aggregation although the aggregation command was given. Check the server logs for error notifications");
		}
		
		TermsAggregationResult fieldTermsAggResult =
				(TermsAggregationResult) serverResponse.getAggregationResult(fieldTermsAgg);
		List<ITermsAggregationUnit> terms = fieldTermsAggResult.getAggregationUnits();
		List<FieldTermItem> termItems = new ArrayList<>();
		for (ITermsAggregationUnit termUnit : terms) {
			FieldTermItem fieldTermItem = new FieldTermItem();
			MaxAggregationResult docScore =
					(MaxAggregationResult) termUnit
							.getSubaggregationResult(FieldTermsRetrievalPreparationComponent.AGG_DOC_SCORE);
			fieldTermItem.term = termUnit.getTerm();
			fieldTermItem.setValue(FieldTermItem.ValueType.COUNT, termUnit.getCount());
			fieldTermItem.setValue(FieldTermItem.ValueType.MAX_DOC_SCORE, docScore.getValue());
			termItems.add(fieldTermItem);
		}

		LegacySemedicoSearchResult result = 
				new LegacySemedicoSearchResult(semCarrier.getSearchCommand().getSemedicoQuery());
		result.fieldTerms = termItems;
		semCarrier.setResult(result);

		return false;
	}
}
