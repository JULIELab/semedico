package de.julielab.semedico.core;

import de.julielab.semedico.search.FacetHitCollectorService;

/**
 * Associated with a particular Term. Holds the information about how often this Term has been found in a concrete search query.
 * Created by {@link FacetHitCollectorService#collectResults}.
 * @author faessler
 *
 */
public class Label implements Comparable<Label>{

	private Long hits;
	private FacetTerm term;

	private boolean hasChildHits;
	
	public Label() {
	}
	
	public Long getHits() {
		return hits;
	}
	
	public void setHits(Long hits) {
		this.hits = hits;
	}

	public FacetTerm getTerm() {
		return term;
	}

	public void setTerm(FacetTerm term) {
		this.term = term;
	}
	public void incHits(){
		hits++;
	}
	
	public int compareTo(Label label) {
		return Long.signum(label.getHits() - hits);
	}

	public boolean hasChildHits() {
		return hasChildHits;
	}

	public void setHasChildHits() {
		this.hasChildHits = true;
	}

	public void clear() {
		hits = 0L;
		hasChildHits = false;
		term = null;
	}
	
	@Override
	public String toString() {
		return "Term: " + term.getLabel() + ", number of hits: " + hits;
	}
}
