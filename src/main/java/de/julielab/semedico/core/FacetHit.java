package de.julielab.semedico.core;

import java.util.HashMap;
import java.util.Map;

import de.julielab.semedico.core.MultiHierarchy.LabelMultiHierarchy;

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

	/**
	 * Default.
	 */
	private static final long serialVersionUID = 1L;

	// This is here to keep the facet counts of a particular search available.
	// Thus, the service is not injected here, this is done in the
	// FacetHitCollector.
	private LabelMultiHierarchy labelHierarchy;

	private boolean visible;

	// Total document hits in this facet. Note that this number is not just the
	// number of Labels/Terms in the associated facet: One document has
	// typically numerous terms associated with it.
	private Map<Facet, Long> totalFacetCounts;

	public FacetHit(LabelMultiHierarchy labelHierarchy) {
		super();
		this.labelHierarchy = labelHierarchy;
		this.totalFacetCounts = new HashMap<Facet, Long>();
		visible = true;
	}

	public void setTotalFacetCount(Facet facet, long totalHits) {
		this.totalFacetCounts.put(facet, totalHits);
	}

	public long getTotalFacetCount(Facet facet) {
		Long count = totalFacetCounts.get(facet);
		return count == null ? 0 : count;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public LabelMultiHierarchy getLabelHierarchy() {
		return labelHierarchy;
	}

	public void setLabelHierarchy(LabelMultiHierarchy labelHierarchy) {
		this.labelHierarchy = labelHierarchy;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (Facet facet : totalFacetCounts.keySet()) {
			b.append(String
					.format("Facet: %s. Total number of document hits for this facet: %d",
							facet.getName(), totalFacetCounts.get(facet)));
			b.append("\n");
		}
		return b.toString();
	}

	/**
	 * Releases resources held by this <code>FacetHit</code>, in particular the
	 * <code>labelHierarchy</code>. The hierarchy is given back to the
	 * LabelCacheService and may be used again.
	 */
	public void clear() {
		labelHierarchy.release();
	}

}
