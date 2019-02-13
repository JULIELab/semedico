package de.julielab.semedico.core.search.searchresponse;

/**
 * This class only exists to add the {@link IAggregationSearchResponse} to the {@link de.julielab.elastic.query.components.data.ElasticServerResponse}.
 */
public class ElasticServerResponse extends de.julielab.elastic.query.components.data.ElasticServerResponse implements IAggregationSearchResponse {
    public ElasticServerResponse(de.julielab.elastic.query.components.data.ElasticServerResponse rootResponse) {
        super(rootResponse.getResponse(), rootResponse.getClient());
        setQueryError(rootResponse.getQueryError());
        setQueryErrorMessage(rootResponse.getQueryErrorMessage());
        setSuggestionSearchResponse(rootResponse.isSuggestionSearchResponse());
    }
}
