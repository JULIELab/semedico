package de.julielab.semedico.core.docmod.base.services;

import de.julielab.semedico.core.docmod.base.entities.DocModInfo;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.query.translation.IQueryTranslator;
import org.apache.tapestry5.ioc.OrderedConfiguration;

public interface IDocumentModule {
    void contributeDocModInformationService(OrderedConfiguration<DocModInfo> configuration);

    void contributeQueryTranslatorChain(OrderedConfiguration<IQueryTranslator<? extends ISemedicoQuery>> configuration);

    void contributeDocModQueryService(OrderedConfiguration<IDocModQueryService> configuration);

}
