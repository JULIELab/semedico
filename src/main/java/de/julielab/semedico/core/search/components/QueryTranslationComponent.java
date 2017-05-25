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
import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerCommand;
import de.julielab.elastic.query.components.data.query.BoolClause;
import de.julielab.elastic.query.components.data.query.BoolQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.elastic.query.components.data.query.TermQuery;
import de.julielab.elastic.query.components.data.query.BoolClause.Occur;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.DocumentQuery;
import de.julielab.semedico.core.query.ISemedicoQuery;
import de.julielab.semedico.core.query.translation.BoolDocumentMetaTranslator;
import de.julielab.semedico.core.query.translation.IQueryTranslator;
import de.julielab.semedico.core.query.translation.SearchTask;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCommand;
import de.julielab.semedico.core.search.components.data.SemedicoSearchResult;
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

	// private final IQueryTranslationService queryTranslationService;
	private IQueryTranslator queryTranslationChain;
	private Logger log;
	private BoolDocumentMetaTranslator documentMetaTranslator;

	public QueryTranslationComponent(Logger log, @Primary IQueryTranslator queryTranslationChain
	// , IQueryTranslationService queryTranslationService
	) {
		this.log = log;
		this.queryTranslationChain = queryTranslationChain;
		// this.queryTranslationService = queryTranslationService;
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
		SearchState searchState = semCarrier.searchState;
		SemedicoSearchCommand searchCmd = semCarrier.searchCmd;
		ISemedicoQuery searchQuery = semCarrier.query;
		// if (null == searchCarrier.queryCmd)
		// throw new IllegalArgumentException("A non-null "
		// + QueryAnalysisCommand.class.getName() + " is expected");
		// if (null == searchState)
		// throw new IllegalArgumentException(
		// "The search state is null. But it is required to store the translated
		// query.");
		if (null == searchQuery && searchCmd == null) {
			throw new IllegalArgumentException("The query is null. Can't continue");
		}
		if (null == searchCmd) {
			// legacy support - just ignore the searchCmd
			searchCmd = new SemedicoSearchCommand();
			// throw new IllegalArgumentException("The " +
			// SemedicoSearchCommand.class.getName()
			// + " is null. However, it is required to get the fields to search
			// on from.");
		}
		// if (null == searchCmd.semedicoQuery) {
		// log.debug("The class " + getClass().getName()
		// + " expects a non-null query ParseTree, but found none. No server
		// query will be created by this component.");
		// return false;
		// }

		ParseTree semedicoQuery = searchCmd.semedicoQuery.compress();

		// The things we want to assemble here - query, filters, ...
		SearchServerQuery finalQuery = null;
		SearchServerQuery facetConceptsPostFilterQuery = null;

		// TODO legacy support, remove when all searches have the appropriate query object
		if (searchQuery == null) {
			searchQuery = new DocumentQuery(semedicoQuery, new HashSet<>(searchCmd.searchFieldFilter));
			searchQuery.setIndexTypes(searchCmd.indexTypes);
			((DocumentQuery) searchQuery).setTask(searchCmd.task);
			((DocumentQuery) searchQuery).setIndex(IIndexInformationService.Indexes.documents);
		}
		Set<SearchTask> tasks = new HashSet<>();
		// TODO when queries are completely implemented, raise an error if the task is null
		if (searchQuery.getTask() != null)
			tasks.add(searchQuery.getTask());
		Set<String> indexTypes = new HashSet<>();
		// TODO the search command should disappear
		if (null != searchCmd.indexTypes && !searchCmd.indexTypes.isEmpty()) {
			for (String type : searchCmd.indexTypes) {
				indexTypes.add(IIndexInformationService.Indexes.documents + "." + type);
			}

		} else if (null != searchQuery && searchQuery.getIndexTypes() != null
				&& !searchQuery.getIndexTypes().isEmpty()) {
			for (String type : searchQuery.getIndexTypes()) {
				indexTypes.add(IIndexInformationService.Indexes.documents + "." + type);
			}
		} else {
			indexTypes.add(IIndexInformationService.Indexes.documents + "."
					+ IIndexInformationService.Indexes.DocumentTypes.medline);
			indexTypes.add(IIndexInformationService.Indexes.documents + "."
					+ IIndexInformationService.Indexes.DocumentTypes.pmc);
		}
		List<SearchServerQuery> queries = new ArrayList<>();
		Map<String, SearchServerQuery> namedQueries = new HashMap<>();
		if (null == tasks || tasks.isEmpty())
			throw new IllegalArgumentException("No tasks specified");
		queryTranslationChain.translate(searchQuery, tasks, indexTypes, queries, namedQueries);

		if (queries.isEmpty())
			log.warn("No queries have been created from ParseTree {}", semedicoQuery);
		else
			finalQuery = documentMetaTranslator.combine(queries,
					searchState != null ? searchState.getSelectedFacetConcepts() : null);

		SearchServerCommand serverCmd = semCarrier.getSingleSearchServerCommandOrCreate();

		// TODO should be removed from this component
		if (semCarrier.searchCmd.docSize >= 0)
			serverCmd.rows = semCarrier.searchCmd.docSize;
		// TODO the userQuery is currently unused in the service. The goal was
		// to keep the userQuery for eventual spelling correction.
		// I think, 'SemedicoQuery' should be a class holding the abstract query
		// structure, e.g. the current Multimap or - better - the ParseTree -
		// and the original user Query and keeps a mapping from the user query
		// snippets to the terms they were mapped to. That way, we can display
		// to the user why he/she has exactly the SememdicoQuery he/she has.
		// String solrQuery = queryTranslationService.createQueryFromTerms(
		// semedicoQuery, userQuery, searchCmd.searchFields);

		// SearchServerQuery query =
		// queryTranslationService.createQuery(semedicoQuery,
		// searchCmd.searchFieldFilter, null);
		// TODO handle multiple queries
		serverCmd.query = finalQuery;
		serverCmd.index = searchQuery.getIndex();
		serverCmd.namedQueries = namedQueries;
		serverCmd.postFilterQuery = facetConceptsPostFilterQuery;
		// if (null != searchState)
		// searchState.setSearchServerQuery(searchServerQuery);

		// if (null != searchState) {
		// SortCriteriumEvents sortCriterium =
		// searchState.getSortCriteriumEvents();
		// if (sortCriterium == SortCriteriumEvents.CERTAINTY_CONTROVERSIAL ||
		// sortCriterium == SortCriteriumEvents.CERTAINTY_HIGH
		// || sortCriterium == SortCriteriumEvents.CERTAINTY_LOW
		// || sortCriterium == SortCriteriumEvents.CERTAINTY_MID) {
		// serverCmd.serverQuery =
		// queryTranslationService.createQuery(semedicoQuery,
		// searchCmd.searchFields,
		// new LikelihoodBooster(sortCriterium));
		// searchState.setSearchServerQuery(serverCmd.serverQuery);
		// } else
		// serverCmd.serverQuery = searchServerQuery;
		// } else
		// serverCmd.serverQuery = searchServerQuery;
		return false;
	}
}
