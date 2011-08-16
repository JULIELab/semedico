package de.julielab.semedico.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.search.IFacettedSearchService;
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
	// It is a mapping from a term's ID to its label for display.
	private Map<String, Label> labels;

	private Map<String, Map<String, Label>> hLabels;
	private Map<String, List<Label>> fLabels;

	// Total document hits in this facet. Note that this number is not just the
	// number of Labels/Terms in the associated facet: One document has
	// typically numerous terms associated with it.
	private Map<Facet, Long> totalFacetCounts;

	private ILabelCacheService labelCacheService;


	private IFacettedSearchService searchService;

	private final ITermService termService;

	
	public FacetHit(Map<String, Label> labels,
			ILabelCacheService labelCacheService,
			ITermService termService, IFacettedSearchService searchService) {
		this.labels = labels;
		this.labelCacheService = labelCacheService;
		this.termService = termService;
		this.searchService = searchService;
		this.totalFacetCounts = new HashMap<Facet, Long>();

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

	/**
	 * @return the hitFacetTermLabels
	 */
	public Map<String, Label> getHitFacetTermLabels() {
		return labels;
	}

	/**
	 * @param allIds
	 * 
	 */
	public void updateLabels(List<String> allIds) {
		List<String> newIds = new ArrayList<String>();
		for (String id : allIds)
			if (!labels.containsKey(id))
				newIds.add(id);
		// TODO only if there are new ids
		labels.putAll(searchService.getFacetCountsForTermIds(newIds));
	}

	/**
	 * @param selectedFacetGroup
	 */
	public void getLabelsForFacetGroup(FacetGroup selectedFacetGroup) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param hitFacetTermLabels
	 */
	public void setLabels(Map<String, Label> hitFacetTermLabels) {
		this.labels = hitFacetTermLabels;
		
	}

	/**
	 * @param term
	 * @return
	 */
	public List<Label> getLabelsForHitChildren(IFacetTerm term) {
		List<Label> retLabels = new ArrayList<Label>();
		Iterator<IFacetTerm> childIt = term.childIterator();
		while (childIt.hasNext()) {
			Label l = labels.get(childIt.next().getId());
			if (l != null)
				retLabels.add(l);
		}
		Collections.sort(retLabels);
		return retLabels;
	}

	/**
	 * @param facet
	 * @return
	 */
	public List<Label> getLabelsForHitFacetRoots(Facet facet) {
		List<Label> retLabels = new ArrayList<Label>();
		Iterator<IFacetTerm> rootIt = termService.getFacetRoots(facet).iterator();
		while (rootIt.hasNext()) {
			Label l = labels.get(rootIt.next().getId());
			if (l != null)
				retLabels.add(l);
		}
		Collections.sort(retLabels);
		return retLabels;
	}
}
