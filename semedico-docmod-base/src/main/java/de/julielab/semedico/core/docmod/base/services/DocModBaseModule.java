package de.julielab.semedico.core.docmod.base.services;

import de.julielab.semedico.core.entities.docmods.DocModInfo;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.services.ChainBuilder;

import java.util.List;

/**
 * <p>The Semedico search framework tries to stay agnostic regarding the actual documents being searched. It just
 * provides the services and business objects for hierarchical concepts stored in a graph database and searching
 * one ore multiple document indices.</p>
 * <p>
 * To offer concrete document types and potentially smaller parts of each types for search, document modules
 * are created. They contribute the this module all
 * necessary services and information to fully integrate the document modules into the final search engine. Much
 * effort is done to make the activation and deactivation of documents modules as simple as possible. In this way,
 * the search engine should easily adaptable to different kinds of document sources.
 * Documentation in the form of diagrams is available in the <tt>developer-resources</tt> directory
 * in the root of the semedico repository.
 * </p>
 */
public class DocModBaseModule {

    private static ChainBuilder chainBuilder;

    public DocModBaseModule(ChainBuilder chainBuilder) {
        this.chainBuilder = chainBuilder;
    }

    public static void bind(ServiceBinder binder) {
        binder.bind(IQueryBroadcastingService.class, QueryBroadcastingService.class);
    }

    /**
     * Gives access to the document module informatin of all modules.
     *
     * @param docModInfos Objects exposing the available document modules and the searchable document parts.
     * @return The document information objects of all document modules that have been added to the search engine instance.
     */
    public static IDocModInformationService buildDocModInformationService(List<DocModInfo> docModInfos) {
        return new DocModInformationService(docModInfos);
    }

    /**
     * Used for query broadcasting. Returns aggregation requests and result collectors appropriate to a given {@link de.julielab.semedico.core.docmod.base.entities.QueryTarget}
     * for the queried type of aggregation or result collector.
     *
     * @param queryServices The document modules' query services as service contributions.
     * @return A chain-of-command of all the contributed document module query services.
     */
    public static IDocModQueryService buildDocModQueryService(List<IDocModQueryService> queryServices) {
        return chainBuilder.build(IDocModQueryService.class, queryServices);
    }
}
