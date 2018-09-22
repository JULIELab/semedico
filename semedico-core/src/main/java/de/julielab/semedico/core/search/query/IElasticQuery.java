package de.julielab.semedico.core.search.query;

import de.julielab.semedico.core.search.services.SearchService.SearchOption;

import java.util.Collection;
import java.util.Set;

/**
 * A query aimed at an ElasticSearch index. Multiple methods are provided that express features that are specific
 * to ElasticSearch, most notably {@link #getAggregationRequests()}. Aggregations are a major feature by ElasticSearch.
 * Other search technologies have similar capabilities but express them differently.
 */
public interface IElasticQuery extends IFieldQuery, IScopedQuery, IAggregationQuery {
    /**
     * The index to perform the query on.
     *
     * @return The index to search.
     */
    String getIndex();

    void setIndex(String index);

    /**
     * The index types (e.g. medline in contrast to fulltext) to perform the
     * search searchScopes on.
     *
     * @return The index types to search on.
     * @deprecated Index types will be removed from ElasticSearch and we don't use them any more anyway.
     */
    @Deprecated
    Collection<String> getIndexTypes();

    /**
     * @param indexTypes
     * @deprecated Index types will be removed from ElasticSearch and we don't use them any more anyway.
     */
    @Deprecated
    void setIndexTypes(Collection<String> indexTypes);

    Set<SearchOption> getSearchOptions();

    void setSearchOptions(Set<SearchOption> searchOptions);
}
