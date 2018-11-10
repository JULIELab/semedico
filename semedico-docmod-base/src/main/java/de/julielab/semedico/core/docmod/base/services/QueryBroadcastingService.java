package de.julielab.semedico.core.docmod.base.services;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.docmod.base.broadcasting.IAggregationBroadcast;
import de.julielab.semedico.core.docmod.base.broadcasting.IResultCollectorBroadcast;
import de.julielab.semedico.core.docmod.base.broadcasting.QueryBroadcastResult;
import de.julielab.semedico.core.docmod.base.entities.DocumentPart;
import de.julielab.semedico.core.docmod.base.entities.QueryTarget;
import de.julielab.semedico.core.search.components.data.ISemedicoSearchCarrier;
import de.julielab.semedico.core.search.query.IAggregationQuery;
import de.julielab.semedico.core.search.query.IFieldQuery;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;

import java.util.List;

public class QueryBroadcastingService implements IQueryBroadcastingService {

    private IDocModQueryService docModQueryService;
    private IDocModInformationService docModInformationService;

    public QueryBroadcastingService(IDocModQueryService docModQueryService, IDocModInformationService docModInformationService) {
        this.docModQueryService = docModQueryService;
        this.docModInformationService = docModInformationService;
    }

    @Override
    public QueryBroadcastResult broadcastQuery(ISemedicoQuery query, List<QueryTarget> queryTargets, List<IAggregationBroadcast> aggregationBroadcasts, List<IResultCollectorBroadcast> resultCollectorBroadcasts) {
        QueryBroadcastResult result = new QueryBroadcastResult();
        for (QueryTarget target : queryTargets) {
            try {
                final ISemedicoQuery queryClone = query.clone();
                // We restrict ourselves to ElasticSearch queries here because these are the only ones supporting aggregations right now.
                if (query instanceof IAggregationQuery) {
                    IAggregationQuery aggregationQuery = (IAggregationQuery) queryClone;
                    for (IAggregationBroadcast aggregationBroadcast : aggregationBroadcasts) {
                        final AggregationRequest aggregationRequest = docModQueryService.getAggregationRequest(target, aggregationBroadcast);
                        aggregationQuery.putAggregationRequest(aggregationRequest);
                    }
                }
                if (query instanceof IFieldQuery) {
                    IFieldQuery fieldQuery = (IFieldQuery) queryClone;
                    final DocumentPart documentPart = docModInformationService.getDocumentPart(target);
                    fieldQuery.setSearchedFields(documentPart.getSearchedFields());
                    fieldQuery.setRequestedFields(documentPart.getRequestedStoredFields());
                }
                for (IResultCollectorBroadcast resultCollectorBroadcast : resultCollectorBroadcasts) {
                    final SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoSearchResult> resultCollector = docModQueryService.getResultCollector(target, resultCollectorBroadcast);
                    result.addSearchResultCollector(queryClone, resultCollector);
                }
                result.addQuery(queryClone);
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException(e);
            }
        }
        return result;
    }
}
