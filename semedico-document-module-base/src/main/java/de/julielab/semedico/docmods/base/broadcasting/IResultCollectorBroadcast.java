package de.julielab.semedico.docmods.base.broadcasting;

/**
 * <p>To distribute a single query across a list of document parts we use broadcasting (see references for details).
 * Thus, the query objects itself has to be multiplied but also the aggregation requests and the result collectors.
 * This interface serves to broadcast result collectors in {@link de.julielab.semedico.docmods.base.services.IQueryBroadcastingService}.</p>
 * @see de.julielab.semedico.docmods.base.services.IQueryBroadcastingService
 */
public interface IResultCollectorBroadcast {
    String getResultBaseName();
}
