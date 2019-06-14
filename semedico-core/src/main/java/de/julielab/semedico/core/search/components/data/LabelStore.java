package de.julielab.semedico.core.search.components.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.julielab.elastic.query.components.data.aggregation.TermsAggregation;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.search.LabelCacheService;
import de.julielab.semedico.core.search.interfaces.ILabelCacheService;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.services.UIService;

/**
 * @author faessler
 */
public class LabelStore {

    public final Map<UIFacet, Set<Label>> fullyUpdatedLabelSets;
    private final Set<String> alreadyQueriedTermIds;
    // Hierarchical Labels always refer to a term and thus are always
    // TermLabels. The map's keys are term IDs.
    public Map<String, TermLabel> labelsHierarchical;
    // Flat Labels may refer to terms in facet which have been set to flat state
    // by the user or StringLabels belonging to a genuinely flat facet facetSource.
    // The map's keys are facet IDs.
    private Map<String, List<Label>> labelsFlat;
    // Total document hits in this facet. Note that this number is not just the
    // number of Labels/Terms in the associated facet: One document has
    // typically numerous terms associated with it.
    private Map<Facet, Long> totalFacetCounts;
    private Set<FacetGroup<UIFacet>> facetGroupsWithLabels;
    private ILabelCacheService labelCacheService;
    private List<String> alreadyQueriedStringFacets;
    private Object currentQuery;

    public LabelStore(ILabelCacheService labelCacheService) {
        this.labelCacheService = labelCacheService;
        this.totalFacetCounts = new HashMap<Facet, Long>();

        this.labelsHierarchical = new HashMap<String, TermLabel>();
        this.labelsFlat = new HashMap<>();
        this.fullyUpdatedLabelSets = new HashMap<>();
        this.alreadyQueriedTermIds = new HashSet<>(200);
        this.facetGroupsWithLabels = new HashSet<>();
        this.alreadyQueriedStringFacets = new ArrayList<>();
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

    public void sortFlatLabelsForFacet(String facetId) {
        List<Label> labels = labelsFlat.get(facetId);
        if (null == labels || null == facetId)
            return;
        Collections.sort(labels);
    }

    /**
     * Actually, also non-string-labels can be added. They will only be shown
     * when the FacetConfiguration is set to flat mode (forced or inherently).
     *
     * @param label
     * @param facetId
     */
    public void addLabelForFacet(Label label, String facetId) {
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
            b.append(String.format("Facet: %s. Total number of document hits for this facet: %d", facet.getName(),
                    totalFacetCounts.get(facet)));
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
        alreadyQueriedTermIds.clear();
        alreadyQueriedStringFacets.clear();
        facetGroupsWithLabels.clear();
        totalFacetCounts.clear();
        currentQuery = null;
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
    public Map<String, List<Label>> getFlatLabels() {
        return labelsFlat;
    }

    /**
     * @param facetConfiguration
     * @return
     */
    public List<Label> getFlatLabels(UIFacet facetConfiguration) {
        return labelsFlat.get(facetConfiguration.getId());
    }


    public void addQueriedConceptId(String termId) {
        alreadyQueriedTermIds.add(termId);
    }

    public boolean termIdAlreadyQueried(String termId) {
        return alreadyQueriedTermIds.contains(termId);
    }

    public void addQueriedStringFacet(String facetId) {
        alreadyQueriedStringFacets.add(facetId);
    }

    public boolean stringFacetAlreadyQueried(String facetId) {
        return alreadyQueriedStringFacets.contains(facetId);
    }

    public void setFacetGroupHasLabels(FacetGroup<UIFacet> facetGroup) {
        facetGroupsWithLabels.add(facetGroup);
    }

    /**
     * Indicates whether the currently shown facets of a facet group have all
     * labels required for display. This is currently determined by just setting
     * this to true for facet groups where the children of the currently shown
     * terms have been counted.
     *
     * @param facetGroup
     * @return
     */
    public boolean hasFacetGroupLabels(FacetGroup<UIFacet> facetGroup) {
        return facetGroupsWithLabels.contains(facetGroup);
    }

    /**
     * <p>
     * Method to determine those concept IDs for which we do not yet have counts relative to the given query object.
     * </p>
     * <p>
     * We load facet concepts for display with they frequency count or score only when the respective facet is
     * actually displayed to the user. This requires an approach that always checks if the displayed
     * facets have either already been displayed before - without changing the query - or if the concepts
     * contained in the requested facets have already been loaded in the context of another facet (since
     * facets may conceptually overlap). It is important to note that this only makes sense as long as the query
     * doesn't change. In the moment a new query is issued, all counts become obsolete.
     * </p>
     * <p>
     * To determine if the query has changed, the passed query object should be the one that actually determines
     * the set of documents returned from the search index. This is typically an instance of {@link de.julielab.semedico.core.parsing.ParseTree}.
     * The passed query object is always retained as the current query. In case that a newly passed query does not
     * {@link Object#equals(Object)} the formely saved query, the internal cache of already queried concept IDs
     * is first cleared.
     * </p>
     *
     * @param facets        The facets to be displayed and for which to determine which concept IDs have to be counted.
     * @param uiService     The {@link UIService}.
     * @param query         The object that determines the documents to returned, typically a {@link de.julielab.semedico.core.parsing.ParseTree}.
     * @param markAsQueried If set to <tt>true</tt>, the returned concept IDs will already be marked as queried and not returned any more for a subsequent call to this method with the same query.
     * @return The concept IDs of the given facets that have not yet been counted for the current query.
     */
    public Multimap<UIFacet, String> getUncountedConceptIdsForFacets(List<UIFacet> facets, UIService uiService, Object query, boolean markAsQueried) {
        if (this.currentQuery != null && !this.currentQuery.equals(query))
            alreadyQueriedTermIds.clear();
        this.currentQuery = query;
        Multimap<UIFacet, String> conceptsToCount = HashMultimap.create();
        Multimap<UIFacet, String> displayedConceptIds = uiService.getDisplayedConceptIdsInFacetGroup(facets);
        for (UIFacet facet : facets) {
            Collection<String> conceptIdsInFacet = displayedConceptIds.get(facet);
            for (String conceptId : conceptIdsInFacet) {
                if (termIdAlreadyQueried(conceptId))
                    continue;

                conceptsToCount.put(facet, conceptId);
                if (markAsQueried)
                    addQueriedConceptId(conceptId);
            }
        }

        return conceptsToCount;
    }

}
