package de.julielab.semedico.core;

import java.util.ArrayList;
import java.util.List;


public class FacetSuggestionHit {
	private Facet facet;
	private List<SuggestionHit> suggestionHits;
	private int completeSize;
	
	public FacetSuggestionHit(Facet facet) {
		super();
		this.facet = facet;
		suggestionHits = new ArrayList<SuggestionHit>();
	}
		
	public List<SuggestionHit> getTermHits() {
		return suggestionHits;
	}
	
	public void setTermHits(List<SuggestionHit> suggestionHits) {
		this.suggestionHits = suggestionHits;
	}

	public Facet getFacet() {
		return facet;
	}

	public void setFacet(Facet facet) {
		this.facet = facet;
	}

	public int getCompleteSize() {
		return completeSize;
	}

	public void setCompleteSize(int completeSize) {
		this.completeSize = completeSize;
	}

	@Override
	public String toString() {
		return "{ facet: "+facet.getName() + " hits:"+ completeSize+ " }"; 
	}
}
