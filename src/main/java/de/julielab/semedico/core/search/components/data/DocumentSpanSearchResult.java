package de.julielab.semedico.core.search.components.data;

/**
 * A search result representing a portion of a (probably) larger document. For example sentences, phrases, statements etc.
 * @author faessler
 *
 */
public class DocumentSpanSearchResult extends SemedicoSearchResult {
	protected String id;
	protected String docId;
	protected SemedicoDocument documentRef;
	protected int[] beginOffsets;
	protected int[] endOffsets;
	
	// TODO create as convenient for swift creation
	public DocumentSpanSearchResult() {
		// TODO Auto-generated constructor stub
	}
	
	public String getId() {
		return id;
	}
	public String getDocId() {
		return docId;
	}
	public SemedicoDocument getDocumentRef() {
		return documentRef;
	}
	public int[] getBeginOffsets() {
		return beginOffsets;
	}
	public int[] getEndOffsets() {
		return endOffsets;
	}
	
}
