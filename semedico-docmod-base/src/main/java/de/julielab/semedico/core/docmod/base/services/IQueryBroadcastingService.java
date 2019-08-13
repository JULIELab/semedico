package de.julielab.semedico.core.docmod.base.services;

import de.julielab.semedico.core.docmod.base.broadcasting.IAggregationBroadcast;
import de.julielab.semedico.core.docmod.base.broadcasting.IResultCollectorBroadcast;
import de.julielab.semedico.core.docmod.base.broadcasting.QueryBroadcastResult;
import de.julielab.semedico.core.docmod.base.entities.QueryTarget;
import de.julielab.semedico.core.search.query.ISemedicoQuery;

import java.util.List;

/**
 * <p>
 * When a query is issued to Semedico, there is the actual query string and a list of document types and
 * document type parts to search in. Thus, the query has to be distributed to all document modules.
 * We call this process "query broadcasting" and this is the service that helps with this task.
 * </p>
 * <p>
 * The service clones and reconfigures the given template query according to the requirements of the given
 * query targets (e.g. document indices). The queries are configured using the {@link IDocModQueryService}
 * managing the result collectors, aggregation requests and the requested highlighting.
 * </p>
 */
public interface IQueryBroadcastingService {
    /**
     * Broadcast <tt>templateQuery</tt> across <tt>queryTargets</tt> with respect to the given
     * aggregation broadcasts and result collector broadcasts.
     *
     * @param templateQuery
     * @param queryTargets
     * @param aggregationBroadcasts
     * @param resultCollectorBroadcasts
     * @return
     */
    QueryBroadcastResult broadcastQuery(ISemedicoQuery<?> templateQuery,
                                        List<QueryTarget> queryTargets,
                                        List<IAggregationBroadcast> aggregationBroadcasts,
                                        List<IResultCollectorBroadcast> resultCollectorBroadcasts);
}
