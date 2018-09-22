package de.julielab.semedico.core.entities.documents;

/**
 * Document parts are title, abstract, the authors etc. This is output element
 * oriented and is the foundation of the dynamic document definition system:
 * Each document consists of multiple elements, each of which is of a specific
 * type. With this information, the frontend should be able to display the
 * document.
 * 
 * @author faessler
 *
 */
public class DocumentElement {
	private String fieldName;
	private DocumentElementType elementType;
	private boolean multiValued;
	private boolean excerpt;

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public DocumentElement() {
	}

	public boolean isExcerpt() {
		return excerpt;
	}

	public void setExcerpt(boolean excerpt) {
		this.excerpt = excerpt;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setElementType(DocumentElementType elementType) {
		this.elementType = elementType;
	}

	/**
	 * The type of this document element. This translates to layout choices in the
	 * frontend.
	 * 
	 * @return The type of part.

	 * @see DocumentElementType
	 */
	public DocumentElementType getElementType() {
		return elementType;
	}

	public boolean isMultiValued() {
		return multiValued;
	}

	public void setMultiValued(boolean multiValued) {
		this.multiValued = multiValued;
	}

	public DocumentElement(String fieldName, DocumentElementType elementType) {
		super();
		this.fieldName = fieldName;
		this.elementType = elementType;
	}

	public DocumentElement(String fieldName, DocumentElementType elementType, boolean isArray) {
		this(fieldName, elementType);
		this.multiValued = isArray;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((elementType == null) ? 0 : elementType.hashCode());
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result + (multiValued ? 1231 : 1237);
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
		DocumentElement other = (DocumentElement) obj;
		if (elementType != other.elementType)
			return false;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (multiValued != other.multiValued)
			return false;
		return true;
	}
	
}
