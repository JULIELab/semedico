package de.julielab.semedico.core.search.components.data;

import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.semedico.core.search.query.TopicModelQuery;
import de.julielab.semedico.core.search.searchresponse.TopicModelSearchResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * The topic model search is extremely simple. It does only accept some plain text query words, here expressed by
 * instances of {@link de.julielab.semedico.core.concepts.TopicTag} and collected in a {@link TopicModelQuery}.
 * The search response is a {@link TopicModelSearchResponse} that exposes the document ID result list as its
 * always existing aggregation result.
 */
public class TopicModelSearchCarrier extends SearchCarrier<TopicModelSearchResponse>
        implements ISemedicoSearchCarrier<TopicModelQuery, TopicModelSearchResponse> {

    private List<TopicModelQuery> queries;

    public TopicModelSearchCarrier(String chainName) {
        super(chainName);
    }

    @Override
    public void addQuery(TopicModelQuery query) {
        if (queries == null)
            queries = new ArrayList<>();
        queries.add(query);
    }

    @Override
    public List<TopicModelQuery> getQueries() {
        return queries;
    }

    @Override
    public void setQueries(List<TopicModelQuery> queries) {
        this.queries = queries;
    }

    @Override
    public TopicModelQuery getQuery(int index) {
        return queries.get(index);
    }
}
