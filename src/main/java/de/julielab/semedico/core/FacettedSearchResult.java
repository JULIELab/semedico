package de.julielab.semedico.core;

import java.util.Collection;

public class FacettedSearchResult {

	private Collection<DocumentHit> documentHits;
	private FacetHit facetHit;
	private int totalHits;
	
	public FacettedSearchResult(FacetHit facetHit, Collection<DocumentHit> documentHits, int totalHits) {
		super();
		this.facetHit = facetHit;
		this.documentHits = documentHits;
		this.totalHits = totalHits;
	}
	
	public Collection<DocumentHit> getDocumentHits() {
		return documentHits;
	}
	
	public FacetHit getFacetHit() {
		return facetHit;
	}
	
	public int getTotalHits() {
		return totalHits;
	}
}
