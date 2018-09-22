package de.julielab.semedico.core.search.searchresponse;

import de.julielab.elastic.query.services.ISearchServerResponse;

public interface IElasticServerResponse extends de.julielab.elastic.query.services.IElasticServerResponse, ISearchServerResponse, IAggregationSearchResponse {
}
