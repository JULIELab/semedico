package de.julielab.semedico.core.docmod.base.services;

import de.julielab.elastic.query.components.data.HighlightCommand;
import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.docmod.base.broadcasting.IAggregationBroadcast;
import de.julielab.semedico.core.docmod.base.broadcasting.IResultCollectorBroadcast;
import de.julielab.semedico.core.docmod.base.entities.QueryTarget;
import de.julielab.semedico.core.search.components.data.ISemedicoSearchCarrier;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;

import java.util.List;

/**
 * <p>This service interface must be implemented by each document module for each document part that is exposed as
 * being able to be queried by the module. This exposition is done through the {@link IDocModInformationService} where
 * each document module contributes its offered document type and documents parts for search.</p>
 * <p>The purpose of this service is to be used in query broadcasting across the available document modules. The
 * broadcasting specifies which aggregation or which result collector should be added to the query (e.g. facet counts
 * and document retrieval). Since each document module might take a different approach on different fields for these
 * information, the modules create their own appropriate aggregation requests or result collectors. Those can then
 * be added to the final query/queries created in the broadcasting which are then sent to the search server (probably ElasticSearch).</p>
 * <p>Services implementing this interface form a <em>chain-of-command</em> as built in {@link DocModBaseModule#buildDocModQueryService(List)}.
 * The exposed methods take a {@link QueryTarget} objects which uniquely identifies the index to search. The services
 * in the chain-of-command need first to check whether they are applicable to the given query target. If they
 * aren't, they are requried to return <tt>null</tt>. Otherwise they return the requested object. As soon as an
 * object is returned, the chain-of-command is terminated and its final result is the returned object.</p>
 *
 * @see IQueryBroadcastingService
 */
public interface IDocModQueryService {
    AggregationRequest getAggregationRequest(QueryTarget queryTarget, IAggregationBroadcast aggregationBroadcast);

    SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoSearchResult> getResultCollector(QueryTarget queryTarget, IResultCollectorBroadcast resultCollectorBroadcast);

    HighlightCommand getHighlightCommand(QueryTarget target, ISemedicoQuery.ResultType resultType);
}
