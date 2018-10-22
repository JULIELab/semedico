package de.julielab.semedico.core.docmod.base.broadcasting;

import de.julielab.semedico.core.docmod.base.services.IQueryBroadcastingService;

/**
 * <p>To distribute a single query across a list of document parts we use broadcasting (see references for details).
 * Thus, the query objects itself has to be multiplied but also the aggregation requests and the result collectors.
 * This interface serves to broadcast aggregation requests in {@link IQueryBroadcastingService}.</p>
 * @see IQueryBroadcastingService
 */
public interface IAggregationBroadcast {
    String getAggregationBaseName();
}
