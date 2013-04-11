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
package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tapestry5.services.ApplicationStateManager;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.query.IQueryDisambiguationService;
import de.julielab.semedico.query.TermAndPositionWrapper;

/**
 * @author faessler
 * 
 */
public class QueryAnalysisComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface QueryAnalysis {
	}

	private final IQueryDisambiguationService queryDisambiguationService;
	private final ApplicationStateManager asm;

	public QueryAnalysisComponent(ApplicationStateManager asm,
			IQueryDisambiguationService queryDisambiguationService) {
		this.asm = asm;
		this.queryDisambiguationService = queryDisambiguationService;

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
		QueryAnalysisCommand queryCmd = searchCarrier.queryCmd;
		if (null == queryCmd)
			throw new IllegalArgumentException("An instance of "
					+ SemedicoSearchCommand.class.getName()
					+ " is expected, but it was null.");
		if (StringUtils.isEmpty(queryCmd.userQuery))
			throw new IllegalArgumentException("The passed "
					+ SemedicoSearchCommand.class.getName()
					+ " is invalid. The user query string is empty.");
		SemedicoSearchCommand searchCmd = searchCarrier.searchCmd;
		if (null == searchCmd) {
			searchCmd = new SemedicoSearchCommand();
			searchCarrier.searchCmd = searchCmd;
		}

		Multimap<String, TermAndPositionWrapper> analysisResult = queryDisambiguationService
				.disambiguateQuery(queryCmd.userQuery,
						new ImmutablePair<String, Integer>(
								queryCmd.selectedTermId,
								queryCmd.facetIdForSelectedTerm));

		// --------------------------------------
		// TODO this is for legacy reasons until the new query structure can be
		// used in the whole of Semedico.
		Multimap<String, IFacetTerm> disambiguatedQuery = HashMultimap.create();
		for (String key : analysisResult.keySet()) {
			Collection<TermAndPositionWrapper> collection = analysisResult
					.get(key);
			for (TermAndPositionWrapper wrapper : collection) {
				IFacetTerm term = wrapper.getTerm();
				disambiguatedQuery.put(key, term);
				// searchState.getQueryTermFacetMap().put(term,
				// term.getFirstFacet());
			}
		}
		// --------------------------------------
		searchCmd.semedicoQuery = disambiguatedQuery;

		// Write the query analysis result into the session so other components
		// have easy access.
		SearchState searchState = asm.get(SearchState.class);
		searchState.setDisambiguatedQuery(disambiguatedQuery);
		searchState.setUserQueryString(queryCmd.userQuery);
		
		Map<IFacetTerm, Facet> queryTermFacetMap = searchState
				.getQueryTermFacetMap();
		for (IFacetTerm queryTerm : disambiguatedQuery.values())
			queryTermFacetMap.put(queryTerm, queryTerm.getFirstFacet());

		return false;
	}

}
