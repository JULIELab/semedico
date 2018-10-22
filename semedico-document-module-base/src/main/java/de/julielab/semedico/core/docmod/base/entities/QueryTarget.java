package de.julielab.semedico.core.docmod.base.entities;

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

    public DocumentPart getDocumentPart() {
        return documentPart;
    }
}
