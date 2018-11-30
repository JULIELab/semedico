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
import java.util.*;

import de.julielab.semedico.core.search.components.data.ISemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import de.julielab.semedico.core.search.query.AbstractSemedicoElasticQuery;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.query.TranslatedQuery;
import de.julielab.semedico.core.search.query.translation.BoolDisjunctMetaTranslator;
import de.julielab.semedico.core.search.query.translation.IQueryTranslator;
import de.julielab.semedico.core.services.SemedicoCoreModule;

/**
 * Should be largely obsolete as soon as there is a single SemedicoQuery class,
 * propably the current ParseTree class. This class should be able to produce
 * the correct Solr query on its own. It will even not be necessary to store the
 * query in the session since the SemedicoQuery object may save that itself.
 * 
 * @author faessler
 * 
 */
public class QueryTranslationComponent extends AbstractSearchComponent<SemedicoESSearchCarrier> {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface QueryTranslation {
		//
	}

	private IQueryTranslator queryTranslationChain;
	private BoolDisjunctMetaTranslator documentMetaTranslator;

	public QueryTranslationComponent(Logger log, @Primary IQueryTranslator queryTranslationChain) {
		super(log);
		this.queryTranslationChain = queryTranslationChain;
		this.documentMetaTranslator = new BoolDisjunctMetaTranslator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.search.components.ISearchComponent#process(de.
	 * julielab .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean processSearch(SemedicoESSearchCarrier searchCarrier) {
		SemedicoESSearchCarrier semCarrier = (SemedicoESSearchCarrier) searchCarrier;
		for (AbstractSemedicoElasticQuery searchQuery : semCarrier.getQueries()) {
			if (null == searchQuery) {
				throw new IllegalArgumentException("The query is null. Can't continue");
			}

			if (searchQuery.getIndex() == null || searchQuery.getIndex().trim().isEmpty()) {
				throw new IllegalArgumentException("No index given that should be searched.");
			}

			// --- QUERY TRANSLATION ---
			List<SearchServerQuery> queries = new ArrayList<>();
			Map<String, SearchServerQuery> namedQueries = new HashMap<>();
			queryTranslationChain.translate(searchQuery, queries, namedQueries);

			SearchServerQuery finalQuery;
			if (queries.isEmpty()) {
				log.warn("No search server queries have been created for query {}. Terminating the search chain.", searchQuery);
				searchCarrier.setErrorMessages(Arrays.asList("No search server query was created."));
				return true;
			}
			else {
				// This is no service but a simple class instantiated in the
				// constructor.
				// Its purpose is to create a single query out of multiple
				// queries that might have been returned by the query
				// translators. This happens when searching on multiple scopes,
				// for example.
				finalQuery = documentMetaTranslator.combine(queries);
			}
			// --- END QUERY TRANSLATION ---

			SemedicoCoreModule.searchTraceLog.debug("Final ElasticSearch query: {}", finalQuery);

			semCarrier.addTranslatedQuery(new TranslatedQuery(finalQuery, namedQueries));
		}
		return false;
	}
}
