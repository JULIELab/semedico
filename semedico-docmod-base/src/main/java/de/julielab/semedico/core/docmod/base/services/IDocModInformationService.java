package de.julielab.semedico.core.docmod.base.services;

import de.julielab.semedico.core.docmod.base.entities.QueryTarget;
import de.julielab.semedico.core.entities.docmods.DocModInfo;
import de.julielab.semedico.core.entities.docmods.DocumentPart;

import java.util.Map;

public interface IDocModInformationService {
    /**
     *
     * @return All registered document information objects.
     */
    Map<String, DocModInfo> getDocModInfos();

    /**
     *
     * @param target A query target (document type and document part).
     * @return The document module info object with the name given by {@link QueryTarget#documentType}.
     */
    DocModInfo getDocModInfo(QueryTarget target);

    /**
     *Returns the document part corresponding to <tt>target</tt>'s {@link QueryTarget#documentType} and {@link DocumentPart#docPartName}, if existing.
     * If either name - document or document part name - are not registered, <tt>null</tt> is returned.
     *
     * @param documentType The name of the document type to get a document part from.
     * @param documentPart The name of the desired document part.
     * @return The document part corresponding to <tt>target</tt>'s {@link QueryTarget#documentType} and {@link DocumentPart#docPartName} or <tt>null</tt> if the part could not be found.
     */
    DocumentPart getDocumentPart(String documentType, String documentPart);
}
