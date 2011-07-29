package de.julielab.semedico.core;

import de.julielab.semedico.core.MultiHierarchy.IMultiHierarchyNode;


/**
 * 
 * @author faessler
 * 
 */
public class Label implements Comparable<Label> {

	private Long hits;
	private IMultiHierarchyNode term;

	private boolean hasChildHits;

	public Label(IMultiHierarchyNode term) {
		this.term = term;
		this.hits = 0L;
	}

	public Long getHits() {
		return hits;
	}

	public void setHits(Long hits) {
		this.hits = hits;
	}

	public IMultiHierarchyNode getTerm() {
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
