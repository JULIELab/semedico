package de.julielab.semedico.core.entities.documents;

import java.util.List;

/**
 * This class is a flexible model to represent documents of different
 * structures. Most documents will have a title but not all have an abstract or
 * MeSH terms. The same real-world document also may have different
 * representations, e.g. as a hit list item with highlights and reader content
 * for full-text display. These elements that a document can have are made
 * explicit with {@link DocumentElement} instances that are collected by an
 * object of this class.
 * 
 * @author faessler
 *
 */
public class DocumentModel {
	private String documentType;
	private List<DocumentElement> elements;

	public DocumentModel(String documentType, List<DocumentElement> elements) {
		this(documentType);
		this.elements = elements;
	}

	public DocumentModel(String documentType) {
		super();
		this.documentType = documentType;
	}

    public DocumentModel() {
    }

    public List<DocumentElement> getElements() {
		return elements;
	}

	public void setElements(List<DocumentElement> elements) {
		this.elements = elements;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((documentType == null) ? 0 : documentType.hashCode());
		result = prime * result + ((elements == null) ? 0 : elements.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DocumentModel other = (DocumentModel) obj;
		if (documentType == null) {
			if (other.documentType != null)
				return false;
		} else if (!documentType.equals(other.documentType))
			return false;
		if (elements == null) {
			if (other.elements != null)
				return false;
		} else if (!elements.equals(other.elements))
			return false;
		return true;
	}

	/**
	 * The document type is closely related to the index or data facetSource the document
	 * comes from. Each document type has its own model. The identifier is arbitrary
	 * but will be something like "pubmed", "relation", "wikipedia" or similar.
	 * 
	 * @return The document type identifier.
	 */
	public String getDocumentType() {
		return documentType;
	}

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
}
