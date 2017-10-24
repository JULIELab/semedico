/**
 * SearchService.java
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
package de.julielab.semedico.core.services;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.collections.map.Flat3Map;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.services.ParallelExecutor;

import de.julielab.elastic.query.components.ISearchComponent;
import de.julielab.semedico.core.search.annotations.SearchChain;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.query.FieldTermsQuery;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.query.WrappingQuery;
import de.julielab.semedico.core.search.results.SingleSearchResult;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.results.SemedicoResultCollection;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;
import de.julielab.semedico.core.services.interfaces.ISearchService;

/**
 * @author faessler
 * 
 */
public class SearchService implements ISearchService {

	public enum SearchOption {
		/**
		 * Create a query that does not return any stored fields or
		 * aggregations, just count the number of hits as quickly as possible.
		 * Corresponds to {@link #NO_FIELDS}, {@link #NO_AGGREGATIONS} and
		 * {@link #NO_HIGHLIGHTING}.
		 */
		HIT_COUNT,
		/**
		 * Build the full query with all requested fields, aggregations and
		 * everything else.
		 */
		FULL,
		/**
		 * Build the query but do not issue it. Useful for {@link WrappingQuery}
		 * to get the desired query (e.g. {@link FieldTermsQuery}).
		 */
		RETURN_SERVER_QUERY,
		/**
		 * Do not return stored fields from a query.
		 */
		NO_FIELDS,
		/**
		 * Do not create aggregations for a query.
		 */
		NO_AGGREGATIONS, NO_HIGHLIGHTING
	}

	private ParallelExecutor executor;
	private ISearchComponent searchChain;

	public SearchService(ParallelExecutor executor, @SearchChain ISearchComponent searchChain) {
		this.executor = executor;
		this.searchChain = searchChain;
	}

	@Override
	public Future<SemedicoResultCollection> search(List<ISemedicoQuery> queries,
			List<EnumSet<SearchOption>> searchOptionList,
			@SuppressWarnings("unchecked") List<SearchResultCollector<? extends SemedicoSearchResult>>... collectorLists) {
		return executor.invoke(prepareSearch(queries, searchOptionList, collectorLists));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Future<SemedicoResultCollection> search(ISemedicoQuery query, EnumSet<SearchOption> searchOptions,
			SearchResultCollector<? extends SemedicoSearchResult>... collectors) {
		return executor.invoke(prepareSearch(query, searchOptions, collectors));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R extends SemedicoSearchResult> Future<SingleSearchResult<R>> search(ISemedicoQuery query,
			EnumSet<SearchOption> searchOptions, SearchResultCollector<R> collector) {
		Invokable<SemedicoResultCollection> prepare = prepareSearch(query, searchOptions,
				new SearchResultCollector[] { collector });
		Future<SingleSearchResult<R>> result = executor.invoke(() -> {
			SemedicoResultCollection resultCollection = prepare.invoke();
			SingleSearchResult<R> oneResult = new SingleSearchResult<>((R) resultCollection.getResult(collector.getName()));
			return oneResult;
		});
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private Invokable<SemedicoResultCollection> prepareSearch(ISemedicoQuery query, EnumSet<SearchOption> searchOptions,
			SearchResultCollector<? extends SemedicoSearchResult>... collectors) {
		return () -> {
			SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(
					"Single query search with " + collectors.length + " result collectors");
			carrier.queries = Collections.singletonList(query);
			carrier.searchOptions = Collections.singletonList(searchOptions);
			searchChain.process(carrier);
			SemedicoResultCollection resultCollection = new SemedicoResultCollection(
					collectors.length <= 3 ? new Flat3Map() : new HashMap<>(collectors.length));
			for (int i = 0; i < collectors.length; i++) {
				SearchResultCollector<? extends SemedicoSearchResult> collector = collectors[i];
				SemedicoSearchResult result = collector.collectResult(carrier, carrier.serverResponses.get(0));
				resultCollection.put(collector.getName(), result);
			}
			return resultCollection;
		};
	}


	@SuppressWarnings("unchecked")
	private Invokable<SemedicoResultCollection> prepareSearch(List<ISemedicoQuery> queries,
			List<EnumSet<SearchOption>> searchOptionList,
			List<SearchResultCollector<? extends SemedicoSearchResult>>... collectorLists) {
		return () -> {
			SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(
					"Multiple query search of " + queries.size() + " queries");
			carrier.queries = queries;
			carrier.searchOptions = searchOptionList;

			searchChain.process(carrier);
			SemedicoResultCollection resultCollection = new SemedicoResultCollection(
					collectorLists.length <= 3 && collectorLists[0].size() <= 3 ? new Flat3Map() : new HashMap<>());
			for (int i = 0; i < collectorLists.length; i++) {
				List<SearchResultCollector<? extends SemedicoSearchResult>> collectors = collectorLists[i];
				for (int j = 0; j < collectors.size(); j++) {
					SearchResultCollector<? extends SemedicoSearchResult> collector = collectors.get(j);
					SemedicoSearchResult result = collector.collectResult(carrier, carrier.serverResponses.get(i));
					resultCollection.put(collector.getName(), result);
				}
			}
			return resultCollection;
		};
	}

}