package de.julielab.semedico.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * For a particular Facet, holds information about the total hit count of Terms
 * in this facets and which Term in this Facet has been hit how often in a
 * document search. This information in stored in the <code>labels</code> field
 * which stores for each Term how often this Term has been found.
 * 
 * @author faessler
 * 
 */
public class FacetHit extends ArrayList<Label> {

	/**
	 * Default.
	 */
	private static final long serialVersionUID = 1L;

	// The Facet for which Term hit counts are stored.
	private Facet facet;
	// Term hit counts.
	private boolean visible;

	// Total document hits in this facet. Note that this number is not just the
	// number of Labels/Terms in the associated facet: One document has
	// typically numerous terms associated with it.
	private long totalHits;

	public FacetHit(Facet facet) {
		super();
		this.facet = facet;
		visible = true;
	}

	public Facet getFacet() {
		return facet;
	}

	public void setFacet(Facet fracet) {
		this.facet = fracet;
	}

	public void setTotalHits(long totalHits) {
		this.totalHits = totalHits;
	}
	
	public long getTotalHits() {
		return totalHits;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void sortLabels() {
		Collections.sort(this);
	}

	/**
	 * First clears all labels stored in this <code>FacetHit</code>, then calls
	 * <code>ArrayList</code>'s <code>clear</code> method.
	 */
	@Override
	public void clear() {
		for (Label l : this)
			l.clear();
		super.clear();
	}

	@Override
	public String toString() {
		return String.format(
				"Facet: %s. Total number of document hits for this facet: %d",
				facet.getName(), size());
	}

}
