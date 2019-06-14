package de.julielab.semedico.core.docmod.base.services;

import de.julielab.semedico.core.entities.docmods.DocModInfo;
import de.julielab.semedico.core.entities.docmods.DocumentPart;
import de.julielab.semedico.core.docmod.base.entities.QueryTarget;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A very simple service that serves as a collection point for the Document Module Information objects
 * of all document modules. Each document module must contribute its document information to this service.
 */
public class DocModInformationService implements IDocModInformationService {
    private Map<String, DocModInfo> infos;

    public DocModInformationService(List<DocModInfo> infos) {
        this.infos = infos.stream().collect(Collectors.toMap(DocModInfo::getDocumentTypeName, Function.identity()));
    }

    @Override
    public Map<String, DocModInfo> getDocModInfos() {
        return infos;
    }

    @Override
    public DocModInfo getDocModInfo(QueryTarget target) {
        return infos.get(target.getDocumentType());
    }

    @Override
    public DocumentPart getDocumentPart(String documentType, String documentPartName) {
        DocModInfo info = infos.get(documentType);
        if (info == null)
            throw new IllegalArgumentException("No document module information for the document type " + documentType + " was found");
        final DocumentPart documentPart = info.getDocumentPart(documentPartName);
        if (documentPart == null)
            throw new IllegalArgumentException("The document module information for document type " + documentType + " does not contain a document part with the name " + documentPartName);
        return documentPart;
    }


}
