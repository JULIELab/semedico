package de.julielab.semedico.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.search.IFacetedSearchService;
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
	// private Map<Facet.Source, Object> labels;

	private Map<String, Label> labelsHierarchical;

	private Map<Integer, List<Label>> labelsFlat;

	// Total document hits in this facet. Note that this number is not just the
	// number of Labels/Terms in the associated facet: One document has
	// typically numerous terms associated with it.
	private Map<Facet, Long> totalFacetCounts;

	private ILabelCacheService labelCacheService;

	private final ITermService termService;

	private final Logger logger;

	public FacetHit(Logger logger, HashMap<String, TermLabel> hashMap,
			ILabelCacheService labelCacheService, ITermService termService) {
		this.logger = logger;
		this.labelCacheService = labelCacheService;
		this.termService = termService;
		this.totalFacetCounts = new HashMap<Facet, Long>();

		// this.labels = new HashMap<Facet.Source, Object>();
		this.labelsHierarchical = new HashMap<String, Label>();
		this.labelsFlat = new HashMap<Integer, List<Label>>();

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
		labelCacheService.releaseLabels(labelsHierarchical.values());
		labelsHierarchical.clear();
		for (Collection<Label> labels : labelsFlat.values())
			labelCacheService.releaseLabels(labels);
		labelsFlat.clear();
	}

	/**
	 * Returns for each FacetSource all labels of term counts which have been
	 * collected so far.
	 * <p>
	 * Right after a search, the returned map is filled with all labels required
	 * to display exactly those facets and their labels correctly which will be
	 * rendered to the user. On further events which do not cause a new search
	 * but new facets/labels to be displayed, the very same map will be
	 * supplemented by the newly required labels, preserving the "old" labels in
	 * case the user chooses to view them again (by drilling a facet up again or
	 * returning to a tab already visited).
	 * </p>
	 * <p>
	 * For hierarchical facet sources, the source's value in the map will be
	 * another map <code>Map&lt;String, Label&gt;</code>. It maps term IDs to
	 * the label corresponding to the term.<br>
	 * For flat facet sources, the value is a <code>List&lt;Label&gt;</code>,
	 * representing a frequency-ordered list of of labels. Note that flat facet
	 * sources do need need to be updated on front end updates as with
	 * hierarchical sources. For flat facets the objects displayed are fixed for
	 * each search (because there is nothing to browse like a tree/general
	 * hierarchy).
	 * <p>
	 * 
	 * @return The hit facet term labels.
	 */
	// public Map<Source, Object> getHitFacetTermLabels() {
	// return labels;
	// }

	/**
	 * @param allDisplayedTerms
	 * 
	 */
	// public void updateLabels(Map<FacetConfiguration, Set<IFacetTerm>>
	// allDisplayedTerms) {
	// Multimap<FacetConfiguration, IFacetTerm> newIds = HashMultimap.create();
	// for (FacetConfiguration facetConfiguration : allDisplayedTerms.keySet())
	// {
	// // Only hierarchical facet labels must be updated, as "update"
	// // always means a click-event (onTabSelect, onDrillDown, on...)
	// // which causes an Ajax-Request rather than a new search. Flat
	// // facets are computed only once per search.
	// if (!facetConfiguration.getFacet().isHierarchical())
	// throw new IllegalStateException(
	// facetConfiguration
	// + " is not hierarchic yet particular term counts are questioned"
	// + " (which makes no sense for flat facets)");
	// // Get the label map served by this facet's source (which may serve
	// // multiple facets).
	// @SuppressWarnings("unchecked")
	// Map<String, Label> facetLabels = (Map<String, Label>) labels
	// .get(facetConfiguration.getFacet().getSource());
	// // Add all term IDs to our new "Label order" which are already
	// // present for the current facet source.
	// for (IFacetTerm id : allDisplayedTerms.get(facetConfiguration)) {
	// if (!facetLabels.containsKey(id))
	// newIds.put(facetConfiguration, id);
	// }
	// }
	// if (newIds.size() > 0)
	// searchService.queryAndStoreHierarchichalFacetCounts(newIds, this);
	// }

	/**
	 * @param selectedFacetGroup
	 */
	// public void getLabelsForFacetGroup(FacetGroup selectedFacetGroup) {
	// // TODO Auto-generated method stub
	//
	// }

	/**
	 * @param hitFacetTermLabels
	 */
	// public void setLabels(Map<Source, Object> hitFacetTermLabels) {
	// this.labels = hitFacetTermLabels;
	//
	// }

	/**
	 * Returns the labels corresponding to the children of <code>term</code>
	 * with respect to <code>facet</code>.
	 * <p>
	 * <code>term</code> should be contained in <code>facet</code> in order to
	 * achieve meaningful results.<br>
	 * Only labels of <code>term</code>'s children which are also contained in
	 * <code>facet</code> are returned, thus delivering a filter mechanism for
	 * facets which exclude particular terms (like the aging facets which are a
	 * subset of MeSH but exclude most terms).
	 * </p>
	 * 
	 * @param term
	 *            The term for whose children labels should be returned.
	 * @param facet
	 *            The facet which constrains the children returned to those
	 *            which are also included in <code>facet</code>.
	 * @return
	 */
	public List<Label> getLabelsForHitChildren(IFacetTerm term, Facet facet) {

		List<Label> retLabels = new ArrayList<Label>();
		Iterator<IFacetTerm> childIt = term.childIterator();
		while (childIt.hasNext()) {
			IFacetTerm child = childIt.next();
			if (!child.isContainedInFacet(facet))
				continue;
			Label l = labelsHierarchical.get(child.getId());
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
		Iterator<IFacetTerm> rootIt = termService.getFacetRoots(facet)
				.iterator();
		while (rootIt.hasNext()) {
			Label l = labelsHierarchical.get(rootIt.next().getId());
			if (l != null)
				retLabels.add(l);
		}
		Collections.sort(retLabels);
		return retLabels;
	}

	/**
	 * @return the labelsHierarchical
	 */
	public Map<String, Label> getLabelsHierarchical() {
		return labelsHierarchical;
	}

	/**
	 * @return the labelsFlat
	 */
	public Map<Integer, List<Label>> getLabelsFlat() {
		return labelsFlat;
	}
}
