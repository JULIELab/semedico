package de.julielab.semedico.core.docmod.base.defaultmodule.services;

import de.julielab.semedico.core.docmod.base.services.IDocumentModule;

/**
 * <p>
 * This is the default document module. It serves as a minimal example for document modules, as a proof of concept
 * for them and as a fallback position for new document types no specific document module yet exists.
 * </p>
 */
public class DefaultDocumentModule implements IDocumentModule {
    @Override
    public void contributeDocModInformationService() {

    }

    @Override
    public void contributeQueryTranslatorChain() {

    }

    @Override
    public void contributeDocModQueryService() {

    }
}
