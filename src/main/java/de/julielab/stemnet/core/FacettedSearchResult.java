package de.julielab.stemnet.core;

import java.util.Collection;
import java.util.List;

public class FacettedSearchResult {

	private Collection<DocumentHit> documentHits;
	private List<FacetHit> facetHits;
	private int totalHits;
	
	public FacettedSearchResult(List<FacetHit> facetHits, Collection<DocumentHit> documentHits, int totalHits) {
		super();
		this.facetHits = facetHits;
		this.documentHits = documentHits;
		this.totalHits = totalHits;
	}
	
	public Collection<DocumentHit> getDocumentHits() {
		return documentHits;
	}
	
	public List<FacetHit> getFacetHits() {
		return facetHits;
	}
	
	public int getTotalHits() {
		return totalHits;
	}
}
