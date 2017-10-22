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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerCommand;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.query.ISemedicoQuery;
import de.julielab.semedico.core.query.translation.BoolDocumentMetaTranslator;
import de.julielab.semedico.core.query.translation.IQueryTranslator;
import de.julielab.semedico.core.query.translation.SearchTask;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchResult;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.SearchService.SearchOption;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

/**
 * Should be largely obsolete as soon as there is a single SemedicoQuery class,
 * propably the current ParseTree class. This class should be able to produce
 * the correct Solr query on its own. It will even not be necessary to store the
 * query in the session since the SemedicoQuery object may save that itself.
 * 
 * @author faessler
 * 
 */
public class QueryTranslationComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface QueryTranslation {
		//
	}

	private IQueryTranslator queryTranslationChain;
	private BoolDocumentMetaTranslator documentMetaTranslator;
	private String literatureIndexName;

	public QueryTranslationComponent(Logger log, @Primary IQueryTranslator queryTranslationChain, @Symbol(SemedicoSymbolConstants.BIOMED_PUBLICATIONS_INDEX_NAME) String documentsIndexName
	) {
		super(log);
		this.queryTranslationChain = queryTranslationChain;
		this.literatureIndexName = documentsIndexName;
		this.documentMetaTranslator = new BoolDocumentMetaTranslator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.search.components.ISearchComponent#process(de.
	 * julielab .semedico.search.components.SearchCarrier)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier<? extends ISemedicoQuery, ? extends SemedicoSearchResult> semCarrier = (SemedicoSearchCarrier<? extends ISemedicoQuery, ? extends SemedicoSearchResult>) searchCarrier;
		ISemedicoQuery searchQuery = semCarrier.query;
		if (null == searchQuery) {
			throw new IllegalArgumentException("The query is null. Can't continue");
		}
		if (searchQuery.getTask() == null) {
			throw new IllegalArgumentException("The search task is null. Aborting.");
		}
		// The things we want to assemble here - query, filters, ...
		SearchServerQuery finalQuery = null;
		SearchServerQuery facetConceptsPostFilterQuery = null;

		Set<SearchTask> tasks = new HashSet<>();
		tasks.add(searchQuery.getTask());
		Set<String> indexTypes = new HashSet<>();
		 if (null != searchQuery && searchQuery.getIndexTypes() != null
				&& !searchQuery.getIndexTypes().isEmpty()) {
			for (String type : searchQuery.getIndexTypes()) {
				indexTypes.add(literatureIndexName + "." + type);
			}
		} else {
			throw new IllegalArgumentException("No index types given that should be searched.");
		}

		List<SearchServerQuery> queries = new ArrayList<>();
		Map<String, SearchServerQuery> namedQueries = new HashMap<>();
		queryTranslationChain.translate(searchQuery, queries, namedQueries);

		if (queries.isEmpty())
			log.warn("No search server queries have been created for query {}", searchQuery);
		else
			finalQuery = documentMetaTranslator.combine(queries);

		SearchServerCommand serverCmd = semCarrier.getSingleSearchServerCommandOrCreate();

		if (searchQuery.getIndex() == null)
			throw new IllegalArgumentException("The given query does not specify an index to search on: " + searchQuery);
		
		serverCmd.query = finalQuery;
		serverCmd.index = searchQuery.getIndex();
		serverCmd.namedQueries = namedQueries;
		serverCmd.postFilterQuery = facetConceptsPostFilterQuery;

		return searchQuery.getSearchOptions().contains(SearchOption.RETURN_SERVER_QUERY) ? true : false;
	}
}
