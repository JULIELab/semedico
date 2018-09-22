package de.julielab.semedico.core.entities.documents;

/**
 * An enumeration of types for document elements. Document elements are title,
 * abstract, the authors etc. This is output element oriented and is the
 * foundation of the dynamic document definition system: Each document consists
 * of multiple elements, each of which is of a specific type. With this
 * information, the frontend should be able to display the document.
 * 
 * @author faessler
 *
 */
public enum DocumentElementType {
	DE_HEADING, DE_TEXT, DE_AUTHORS, DE_JOURNAL, DE_DATE, DE_SNIPPET, DE_GENERIC
}
