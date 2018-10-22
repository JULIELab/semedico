package de.julielab.semedico.core.docmod.base.services;

import de.julielab.semedico.core.docmod.base.entities.DocModInfo;

import java.util.List;

/**
 * A very simple service that serves as a collection point for the Document Module Information objects
 * of all document modules. Each document module must contribute its document information to this service.
 */
public class DocModInformationService implements IDocModInformationService {
    private List<DocModInfo> infos;

    public DocModInformationService(List<DocModInfo> infos) {
        this.infos = infos;
    }

    @Override
    public List<DocModInfo> getDocModInfo() {
        return infos;
    }
}
