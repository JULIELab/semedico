package de.julielab.semedico.core.search.query;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;

import java.util.Map;

/**
 * <p>
 * This interface is only there to represent queries that may return aggregations. It is not meant to serve as an
 * implementation point for concrete query classes.
 * </p>
 * <p>
 * Query classes implementing this interface may take instances of {@link AggregationRequest}. An aggregation is
 * hereby oriented on the notation of ElasticSearch that uses aggregations to extract compact data from full text
 * documents. The most commonly known example of this are facets: Instead of retrieving all words of a field
 * one by one, a list of unique words with their frequency is returned.
 * </p>
 * <p>
 * Other queries than {@link IElasticQuery} may use this interface. They must be able to return instances of classes
 * implementing {@link de.julielab.elastic.query.components.data.aggregation.IAggregationResult} via
 * {@link de.julielab.elastic.query.services.ISearchServerResponse#getAggregationResult(AggregationRequest)}.
 * </p>
 */
interface IAggregationQuery {
    Map<String, AggregationRequest> getAggregationRequests();
}
