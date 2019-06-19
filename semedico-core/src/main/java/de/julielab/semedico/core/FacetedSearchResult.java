package de.julielab.semedico.core;

import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;

import java.util.List;

public class FacetedSearchResult {

	private List<HighlightedSemedicoDocument> documentHits;
	private int totalHits;
	private long elapsedTime;
	
	public FacetedSearchResult(List<HighlightedSemedicoDocument> documentHits, int totalHits) {
		super();
		this.documentHits = documentHits;
		this.totalHits = totalHits;
	}
	
	/**
	 * @return the elapsedTime
	 */
	public long getElapsedTime() {
		return elapsedTime;
	}

	/**
	 * @param elapsedTime the elapsedTime to set
	 */
	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public List<HighlightedSemedicoDocument> getDocumentHits() {
		return documentHits;
	}
	
	public int getTotalHits() {
		return totalHits;
	}

}
