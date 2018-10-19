package de.julielab.semedico.docmods.base.services;

import de.julielab.semedico.core.search.broadcasting.IAggregationBroadcast;
import de.julielab.semedico.core.search.broadcasting.IResultCollectorBroadcast;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.services.SearchService;
import de.julielab.semedico.docmods.base.entities.QueryTarget;

import java.util.EnumSet;
import java.util.List;

public interface IBroadcastingService {
    List<ISemedicoQuery> broadcastQuery(ISemedicoQuery query,
                                        EnumSet<SearchService.SearchOption> searchOptions,
                                        List<QueryTarget> queryTargets,
                                        List<IAggregationBroadcast> aggregationBroadcasts,
                                        List<IResultCollectorBroadcast> resultCollectorBroadcasts);
}
