package de.julielab.semedico.core.docmod.base.defaultmodule.services;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.docmod.base.broadcasting.*;
import de.julielab.semedico.core.docmod.base.defaultmodule.entities.DefaultSerpItemCollector;
import de.julielab.semedico.core.entities.docmods.DocModInfo;
import de.julielab.semedico.core.docmod.base.entities.QueryTarget;
import de.julielab.semedico.core.docmod.base.services.IDocModQueryService;
import de.julielab.semedico.core.docmod.base.services.IHighlightingService;
import de.julielab.semedico.core.search.components.data.ISemedicoSearchCarrier;
import de.julielab.semedico.core.search.query.AggregationRequests;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;
import de.julielab.semedico.core.search.services.ResultCollectors;
import org.slf4j.Logger;

/**
 * A basic implementation of a DocumentModule Query Service. It supports the fields defined in
 * {@link DefaultDocumentModule} including simple terms faceting.
 */
public class DefaultDocModQueryService implements IDocModQueryService {

    public static final String BROADCAST_SUFFIX = "_defaultdocmod";
    private Logger log;
    private DocModInfo defaultDocModInfo;
    private IHighlightingService highlightingService;

    public DefaultDocModQueryService(Logger log, DocModInfo defaultDocModInfo, IHighlightingService highlightingService) {
        this.log = log;
        this.defaultDocModInfo = defaultDocModInfo;

        this.highlightingService = highlightingService;
    }

    @Override
    public AggregationRequest getAggregationRequest(QueryTarget queryTarget, IAggregationBroadcast aggregationBroadcast) {
        if (!matchesQueryTarget(queryTarget))
            return null;
        AggregationRequest request = null;

        if (aggregationBroadcast.getClass().equals(FieldTermAggregationBroadcast.class)) {
            FieldTermAggregationBroadcast ftbc = (FieldTermAggregationBroadcast) aggregationBroadcast;
            request = AggregationRequests.getFieldTermsRequest(ftbc.getAggregationBaseName() + BROADCAST_SUFFIX, DefaultDocumentModule.FIELD_FACETS, ftbc.getTermNumber(), ftbc.getOrderType(), ftbc.getSortOrder());
        } else {
            log.debug("{} did not handle aggregation broadcast {} although the query target {} matches", getClass().getCanonicalName(), aggregationBroadcast, queryTarget);
        }
        return request;
    }

    @Override
    public SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoSearchResult> getResultCollector(QueryTarget queryTarget, IResultCollectorBroadcast resultCollectorBroadcast) {
        if (!matchesQueryTarget(queryTarget))
            return null;
        SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoSearchResult> collector = null;
        if (resultCollectorBroadcast.getClass().equals(SerpItemCollectorBroadcast.class)) {
            collector = new DefaultSerpItemCollector(defaultDocModInfo, highlightingService);
        } else if (resultCollectorBroadcast.getClass().equals(FieldTermCollectorBroadcast.class)) {
            FieldTermCollectorBroadcast ftcb = (FieldTermCollectorBroadcast) resultCollectorBroadcast;
            collector = ResultCollectors.getFieldTermsCollector(ftcb.getResultBaseName() + BROADCAST_SUFFIX, ftcb.getAggregationRequestBaseNames().stream().map(name -> name + BROADCAST_SUFFIX).toArray(String[]::new));
        }
        return collector;
    }


    private boolean matchesQueryTarget(QueryTarget target) {
        return target.getDocumentType().equals(defaultDocModInfo.getDocumentTypeName()) && defaultDocModInfo.getDocumentParts().values().contains(target.getDocumentPart());
    }
}
