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
package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;

import de.julielab.scicopia.core.elasticsearch.legacy.AbstractSearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchServerCommand;
import de.julielab.scicopia.core.parsing.QueryPriority;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchCarrier;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCommand;

/**
 * This class only combines the queries of all tokens, as is done in the final stage of the parser.
 * 
 * @author kampe
 * 
 */
public class QueryTranslationComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface QueryTranslation {
		//
	}

	private Logger log;

	public QueryTranslationComponent(Logger log
			) {
		this.log = log;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.search.components.ISearchComponent#process(de.
	 * julielab .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		SemedicoSearchCommand searchCmd = semCarrier.getSearchCommand();

		if (searchCmd == null) {
			throw new IllegalArgumentException("The query is null. Can't continue");
		}

		ParseTree semedicoQuery = searchCmd.getSemedicoQuery();

		// The things we want to assemble here - query, filters, ...
		QueryBuilder finalQuery = null;

		Map<String, QueryBuilder> namedQueries = new HashMap<>();

		SearchServerCommand serverCmd = semCarrier.getSingleSearchServerCommandOrCreate();

		List<QueryToken> tokens = semedicoQuery.getQueryTokens();
		boolean queriesStored = true;
		for (QueryToken token : tokens) {
			if (token.getQuery() == null) {
				queriesStored = false;
				break;
			}
		}

		if (!queriesStored) {
			serverCmd.query = finalQuery;
		} else {
			if (tokens.size() == 1) {
				serverCmd.query = tokens.get(0).getQuery();
			} else {
				BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
				
				for (QueryToken token : tokens) {
					QueryBuilder query = token.getQuery();
					QueryPriority priority = token.getPriority();
					if (priority == QueryPriority.MUST) {
						boolQuery.must(query);
					} else if (priority == QueryPriority.MUSTNOT) {
						if (!query.getName().equals("wrapper")) {
							BoolQueryBuilder temp = (BoolQueryBuilder) query;
							boolQuery.mustNot(temp.mustNot().get(0));
						} else {
							boolQuery.mustNot(query);
						}
						
					} else {
						boolQuery.should(query);
					}

				}

				log.debug("All queries were stored!");
				serverCmd.query = boolQuery;
			}
		}
		
		serverCmd.namedQueries = namedQueries;
		return false;
	}
}
