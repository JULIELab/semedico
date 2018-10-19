package de.julielab.semedico.docmods.base.entities;

public class QueryTarget {
    private final String documentType;
    private String documentPart;

    public QueryTarget(String documentType, String documentPart) {

        this.documentType = documentType;
        this.documentPart = documentPart;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getDocumentPart() {
        return documentPart;
    }

    public void setDocumentPart(String documentPart) {
        this.documentPart = documentPart;
    }
}
