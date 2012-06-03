package de.julielab.semedico.core;

import java.util.List;

public class FacetedSearchResult {

	private List<DocumentHit> documentHits;
	private int totalHits;
	private long elapsedTime;
	
	public FacetedSearchResult(List<DocumentHit> documentHits, int totalHits) {
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

	public List<DocumentHit> getDocumentHits() {
		return documentHits;
	}
	
	public int getTotalHits() {
		return totalHits;
	}

}
