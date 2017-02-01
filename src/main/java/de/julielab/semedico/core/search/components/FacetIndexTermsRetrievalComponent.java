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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.FacetCommand;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerCommand;
import de.julielab.elastic.query.components.data.query.MatchAllQuery;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCommand;

/**
 * @author faessler
 * @deprecated This component does nothing more than to create a FacetCommand for the requested facets. This can be done directly in the SearchService.
 */
@Deprecated
public class FacetIndexTermsRetrievalComponent extends AbstractSearchComponent {

	public static String NAME_PREFIX = "allfieldterms_";
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface FacetIndexTermsRetrieval {
		//
	}

	@SuppressWarnings("unused")
	private final Logger log;

	public FacetIndexTermsRetrievalComponent(Logger log) {
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
		SemedicoSearchCommand searchCmd = semCarrier.searchCmd;
		SearchServerCommand serverCmd = semCarrier.getSingleSearchServerCommandOrCreate();

		MatchAllQuery matchAllQuery = new MatchAllQuery();
		serverCmd.query = matchAllQuery;
		serverCmd.index = searchCmd.index;

		for (Facet facet : searchCmd.facetsToGetAllIndexTerms) {
			if (null == facet)
				continue;
			FacetCommand fc = new FacetCommand();
			fc.name = NAME_PREFIX + facet.getSource().getName();
			fc.addFacetField(facet.getSource().getName());
			fc.limit = -1;
			serverCmd.addFacetCommand(fc);
		}

		return false;
	}

}
