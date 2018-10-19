package de.julielab.semedico.docmods.base.services;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.docmods.base.entities.AggregationBroadcast;
import de.julielab.semedico.docmods.base.entities.QueryTarget;

public interface IDocModQueryService {
    AggregationRequest getAggregationRequest(QueryTarget queryTarget, AggregationBroadcast aggregationBroadcast);

    AggregationRequest getResultCollector(QueryTarget queryTarget, AggregationBroadcast aggregationBroadcast);
}
