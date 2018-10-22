package de.julielab.semedico.core.docmod.base.services;

public interface IDocumentModule {
    void contributeDocModInformationService();

    void contributeQueryTranslatorChain();

    void contributeDocModQueryService();

}
