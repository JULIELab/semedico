package de.julielab.semedico.core.docmod.base.services;

import de.julielab.semedico.core.docmod.base.entities.DocModInfo;
import de.julielab.semedico.core.docmod.base.entities.DocumentPart;
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
    public DocumentPart getDocumentPart(QueryTarget target) {
        DocModInfo info =  infos.get(target);
        if (info != null) {
            return info.getDocumentPart(target);
        }
        return null;
    }


}
