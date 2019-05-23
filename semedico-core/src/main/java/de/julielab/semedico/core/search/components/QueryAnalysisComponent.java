/**
 * QueryAnalysisComponent.java
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

import org.slf4j.Logger;

import de.julielab.scicopia.core.elasticsearch.legacy.AbstractSearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchCarrier;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCommand;
import de.julielab.semedico.core.services.interfaces.IQueryAnalysisService;

/**
 * @author faessler
 * 
 */
public class QueryAnalysisComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface QueryAnalysis {
		//
	}

	private IQueryAnalysisService queryAnalysisService;
	private Logger log;

	public QueryAnalysisComponent(Logger log, IQueryAnalysisService queryAnalysisService) {
		this.log = log;
		this.queryAnalysisService = queryAnalysisService;
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
		List<QueryToken> tokens = semCarrier.getUserQuery();

		if (null == tokens) {
			throw new IllegalArgumentException("A list of " + QueryToken.class.getName()
					+ " is expected, but it was null.");
		}
		
		if (tokens.isEmpty()) {
			throw new IllegalArgumentException("The passed list of " + QueryToken.class.getName()
					+ " is invalid. The user query is empty.");
		}

		SearchState searchState = semCarrier.getSearchState();
		if (null == searchState) {
			throw new IllegalArgumentException(
					"The search state is null. However, it is required to store the parsed Semedico query.");
		}

		ParseTree parseTree = queryAnalysisService.analyseQueryString(tokens);

		// No query structure came out of the analysis process. Perhaps an empty query, perhaps only stopwords...?
		if (null == parseTree || parseTree.isEmpty()) {
			log.warn("The query analysis process produced an empty parse tree for the input {}", tokens);
			LegacySemedicoSearchResult errorResult = new LegacySemedicoSearchResult(semCarrier.getSearchCommand().getSemedicoQuery());
			errorResult.errorMessage =
					"The analysis of your query did not yield searchable items due to stop word removal. Please reformulate your query.";
			semCarrier.setResult(errorResult);
			return true;
		}

		SemedicoSearchCommand searchCmd = semCarrier.getSearchCommand();
		searchCmd.setSemedicoQuery(parseTree);
		searchState.setDisambiguatedQuery(parseTree);
		// TODO or rather set the whole user query object?
		searchState.setUserQueryString(null);

		return false;
	}
}
