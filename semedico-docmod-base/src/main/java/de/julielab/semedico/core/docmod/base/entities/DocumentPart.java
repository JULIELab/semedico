package de.julielab.semedico.core.docmod.base.entities;

import de.julielab.semedico.core.entities.documents.SemedicoIndexField;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Simple class to describe the part of a document type / corpus. To be used in {@link DocModInfo}.
 *
 * @see DocModInfo
 */
public class DocumentPart {
    private String docPartName;
    private String indexName;
    private List<SemedicoIndexField> searchedFields;
    private List<String> requestedStoredFields;

    public DocumentPart(String docPartName, String indexName) {
        this.docPartName = docPartName;
        this.indexName = indexName;
    }


    public DocumentPart(String docPartName, String indexName, List<SemedicoIndexField> searchedFields, List<String> requestedStoredFields) {
        this.docPartName = docPartName;
        this.indexName = indexName;
        this.searchedFields = searchedFields;
        this.requestedStoredFields = requestedStoredFields;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentPart that = (DocumentPart) o;
        return Objects.equals(docPartName, that.docPartName) &&
                Objects.equals(indexName, that.indexName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(docPartName, indexName);
    }

    public List<SemedicoIndexField> getSearchedFields() {
        return searchedFields;
    }

    public void setSearchedFields(List<SemedicoIndexField> searchedFields) {
        this.searchedFields = searchedFields;
    }

    public List<String> getRequestedStoredFields() {
        return requestedStoredFields;
    }

    public void setRequestedStoredFields(List<String> requestedStoredFields) {
        this.requestedStoredFields = requestedStoredFields;
    }

    public String getDocPartName() {
        return docPartName;
    }

    public void setDocPartName(String docPartName) {
        this.docPartName = docPartName;
    }
}
