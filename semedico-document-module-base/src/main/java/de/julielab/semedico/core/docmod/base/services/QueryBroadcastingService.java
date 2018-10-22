package de.julielab.semedico.core.docmod.base.services;

import de.julielab.semedico.core.docmod.base.entities.QueryTarget;
import de.julielab.semedico.core.search.broadcasting.IAggregationBroadcast;
import de.julielab.semedico.core.search.broadcasting.IResultCollectorBroadcast;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.services.SearchService;

import java.util.EnumSet;
import java.util.List;

public class QueryBroadcastingService implements IQueryBroadcastingService {
    @Override
    public List<ISemedicoQuery> broadcastQuery(ISemedicoQuery query, EnumSet<SearchService.SearchOption> searchOptions, List<QueryTarget> queryTargets, List<IAggregationBroadcast> aggregationBroadcasts, List<IResultCollectorBroadcast> resultCollectorBroadcasts) {
        return null;
    }
}
