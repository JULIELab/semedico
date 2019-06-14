package de.julielab.semedico.core.docmod.base.entities;

import de.julielab.semedico.core.entities.docmods.DocumentPart;

public class QueryTarget {
    private final String documentType;
    private final DocumentPart documentPart;

    public QueryTarget(String documentType, DocumentPart documentPart) {

        this.documentType = documentType;
        this.documentPart = documentPart;
    }

    public String getDocumentType() {
        return documentType;
    }

    @Override
    public String toString() {
        return "QueryTarget{" +
                "documentType='" + documentType + '\'' +
                ", documentPart=" + documentPart +
                '}';
    }

    public DocumentPart getDocumentPart() {
        return documentPart;
    }
}
