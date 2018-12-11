package de.julielab.semedico.core.docmod.base.services;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.java.utilities.prerequisites.PrerequisiteChecker;
import de.julielab.semedico.core.docmod.base.broadcasting.IAggregationBroadcast;
import de.julielab.semedico.core.docmod.base.broadcasting.IResultCollectorBroadcast;
import de.julielab.semedico.core.docmod.base.broadcasting.QueryBroadcastResult;
import de.julielab.semedico.core.entities.docmods.DocumentPart;
import de.julielab.semedico.core.docmod.base.entities.QueryTarget;
import de.julielab.semedico.core.search.components.data.ISemedicoSearchCarrier;
import de.julielab.semedico.core.search.query.*;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;

import javax.annotation.Nonnull;
import java.util.List;

public class QueryBroadcastingService implements IQueryBroadcastingService {

    private IDocModQueryService docModQueryService;

    public QueryBroadcastingService(IDocModQueryService docModQueryService) {
        this.docModQueryService = docModQueryService;
    }

    @Override
    public QueryBroadcastResult broadcastQuery(ISemedicoQuery query, List<QueryTarget> queryTargets, List<IAggregationBroadcast> aggregationBroadcasts, List<IResultCollectorBroadcast> resultCollectorBroadcasts) {
        QueryBroadcastResult result = new QueryBroadcastResult();
        for (QueryTarget target : queryTargets) {
            try {
                final ISemedicoQuery queryClone = query.clone();
                // We restrict ourselves to ElasticSearch queries here because these are the only ones supporting aggregations right now.
                if (query instanceof IAggregationQuery && aggregationBroadcasts != null) {
                    IAggregationQuery aggregationQuery = (IAggregationQuery) queryClone;
                    for (IAggregationBroadcast aggregationBroadcast : aggregationBroadcasts) {
                        final AggregationRequest aggregationRequest = docModQueryService.getAggregationRequest(target, aggregationBroadcast);
                        aggregationQuery.putAggregationRequest(aggregationRequest);
                    }
                }
                if (query instanceof IFieldQuery) {
                    IElasticQuery elasticQuery = (IElasticQuery) queryClone;
                    final DocumentPart documentPart = target.getDocumentPart();
                    elasticQuery.setIndex(documentPart.getIndexName());
                    elasticQuery.setSearchedFields(documentPart.getSearchedFields());
                    elasticQuery.setRequestedFields(documentPart.getRequestedStoredFields());
                }
                if (resultCollectorBroadcasts != null) {
                    for (IResultCollectorBroadcast resultCollectorBroadcast : resultCollectorBroadcasts) {
                        final SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoSearchResult> resultCollector = docModQueryService.getResultCollector(target, resultCollectorBroadcast);
                        PrerequisiteChecker.checkThat().notNull(resultCollector).withNames("Result Collector for query target " + target + " and result collector broadcast " + resultCollectorBroadcast);
                        result.addSearchResultCollector(queryClone, resultCollector);
                    }
                }
                if (queryClone instanceof AbstractSemedicoElasticQuery) {
                    AbstractSemedicoElasticQuery esQuery = (AbstractSemedicoElasticQuery) queryClone;
                    esQuery.setHlCmd(docModQueryService.getHighlightCommand(target, esQuery.getResultType()));
                }
                result.addQuery(queryClone);
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException(e);
            }
        }
        return result;
    }
}
