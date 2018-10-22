package de.julielab.semedico.core.docmod.base.services;

import de.julielab.semedico.core.docmod.base.entities.DocModInfo;
import org.apache.tapestry5.ioc.services.ChainBuilder;

import java.util.List;

/**
 * This module offers the services required to add new document modules to the search engine.
 */
public class DocModBaseModule {

    private ChainBuilder chainBuilder;

    public DocModBaseModule(ChainBuilder chainBuilder) {
        this.chainBuilder = chainBuilder;
    }

    /**
     * Gives access to the document module informatin of all modules.
     *
     * @param docModInfos Objects exposing the available document modules and the searchable document parts.
     * @return The document information objects of all document modules that have been added to the search engine instance.
     */
    IDocModInformationService buildDocModInformationService(List<DocModInfo> docModInfos) {
        return new DocModInformationService(docModInfos);
    }

    /**
     * Used for query broadcasting. Returns aggregation requests and result collectors appropriate to a given {@link de.julielab.semedico.core.docmod.base.entities.QueryTarget}
     * for the queried type of aggregation or result collector.
     *
     * @param queryServices The document modules' query services as service contributions.
     * @return A chain-of-command of all the contributed document module query services.
     */
    IDocModQueryService buildDocModQueryService(List<IDocModQueryService> queryServices) {
        return chainBuilder.build(IDocModQueryService.class, queryServices);
    }
}
