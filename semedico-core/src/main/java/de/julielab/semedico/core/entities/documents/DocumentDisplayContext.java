package de.julielab.semedico.core.entities.documents;

/**
 * Constants for different contexts in which documents may be displayed in the
 * frontend. This information is used together with the facetSource or type or
 * information of search server documents to determine the {@link DocumentModel}
 * to apply.
 * 
 * @author faessler
 *
 */
public enum DocumentDisplayContext {
	/**
	 * The document should be displayed as a hit list item.
	 */
	HITLIST,
	/**
	 * The document should be displayed for reading or detailed view.
	 */
	READER
}
