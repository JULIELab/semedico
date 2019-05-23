/**
 * FacetIndexTermsProcessComponent.java
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

import org.slf4j.Logger;

import de.julielab.scicopia.core.elasticsearch.legacy.AbstractSearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.IFacetField;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchServerResponse;
import de.julielab.scicopia.core.elasticsearch.legacy.TermCountCursor;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchCarrier;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;

/**
 * @author faessler
 * 
 */
public class FacetIndexTermsProcessComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FacetIndexTermsProcess {
		//
	}

	private final Logger log;

	public FacetIndexTermsProcessComponent(Logger log) {
		this.log = log;

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
		if (null == serverResponse)
			throw new IllegalArgumentException("The solr response must not be null, but it is.");
		if (null == serverResponse.getFacetFields()) {
			log.warn("The Solr response does not contain facet counts for any fields.");
			return false;
		}
		List<String> termIds = new ArrayList<>();
		List<IFacetField> facetFields = serverResponse.getFacetFields();
		for (IFacetField ff : facetFields) {
			TermCountCursor cursor = ff.getFacetValues();
			while (cursor.forwardCursor()) {
				termIds.add(cursor.getName());
			}
		}

		LegacySemedicoSearchResult result = new LegacySemedicoSearchResult(semCarrier.getSearchCommand().getSemedicoQuery());
		result.facetIndexTerms = termIds;
		semCarrier.setResult(result);

		return false;
	}
}
