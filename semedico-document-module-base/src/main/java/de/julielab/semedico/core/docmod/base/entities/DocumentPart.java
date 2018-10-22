package de.julielab.semedico.core.docmod.base.entities;

import java.util.Objects;

/**
 * Simple class to describe the part of a document type / corpus. To be used in {@link DocModInfo}.
 *
 * @see DocModInfo
 */
public class DocumentPart {
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

    private String docPartName;
    private String indexName;

    public DocumentPart(String docPartName, String indexName) {
        this.docPartName = docPartName;
        this.indexName = indexName;
    }

    public String getDocPartName() {
        return docPartName;
    }

    public void setDocPartName(String docPartName) {
        this.docPartName = docPartName;
    }
}
