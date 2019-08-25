package de.julielab.semedico.core.search.query;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.search.ServerType;

public class ElasticSearchQuery extends AbstractSemedicoElasticQuery<SearchServerQuery> {

    private SearchServerQuery query;

    public ElasticSearchQuery(String index, SearchStrategy searchStrategy) {
        super(index, searchStrategy);
    }

    public ElasticSearchQuery(String index, SearchStrategy searchStrategy, SearchServerQuery query) {
        this(index, searchStrategy);
        this.query = query;
    }

    @Override
    public SearchServerQuery getQuery() {
        return query;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.ELASTIC_SEARCH;
    }
}
