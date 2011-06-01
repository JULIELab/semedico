package de.julielab.semedico.core;

import de.julielab.semedico.core.MultiHierarchy.MultiHierarchyNode;
import de.julielab.semedico.search.FacetHitCollectorService;

/**
 * Associated with a particular Term. Holds the information about how often this
 * Term has been found in a concrete search query. Created by
 * {@link FacetHitCollectorService#collectResults}.
 * 
 * @author faessler
 * 
 */
public class Label extends MultiHierarchyNode implements Comparable<Label> {

	private Long hits;
	private FacetTerm term;
	private long searchTimestamp = -2;

	private boolean hasChildHits;

	public Label(FacetTerm term) {
		super(term.getId(), term.getName());
		this.term = term;
		this.hits = 0L;
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
	
	public void setSearchTimestamp(long searchTimestamp) {
		this.searchTimestamp = searchTimestamp;
	}
	
	public long getSearchTimestamp() {
		return searchTimestamp;
	}

	@Override
	public String toString() {
		return "Label: " + name + ", number of hits: " + hits + ", timestamp of last hit: " + searchTimestamp;
	}
}
