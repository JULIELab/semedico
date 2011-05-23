package de.julielab.stemnet.core;

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
public class FacetHit {

	// The Facet for which Term hit counts are stored.
	private Facet facet;
	// Term hit counts.
	private List<Label> labels;
	private boolean visible;
	// Total hits in this facet (...should just be the sum of the the hit fields in the labels (?) EF, 16.04.2011).
	private int totalHits;

	public FacetHit(Facet facet) {
		super();
		this.facet = facet;
		labels = new ArrayList<Label>();
		visible = true;
	}

	public Facet getFacet() {
		return facet;
	}

	public void setFacet(Facet fracet) {
		this.facet = fracet;
	}

	public List<Label> getLabels() {
		return labels;
	}

	public void setLabels(List<Label> labels) {
		this.labels = labels;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void sortLabels() {
		Collections.sort(labels);
	}

	public int getTotalHits() {
		return totalHits;
	}

	public void setTotalHits(int totalHits) {
		this.totalHits = totalHits;
	}
}
