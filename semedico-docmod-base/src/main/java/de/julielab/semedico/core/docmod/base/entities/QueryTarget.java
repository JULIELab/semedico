package de.julielab.semedico.core.docmod.base.entities;

import de.julielab.semedico.core.entities.docmods.DocumentPart;
import de.julielab.semedico.core.search.query.SearchStrategy;

public class QueryTarget {
    private final String documentType;
    private final DocumentPart documentPart;
    private final SearchStrategy searchStrategy;

    public QueryTarget(String documentType, DocumentPart documentPart, SearchStrategy searchStrategy) {

        this.documentType = documentType;
        this.documentPart = documentPart;
        this.searchStrategy = searchStrategy;
    }

    public SearchStrategy getSearchStrategy() {
        return searchStrategy;
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
