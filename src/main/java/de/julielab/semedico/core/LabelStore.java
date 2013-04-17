package de.julielab.semedico.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.julielab.semedico.search.LabelCacheService;
import de.julielab.semedico.search.interfaces.ILabelCacheService;

/**
 * 
 * @author faessler
 * 
 */
public class LabelStore {

	// This is here to keep the facet counts of a particular search available.
	// It is a mapping from a term's ID to its label for display.
	// private Map<Facet.Source, Object> labels;

	// Hierarchical Labels always refer to a term and thus are always
	// TermLabels. The map's keys are term IDs.
	public Map<String, TermLabel> labelsHierarchical;

	// Flat Labels may refer to terms in facet which have been set to flat state
	// by the user or StringLabels belonging to a genuinely flat facet source.
	// The map's keys are facet IDs.
	private Map<Integer, List<Label>> labelsFlat;

	// Total document hits in this facet. Note that this number is not just the
	// number of Labels/Terms in the associated facet: One document has
	// typically numerous terms associated with it.
	private Map<Facet, Long> totalFacetCounts;

	private final Set<String> alreadyQueriesTermIds;

	private Set<FacetGroup<UIFacet>> facetGroupsWithLabels;

	private ILabelCacheService labelCacheService;


	public final Map<UIFacet, Set<Label>> fullyUpdatedLabelSets;

	public LabelStore(ILabelCacheService labelCacheService) {
		this.labelCacheService = labelCacheService;
		this.totalFacetCounts = new HashMap<Facet, Long>();

		// this.labels = new HashMap<Facet.Source, Object>();
		this.labelsHierarchical = new HashMap<String, TermLabel>();
		this.labelsFlat = new HashMap<Integer, List<Label>>();
		this.fullyUpdatedLabelSets = new HashMap<UIFacet, Set<Label>>();
		this.alreadyQueriesTermIds = new HashSet<String>(200);
		this.facetGroupsWithLabels = new HashSet<FacetGroup<UIFacet>>();
	}

	public void setTotalFacetCount(Facet facet, long totalHits) {
		this.totalFacetCounts.put(facet, totalHits);
	}

	public void incrementTotalFacetCount(Facet facet, long additionalHits) {
		Long count = this.totalFacetCounts.get(facet);
		if (count == null)
			count = new Long(1);
		else
			count += 1;
		this.totalFacetCounts.put(facet, count);
	}

	public long getTotalFacetCount(Facet facet) {
		Long count = totalFacetCounts.get(facet);
		return count == null ? 0 : count;
	}

	public void addTermLabel(TermLabel label) {
		labelsHierarchical.put(label.getId(), label);
	}

	public void sortFlatLabelsForFacet(int facetId) {
		Collections.sort(labelsFlat.get(facetId));
	}

	/**
	 * Actually, also non-string-labels can be added. They will only be shown
	 * when the FacetConfiguration is set to flat mode (forced or inherently).
	 * 
	 * @param label
	 * @param facetId
	 */
	public void addLabelForFacet(Label label, Integer facetId) {
		List<Label> labelList = labelsFlat.get(facetId);
		if (labelList == null) {
			labelList = new ArrayList<Label>();
			labelsFlat.put(facetId, labelList);
		}
		labelList.add(label);
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
	 * <p>
	 * Clears the contents of the <code>FacetHit</code> but does not change user
	 * interface states (e.g. the batch size of <code>DisplayGroups</code> is
	 * not altered).
	 * </p>
	 * <p>
	 * This method releases all <code>Label</code> objects to the
	 * {@link LabelCacheService} and clears the <code>DisplayGroups</code>.
	 * </p>
	 */
	public void clear() {
		labelCacheService.releaseLabels(labelsHierarchical.values());
		labelsHierarchical.clear();
		for (Collection<Label> labels : labelsFlat.values())
			labelCacheService.releaseLabels(labels);
		labelsFlat.clear();
		fullyUpdatedLabelSets.clear();
		alreadyQueriesTermIds.clear();
		facetGroupsWithLabels.clear();
		totalFacetCounts.clear();
	}

	public void reset() {
		clear();
	}


	/**
	 * @return the labelsHierarchical
	 */
	public Map<String, TermLabel> getLabelsHierarchical() {
		return labelsHierarchical;
	}

	/**
	 * @return the labelsFlat
	 */
	public Map<Integer, List<Label>> getFlatLabels() {
		return labelsFlat;
	}

	/**
	 * @param facetConfiguration
	 * @return
	 */
	public List<Label> getFlatLabels(UIFacet facetConfiguration) {
		return labelsFlat.get(facetConfiguration.getId());
	}


	/**
	 * <p>
	 * Retrieves the labels which should currently be ready to display for the
	 * <code>FacetBox</code> component associated with
	 * <code>facetConfiguration</code> and puts them into the
	 * <code>displayGroup</code> object associated with this
	 * <code>FacetBox</code>.
	 * </p>
	 * <p>
	 * For facets in hierarchical mode, these labels are the roots of the
	 * currently selected term-subtree in this facet. That is, the children of
	 * the last term on the drill-down-path of <code>facetConfiguration</code>
	 * or the facet roots if the facet is not drilled down at all.
	 * </p>
	 * <p>
	 * For facets in flat mode, these labels are given by the sorted list of
	 * labels retrieved from the facet's source (the top N terms in the Solr
	 * field associated with the facet).
	 * </p>
	 * 
	 * @param facetConfiguration
	 *            The <code>facetConfiguration</code> for whose
	 *            <code>FacetBox</code> the correct labels are to be determined
	 *            and filled into the <code>DisplayGroup</code> meant for this
	 *            <code>FacetBox</code>.
	 */

	public void addQueriedTermId(String termId) {
		alreadyQueriesTermIds.add(termId);
	}

	public boolean termIdAlreadyQueried(String termId) {
		return alreadyQueriesTermIds.contains(termId);
	}

	public void setFacetGroupHasLabels(FacetGroup<UIFacet> facetGroup) {
		facetGroupsWithLabels.add(facetGroup);
	}

	public boolean hasFacetGroupLabels(FacetGroup<UIFacet> facetGroup) {
		return facetGroupsWithLabels.contains(facetGroup);
	}

}
