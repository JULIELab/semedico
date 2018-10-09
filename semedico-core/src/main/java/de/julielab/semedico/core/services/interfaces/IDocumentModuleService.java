package de.julielab.semedico.core.services.interfaces;

import de.julielab.semedico.core.entities.documentmodules.DocumentModuleInfo;

import java.util.List;

public interface IDocumentModuleService {
    List<DocumentModuleInfo> getDocumentModuleInfo();
}
