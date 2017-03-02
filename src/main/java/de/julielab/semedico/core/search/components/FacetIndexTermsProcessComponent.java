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

import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.IFacetField;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.elastic.query.util.TermCountCursor;
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
			// Only take those facets into account that were meant for index term retrieval; actually there shouldn't be
			// other facets present when this component is employed, but you never know...
//			if (!ff.getName().startsWith(FacetIndexTermsRetrievalComponent.NAME_PREFIX))
//				continue;
			TermCountCursor cursor = ff.getFacetValues();
			while (cursor.forwardCursor()) {
				termIds.add(cursor.getName());
			}
		}

		semCarrier.result = new LegacySemedicoSearchResult(semCarrier.searchCmd.semedicoQuery);
		((LegacySemedicoSearchResult)semCarrier.result).facetIndexTerms = termIds;

		return false;
	}
}
