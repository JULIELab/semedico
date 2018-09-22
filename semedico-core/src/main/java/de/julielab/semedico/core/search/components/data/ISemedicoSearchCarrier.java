package de.julielab.semedico.core.search.components.data;

import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.search.query.ISemedicoQuery;

import java.util.List;

public interface ISemedicoSearchCarrier<Q extends ISemedicoQuery, R extends ISearchServerResponse> {
    void setQueries(List<Q> queries);

    void addQuery(Q query);

    List<Q> getQueries();

    Q getQuery(int index);

    void setSearchResponses(List<R> responses);

    void addSearchResponse(R response);

    List<R> getSearchResponses();

    R getSearchResponse(int index);

    String getFirstError();
}
