package de.julielab.semedico.core.docmod.base.entities;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;

import java.util.List;

public abstract class DocumentModuleInfo {

    public enum AggregationType {CONCEPT_FACET}

    private String documentType;

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public List<String> getDocumentParts() {
        return documentParts;
    }

    public void setDocumentParts(List<String> documentParts) {
        this.documentParts = documentParts;
    }

    private List<String> documentParts;

    public abstract AggregationRequest getAggregationRequest(AggregationType aggregationType);
}
