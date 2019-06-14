package de.julielab.semedico.core.search.results;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;

interface IAggregationResult {
    IAggregationResult getAggregationResult(AggregationRequest aggRequest);
}
