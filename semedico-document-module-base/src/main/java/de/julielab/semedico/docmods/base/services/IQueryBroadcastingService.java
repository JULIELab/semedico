package de.julielab.semedico.docmods.base.services;

import de.julielab.semedico.core.search.broadcasting.IAggregationBroadcast;
import de.julielab.semedico.core.search.broadcasting.IResultCollectorBroadcast;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.services.SearchService;
import de.julielab.semedico.docmods.base.entities.QueryTarget;

import java.util.EnumSet;
import java.util.List;

/**
 * <p>
 *     When a query is issued to Semedico, there is the actual query string and a list of document types and
 *     document type parts to search in. Thus, the query has to be distributed to all document modules.
 *     We call this process "query broadcasting" and this is the service that helps with this task.
 * </p>
 */
public interface IQueryBroadcastingService {
    List<ISemedicoQuery> broadcastQuery(ISemedicoQuery query,
                                        EnumSet<SearchService.SearchOption> searchOptions,
                                        List<QueryTarget> queryTargets,
                                        List<IAggregationBroadcast> aggregationBroadcasts,
                                        List<IResultCollectorBroadcast> resultCollectorBroadcasts);
}
