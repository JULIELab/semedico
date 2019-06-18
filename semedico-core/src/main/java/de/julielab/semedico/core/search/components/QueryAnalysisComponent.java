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

import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.semedico.core.entities.state.SearchState;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCommand;
import de.julielab.semedico.core.services.query.IQueryAnalysisService;

/**
 * @author faessler
 * 
 */
public class QueryAnalysisComponent extends AbstractSearchComponent<SemedicoESSearchCarrier> {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface QueryAnalysis {
		//
	}

	// private final IQueryDisambiguationService queryDisambiguationService;
	private IQueryAnalysisService queryAnalysisService;
	private Logger log;

	// public QueryAnalysisComponent(
	// IQueryDisambiguationService queryDisambiguationService) {
	// this.queryDisambiguationService = queryDisambiguationService;
	//
	// }

	public QueryAnalysisComponent(Logger log, IQueryAnalysisService queryAnalysisService) {
		super(log);
		this.queryAnalysisService = queryAnalysisService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.search.components.ISearchComponent#process(de.julielab
	 * .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean processSearch(SemedicoESSearchCarrier semCarrier) {
		QueryAnalysisCommand queryCmd = semCarrier.queryAnalysisCmd;
		SearchState searchState = semCarrier.getSearchState();
		if (null == queryCmd)
			throw new IllegalArgumentException("An instance of " + QueryAnalysisCommand.class.getName()
					+ " is expected, but it was null.");
		if (null == queryCmd.userQuery || queryCmd.userQuery.tokens.isEmpty())
			throw new IllegalArgumentException("The passed " + QueryAnalysisCommand.class.getName()
					+ " is invalid. The user query is empty.");
		SemedicoSearchCommand searchCmd = semCarrier.searchCmd;
//		if (null == searchCmd) {
//			searchCmd = new SemedicoSearchCommand();
//			semCarrier.searchCmd = searchCmd;
//		}
		if (null == searchState)
			throw new IllegalArgumentException(
					"The search state is null. However, it is required to store the parsed Semedico query.");

		ParseTree parseTree = null;
		try {
			parseTree = queryAnalysisService.analyseQueryString(queryCmd.userQuery, false);

			// No query structure came out of the analysis process. Perhaps an empty query, perhaps only stopwords...?
			if (null == parseTree || parseTree.isEmpty()) {
				log.warn("The query analysis process produced an empty parse tree for the input {}", queryCmd.userQuery.tokens);
				LegacySemedicoSearchResult errorResult = new LegacySemedicoSearchResult(semCarrier.searchCmd.semedicoQuery);
				errorResult.errorMessage =
						"The analysis of your query did not yield searchable items due to stop word removal. Please reformulate your query.";
				// TODO adapt
//				semCarrier.result = errorResult;
				return true;
			}

			searchCmd.semedicoQuery = parseTree;
			searchState.setDisambiguatedQuery(parseTree);
			searchState.setUserQueryString(queryCmd.userQuery);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return false;
	}
}
