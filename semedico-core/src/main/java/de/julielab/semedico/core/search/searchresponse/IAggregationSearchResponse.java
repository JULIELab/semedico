package de.julielab.semedico.core.search.searchresponse;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.elastic.query.components.data.aggregation.IAggregationResult;
import de.julielab.elastic.query.services.ISearchServerResponse;

/**
 * <p>An interface that is not used directly to implement search response classes. It is only meant to provide the
 * 'aggregation' facet to actual implementations or to interfaces extending {@link ISearchServerResponse}.</p>
 */
public interface IAggregationSearchResponse {
    IAggregationResult getAggregationResult(AggregationRequest aggCmd);
}
