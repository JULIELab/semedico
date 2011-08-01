package de.julielab.semedico.core;

import de.julielab.semedico.core.Taxonomy.IFacetTerm;


/**
 * 
 * @author faessler
 * 
 */
public class Label implements Comparable<Label> {

	private Long hits;
	private IFacetTerm term;

	private boolean hasChildHits;

	public Label(IFacetTerm term) {
		this.term = term;
		this.hits = 0L;
	}

	public Long getHits() {
		return hits;
	}

	public void setHits(Long hits) {
		this.hits = hits;
	}

	public IFacetTerm getTerm() {
		return term;
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
	
	public String getId() {
		return term.getId();
	}

	public void clear() {
		hits = 0L;
		hasChildHits = false;
		term = null;
	}
	
}
