/**
 * QueryTranslationComponent.java
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

import org.apache.tapestry5.services.ApplicationStateManager;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.query.IQueryTranslationService;

/**
 * Should be largely obsolete as soon as there is a single SemedicoQuery class,
 * propably the current ParseTree class. This class should be able to produce
 * the correct Solr query on its own. It will even not be necessary to store the
 * query in the session since the SemedicoQuery object may save that itself.
 * 
 * @author faessler
 * 
 */
public class QueryTranslationComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface QueryTranslation {
	}
	
	private final IQueryTranslationService queryTranslationService;
	private final ApplicationStateManager asm;

	public QueryTranslationComponent(ApplicationStateManager asm,
			IQueryTranslationService queryTranslationService) {
		this.asm = asm;
		this.queryTranslationService = queryTranslationService;
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
		if (null == searchCarrier.queryCmd)
			throw new IllegalArgumentException("A non-null " + QueryAnalysisCommand.class.getName() + " is expected");
		
		Multimap<String, IFacetTerm> semedicoQuery = searchCarrier.searchCmd.semedicoQuery;
		String userQuery = searchCarrier.queryCmd.userQuery;
		if (null == semedicoQuery)
			throw new IllegalArgumentException("The class "
					+ getClass().getName()
					+ " expects a non-null Semedico query, but found none.");

		SolrSearchCommand solrCmd = searchCarrier.solrCmd;
		if (null == solrCmd) {
			solrCmd = new SolrSearchCommand();
			searchCarrier.solrCmd = solrCmd;
		}
		// TODO the userQuery is currently unused in the service. The goal was
		// to keep the userQuery for eventual spelling correction.
		// I think, 'SemedicoQuery' should be a class holding the abstract query
		// structure, e.g. the current Multimap or - better - the ParseTree -
		// and the original user Query and keeps a mapping from the user query
		// snippets to the terms they were mapped to. That way, we can display
		// to the user why he/she has exactly the SememdicoQuery he/she has.
		String solrQuery = queryTranslationService.createQueryFromTerms(semedicoQuery, userQuery);
		solrCmd.solrQuery = solrQuery;
		
		SearchState searchState = asm.get(SearchState.class);
		searchState.setSolrQueryString(solrQuery);
		
		return false;
	}

}
