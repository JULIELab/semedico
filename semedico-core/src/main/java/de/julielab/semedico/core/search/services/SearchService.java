/**
 * SearchService.java
 * <p>
 * Copyright (c) 2013, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * <p>
 * Author: faessler
 * <p>
 * Current version: 1.0
 * Since version:   1.0
 * <p>
 * Creation date: 09.04.2013
 */

/**
 *
 */
package de.julielab.semedico.core.search.services;

import de.julielab.elastic.query.components.ISearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.search.ServerType;
import de.julielab.semedico.core.search.annotations.SearchChain;
import de.julielab.semedico.core.search.annotations.TopicModelSearchChain;
import de.julielab.semedico.core.search.components.data.ISemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import de.julielab.semedico.core.search.components.data.TopicModelSearchCarrier;
import de.julielab.semedico.core.search.query.AbstractSemedicoElasticQuery;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.query.TopicModelQuery;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.results.SemedicoResultCollection;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;
import de.julielab.semedico.core.search.results.SingleSearchResult;
import de.julielab.semedico.core.util.SearchException;
import de.julielab.semedico.core.util.SemedicoRuntimeException;
import org.apache.commons.collections4.map.Flat3Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.services.ParallelExecutor;

import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

/**
 * @author faessler
 */
public class SearchService implements ISearchService {

    private ParallelExecutor executor;
    private ISearchComponent<SemedicoESSearchCarrier> elasticChain;
    private ISearchComponent<TopicModelSearchCarrier> topicModelChain;

    public SearchService(ParallelExecutor executor, @SearchChain ISearchComponent<SemedicoESSearchCarrier> elasticChain, @TopicModelSearchChain ISearchComponent<TopicModelSearchCarrier> topicModelChain) {
        this.executor = executor;
        this.elasticChain = elasticChain;
        this.topicModelChain = topicModelChain;
    }

    @Override
    public Future<SemedicoResultCollection> search(List<ISemedicoQuery> queries,
                                                   List<EnumSet<SearchOption>> searchOptionList,
                                                   List<List<SearchResultCollector<? super ISemedicoSearchCarrier<?, ?>, ? super SemedicoSearchResult>>> collectorLists) {
        return executor.invoke(prepareSearch(queries, searchOptionList, collectorLists));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Future<SemedicoResultCollection> search(ISemedicoQuery query, EnumSet<SearchOption> searchOptions,
                                                   SearchResultCollector<? super ISemedicoSearchCarrier<?, ?>, ? super SemedicoSearchResult>... collectors) {
        return executor.invoke(prepareSearch(query, searchOptions, collectors));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends ISemedicoSearchCarrier<?, ?>, R extends SemedicoSearchResult> Future<SingleSearchResult<R>> search(ISemedicoQuery query,
                                                                                 EnumSet<SearchOption> searchOptions, SearchResultCollector<C, R> collector) {
        Invokable<SemedicoResultCollection> prepare = prepareSearch(query, searchOptions,
                new SearchResultCollector[]{collector});
        return executor.invoke(() -> {
            SemedicoResultCollection resultCollection = prepare.invoke();
            SingleSearchResult<R> oneResult = new SingleSearchResult<>((R) resultCollection.getResult(collector.getName()));
            oneResult.setSearchCarrier(resultCollection.getResult(collector.getName()).getSearchCarrier());
            return oneResult;
        });
    }

    /**
     * Search for a single query.
     *
     * @param query         The query.
     * @param searchOptions Search options.
     * @param collectors    Result collections.
     * @return The collected results.
     */
    private Invokable<SemedicoResultCollection> prepareSearch(ISemedicoQuery query, EnumSet<SearchOption> searchOptions,
                                                              @SuppressWarnings("unchecked") SearchResultCollector<? super ISemedicoSearchCarrier<?, ?>, ? super SemedicoSearchResult>... collectors) {
        return () -> {
            ISemedicoSearchCarrier<?, ?> carrier;
            String chainName = "Single %s query search with " + collectors.length + " result collectors";
            boolean error;
            switch (query.getServerType()) {
                case TOPIC_MODEL:
                    TopicModelSearchCarrier tmCarrier = new TopicModelSearchCarrier(String.format(chainName, "topic model"));
                    carrier = tmCarrier;
                    error = topicModelChain.process(tmCarrier);
                    break;
                case ELASTIC_SEARCH:
                    SemedicoESSearchCarrier esCarrier = new SemedicoESSearchCarrier(
                            String.format(chainName, "elastic search"));
                    esCarrier.addQuery((AbstractSemedicoElasticQuery) query);
                    esCarrier.addSearchOptions(searchOptions);
                    carrier = esCarrier;
                    error = elasticChain.process(esCarrier);
                    break;
                default:
                    throw new IllegalArgumentException("The search server type " + query.getServerType() + " is not supported.");
            }

            if (error)
                throw new SemedicoRuntimeException(new SearchException(carrier.getFirstError()));
            SemedicoResultCollection resultCollection = new SemedicoResultCollection(
                    collectors.length <= 3 ? new Flat3Map<>() : new HashMap<>(collectors.length));
            for (int i = 0; i < collectors.length; i++) {
                SearchResultCollector<? super ISemedicoSearchCarrier<?, ?>,? super SemedicoSearchResult> collector = collectors[i];
                SemedicoSearchResult result = collector.collectResult(carrier, 0);
                resultCollection.put(collector.getName(), result);
            }
            return resultCollection;
        };
    }

    /**
     * For multiple queries. All input lists are required to be parallel.
     *
     * @param queries          The queries.
     * @param searchOptionList The options for each query.
     * @param collectorLists   The result collectors for each query.
     * @return The collected results.
     */
    private Invokable<SemedicoResultCollection> prepareSearch(List<ISemedicoQuery> queries,
                                                              List<EnumSet<SearchOption>> searchOptionList,
                                                              List<List<SearchResultCollector<? super ISemedicoSearchCarrier<?, ?>, ? super SemedicoSearchResult>>> collectorLists) {
        return () -> {
            // This is the object we want to have: The collection of search results. It will be filled in the loop
            // below.
            SemedicoResultCollection resultCollection = new SemedicoResultCollection(
                    collectorLists.size() <= 3 && collectorLists.get(0).size() <= 3 ? new Flat3Map<>() : new HashMap<>());

            // Different search technologies have different capabilities. For each search technology, there is a
            // specific implementation of ISemedicoSearchCarrier. We do this to not lose ourselves is abstraction
            // layers without any clue what's going on at a specific code location. We try to be specific when it'
            // possible.
            // First, group the query indexes by search technology. Since queries, searchOptionList and collectorLists
            // are parallel, the grouping is valid for those as well.
            Map<ServerType, Set<Integer>> serverTypeIndices = IntStream.range(0, queries.size()).
                    mapToObj(i -> new ImmutablePair<>(queries.get(i).getServerType(), i)).
                    collect(groupingBy(Pair::getLeft, mapping(Pair::getRight, toSet())));

            // They we iterate over the technology groups. Each map entry groups all queries, options and collectors
            // for one search type. Thus, we take all the queries options and collectors of the type,
            // issue the queries and collect the results.
            for (Map.Entry<ServerType, Set<Integer>> e : serverTypeIndices.entrySet()) {
                Set<Integer> applicableIndices = e.getValue();
                // Extract the queries, options and collectors for the current type.
                List<ISemedicoQuery> applicableQueries = IntStream.range(0, queries.size()).
                        filter(applicableIndices::contains).
                        mapToObj(queries::get).
                        collect(toList());
                List<EnumSet<SearchOption>> applicableOptions = IntStream.range(0, searchOptionList.size()).
                        filter(applicableIndices::contains).
                        mapToObj(searchOptionList::get)
                        .collect(toList());
                List<List<SearchResultCollector<? super ISemedicoSearchCarrier<?, ?>, ? super SemedicoSearchResult>>> applicableCollectorLists = IntStream.range(0, collectorLists.size()).
                        filter(applicableIndices::contains).
                        mapToObj(collectorLists::get).
                        collect(toList());
                boolean error;
                // Create the search carrier. The carrier depends on the actual search technology each query is
                // targeted at. Also depending on search technology is the actual searchChain, consisting of the
                // ISearchComponents that build up the search process. So here comes the technology-specific part.
                ISemedicoSearchCarrier<?, ?> carrier;
                switch (e.getKey()) {
                    case ELASTIC_SEARCH:
                        SemedicoESSearchCarrier esCarrier = new SemedicoESSearchCarrier(
                                "Multiple ES query search of " + applicableQueries.size() + " queries");
                        for (ISemedicoQuery q : applicableQueries)
                            esCarrier.addQuery((AbstractSemedicoElasticQuery) q);
                        esCarrier.setSearchOptions(applicableOptions);

                        error = elasticChain.process(esCarrier);
                        carrier = esCarrier;
                        break;
                    case TOPIC_MODEL:
                        TopicModelSearchCarrier tmCarrier = new TopicModelSearchCarrier(
                                "Multiple topic model query search of " + applicableQueries.size() + " queries");
                        for (ISemedicoQuery q : applicableQueries)
                            tmCarrier.addQuery((TopicModelQuery) q);

                        error = topicModelChain.process(tmCarrier);
                        carrier = tmCarrier;
                        break;
                    default:
                        throw new IllegalArgumentException("The search server type " + e.getKey() + " is not supported.");
                }
                // We have now issued the search and have the responses of the respective search indexes.
                // Handle the outcome: Errors or the search result.
                if (error)
                    throw new SemedicoRuntimeException(new SearchException(carrier.getFirstError()));
                // The last thing to do in each loop iteration. Let all the result collectors specified for a search
                // create results and collect them in the result collection.
                for (int i = 0; i < applicableCollectorLists.size(); i++) {
                    List<SearchResultCollector<? super ISemedicoSearchCarrier<?, ?>, ? super SemedicoSearchResult>> collectors = applicableCollectorLists.get(i);
                    for (int j = 0; j < collectors.size(); j++) {
                        SearchResultCollector<? super ISemedicoSearchCarrier<?, ?>,? super SemedicoSearchResult> collector = collectors.get(j);
                        SemedicoSearchResult result = collector.collectResult(carrier, i);
                        resultCollection.put(collector.getName(), result);
                    }
                }
                throw new IllegalArgumentException("The search server type " + e.getKey() + " is not supported.");
            }

            return resultCollection;
        };
    }

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
         * Currently, does nothing.
         */
        RETURN_SERVER_QUERY,
        /**
         * Do not return stored fields from a query.
         */
        NO_FIELDS,
        /**
         * Do not create aggregations for a query.
         */
        NO_AGGREGATIONS,
        NO_HITS,
        NO_HIGHLIGHTING
    }

}