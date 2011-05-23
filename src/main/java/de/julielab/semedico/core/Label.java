package de.julielab.semedico.core;

import java.util.ArrayList;
import java.util.List;

import de.julielab.semedico.search.FacetHitCollectorService;

/**
 * Associated with a particular Term. Holds the information about how often this Term has been found in a concrete search query.
 * Created by {@link FacetHitCollectorService#collectResults}.
 * @author faessler
 *
 */
public class Label implements Comparable<Label>{

	private Integer hits;
	private FacetTerm term;

	private boolean hasChildHits;
	
	public Label() {
	}
	
	public Label(FacetTerm term) {
		super();
		this.term = term;
		hits = 0;
	}

	public Label(FacetTerm term, Integer hits) {
		super();
		this.term = term;
		this.hits = hits;
	}
	
	public Integer getHits() {
		return hits;
	}
	
	public void setHits(Integer hits) {
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
		return label.getHits() - hits;
	}

	public boolean hasChildHits() {
		return hasChildHits;
	}

	public void setHasChildHits(boolean hasChildHits) {
		this.hasChildHits = hasChildHits;
	}

	public void clear() {
		hits = 0;
		hasChildHits = false;
		term = null;
	}
}
