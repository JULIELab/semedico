package de.julielab.semedico.core.search.query;

import de.julielab.semedico.core.search.services.SearchService.SearchOption;

import java.util.Collection;
import java.util.Set;

/**
 * A query aimed at an ElasticSearch index. Multiple methods are provided that express features that are specific
 * to ElasticSearch, most notably {@link #getAggregationRequests()}. Aggregations are a major feature by ElasticSearch.
 * Other search technologies have similar capabilities but express them differently.
 */
public interface IElasticQuery extends IFieldQuery, IAggregationQuery {
    /**
     * The index to perform the query on.
     *
     * @return The index to search.
     */
    String getIndex();

    void setIndex(String index);
}
