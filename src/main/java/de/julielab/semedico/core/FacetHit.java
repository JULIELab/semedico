package de.julielab.semedico.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.Taxonomy.MultiHierarchyNode;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.search.ILabelCacheService;

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

	// This is here to keep the facet counts of a particular search available.
	private Map<String, Label> labels;

	// Total document hits in this facet. Note that this number is not just the
	// number of Labels/Terms in the associated facet: One document has
	// typically numerous terms associated with it.
	private Map<Facet, Long> totalFacetCounts;

	private final ILabelCacheService labelCacheService;

	private final ITermService termService;

	public FacetHit(ILabelCacheService labelCacheService,
			ITermService termService) {
		this.labelCacheService = labelCacheService;
		this.termService = termService;
		this.labels = new HashMap<String, Label>();
		this.totalFacetCounts = new HashMap<Facet, Long>();
	}

	public void addLabel(String termId, long frequency) {
		// labels.put(label.getId(), label);

		// First check, whether we already have added the label for termId. This
		// may happen when a label for a sub term of the term with ID termId is
		// added first.
		Label label = labels.get(termId);
		if (label == null) {
			label = labelCacheService.getCachedLabel(termId);
			labels.put(termId, label);
		}
		label.setHits(frequency);
		// Mark the parent term as having a sub term hit. If we
		// don't already have met the parent term, we just
		// create it now and set its hits later when the loop
		// comes to it.
		// We can do that because whenever a term has been hit, all his parents
		// must also have been hit (IS-A relation).
		for (IFacetTerm parentTerm : label.getTerm().getAllParents()) {
			Label parentLabel = labels.get(parentTerm.getId());
			if (parentLabel == null) {
				parentLabel = labelCacheService.getCachedLabel(parentTerm
						.getId());
				labels.put(parentTerm.getId(), parentLabel);
			}
			parentLabel.setHasChildHits();
		}
	}

	/**
	 * @param facet
	 * @return
	 */
	public List<Label> getHitFacetRoots(Facet facet) {
		Collection<IFacetTerm> roots = termService.getFacetRoots(facet);
		Iterator<IFacetTerm> rootIt = roots.iterator();
		List<Label> retLabels = new ArrayList<Label>();
		while (rootIt.hasNext()) {
			IFacetTerm root = rootIt.next();
			if (labels.containsKey(root.getId()))
				retLabels.add(labels.get(root.getId()));
		}
		return retLabels;
	}

	/**
	 * @param id
	 * @return
	 */
	public List<Label> getHitChildren(String id) {
		IFacetTerm term = termService.getNode(id);
		Iterator<IFacetTerm> childIt = term.childIterator();
		List<Label> retLabels = new ArrayList<Label>();
		while (childIt.hasNext()) {
			IFacetTerm child = childIt.next();
			if (labels.containsKey(child.getId()))
				retLabels.add(labels.get(child.getId()));
		}
		return retLabels;
	}

	public void setTotalFacetCount(Facet facet, long totalHits) {
		this.totalFacetCounts.put(facet, totalHits);
	}

	public long getTotalFacetCount(Facet facet) {
		Long count = totalFacetCounts.get(facet);
		return count == null ? 0 : count;
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
	 * 
	 */
	public void clear() {
		labelCacheService.releaseHierarchy(labels.values());
		labels.clear();
	}
}
