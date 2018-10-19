package de.julielab.semedico.core.services.interfaces;

import de.julielab.semedico.docmods.base.services.documentmodules.DocumentModuleInfo;

import java.util.List;

public interface IDocumentModuleService {
    List<DocumentModuleInfo> getDocumentModuleInfo();
}
