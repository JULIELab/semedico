package de.julielab.semedico.core.facets;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.julielab.neo4j.plugins.datarepresentation.constants.NodeIDPrefixConstants;
import de.julielab.semedico.commons.concepts.FacetLabels;
import de.julielab.semedico.commons.concepts.SemedicoFacetConstants;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.util.ConceptCreationException;
import de.julielab.semedico.core.util.ConceptLoadingException;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;

@JsonIgnoreProperties(value = {"facetGroup", "noFacet"})
public class Facet implements Comparable<Facet> {

    public static Facet KEYWORD_FACET = new Facet(NodeIDPrefixConstants.FACET + "-1", "Keyword", "keywords");
//	static {
//		KEYWORD_FACET.searchFieldNames = Arrays.asList(IIndexInformationService.MEDLINE_SEARCH_FIELDS);
//	}

    public static Facet CORE_TERMS_FACET = new Facet(NodeIDPrefixConstants.FACET + "-2", "Special Terms", "specialterms");
    //	static {
//		CORE_TERMS_FACET.searchFieldNames = Arrays.asList(IIndexInformationService.TITLE, IIndexInformationService.ABSTRACT);
//	}
    public static Facet MOST_INFORMATIVE_CONCEPTS_FACET = new Facet(NodeIDPrefixConstants.FACET + "-3", "Most Informative Concepts", "mostinformative");
    public static Facet MOST_FREQUENT_CONCEPTS_FACET = new Facet(NodeIDPrefixConstants.FACET + "-4", "Most Frequent Concepts", "mostfrequent");
    public static Facet BOOLEAN_OPERATORS_FACET = new Facet(NodeIDPrefixConstants.FACET + "-3", "Boolean Operators", "booleanoperators");

    static {
        FacetSource defaultFacetSource = new FacetSource(FacetSource.SourceType.FIELD_FLAT_TERMS, "conceptlist");
        MOST_INFORMATIVE_CONCEPTS_FACET.setSource(defaultFacetSource);
        MOST_FREQUENT_CONCEPTS_FACET.setSource(defaultFacetSource);
    }

    /**
     * Name of this facet. This is also used for display.
     */
    protected String name;
    protected String cssId;
    /**
     * The position of this facet when it comes to ordering for display. This only delivers a default-order within a
     * facet group. The order could be changed by the user, which will <em>not</em> reflect in another position number
     * (this class is part of the object model rather then of the session state!). The actual display order of facets
     * will be determined by their position in the session state object copy of FacetGroup in the searchConfiguration.
     */
    protected int position;
    // The facetSource of facet labels for this facet. This can be a field in the
    // index which contains (internally) hierarchical arranged terms. Another
    // field (e.g. for journals, authors...) could contain unordered facet
    // labels.
    @JsonDeserialize(using = SourceDeserializer.class)
    @JsonAlias({SemedicoFacetConstants.PROP_SOURCE_NAME, SemedicoFacetConstants.PROP_SOURCE_TYPE})
    protected FacetSource facetSource;
    /**
     * Identifier number of this facet.
     */
    private String id;
    private String customId;
    private Collection<String> searchFieldNames;
    private Collection<String> filterFieldNames;
    /**
     * A set of labels to mark particular facets, e.g. to be "The Authors facet" or "the BTerm Facet" and such.
     */
    private Set<FacetLabels.Unique> uniqueLabels;
    private Set<FacetLabels.General> labels;
    private String inducingTermId;
    /**
     * The number of root terms this facet has. This value is precomputed within the term database and then just loaded
     * from there. Only hierarchical facets have roots at all, so for flat facets this is <tt>null</tt>.
     */
    private int numRoots;
    private ITermService termService;
    private Set<de.julielab.semedico.commons.concepts.FacetLabels.General> aggregationLabels;
    private List<String> aggregationFields;
    private String shortName;

    private boolean active;

    /**
     * Exclusively used to generate {@link #KEYWORD_FACET}.
     *
     * @param name
     * @param cssId
     */
    private Facet(String id, String name, String cssId) {
        this.id = id;
        this.name = name;
        this.cssId = cssId;
        // A special facetSource which is of no facetSource type, not hierarchical and not
        // flat. This facetSource type should not occur anywhere else.
        this.facetSource = new FacetSource(FacetSource.SourceType.KEYWORD, "keywords") {

            /*
             * (non-Javadoc)
             *
             * @see de.julielab.semedico.core.Facet.FacetSource#isFlat()
             */
            @Override
            public boolean isFlat() {
                return true;
            }

            /*
             * (non-Javadoc)
             *
             * @see de.julielab.semedico.core.Facet.FacetSource#isHierarchical()
             */
            @Override
            public boolean isHierarchic() {
                return false;
            }

        };
        // facetRoots = Collections.emptyList();
        uniqueLabels = new HashSet<>();
        uniqueLabels.add(FacetLabels.Unique.KEYWORDS);
        labels = new HashSet<>();
        setSearchFieldNames(Collections.emptyList());
        setFilterFieldNames(Collections.emptyList());
    }

    /**
     * Only use for tests.
     *
     * @param id
     */
    public Facet(String id) {
        this.id = id;
        facetSource = null;
        // facetRoots = Collections.emptyList();
    }

    public Facet(String id, String name) {
        this.id = id;
        this.name = name;
        this.facetSource = null;
        this.searchFieldNames = Collections.emptyList();
        this.filterFieldNames = Collections.emptyList();
        this.uniqueLabels = Collections.emptySet();
    }

    public Facet(String id, String name, Collection<String> searchFieldNames, Collection<String> filterFieldName,
                 Set<FacetLabels.General> labels, Set<FacetLabels.Unique> uniqueLabels, int position, String cssId,
                 FacetSource facetSource, ITermService termService) {
        this.id = id;
        this.name = name;

        this.searchFieldNames = searchFieldNames;
        this.filterFieldNames = filterFieldName;
        this.labels = labels;
        this.position = position;
        this.cssId = cssId;
        this.facetSource = facetSource;
        this.uniqueLabels = uniqueLabels;
        this.termService = termService;
    }

    public Facet() {
    }

    public Facet(Facet template) {
        this.id = template.id;
        this.name = template.name;
        this.searchFieldNames = template.searchFieldNames;
        this.filterFieldNames = template.filterFieldNames;
        this.labels = template.labels;
        this.position = template.position;
        this.cssId = template.cssId;
        this.facetSource = template.facetSource;
        this.uniqueLabels = template.uniqueLabels;
        this.termService = template.termService;
        this.aggregationLabels = template.aggregationLabels;
        this.aggregationFields = template.aggregationFields;
        this.numRoots = template.numRoots;
    }

    /**
     * Only relevant during facet loading. Facets that are not active are discarded after loading.
     *
     * @return Whether this facet should be included in the system or not.
     */
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }

    ;

    public Set<FacetLabels.Unique> getUniqueLabels() {
        return uniqueLabels;
    }

    public Set<FacetLabels.General> getLabels() {
        return labels;
    }

    public void setLabels(Set<FacetLabels.General> labels) {
        this.labels = labels;
    }

    public String getName() {
        return name;
    }

    public String getCssId() {
        return cssId;
    }

    public String getId() {
        return id;
    }

    public Collection<String> getSearchFieldNames() {
        return searchFieldNames;
    }

    public void setSearchFieldNames(Collection<String> searchFieldNames) {
        this.searchFieldNames = searchFieldNames;
    }

    /**
     * @return the filterFieldName
     */
    public Collection<String> getFilterFieldNames() {
        return filterFieldNames;
    }

    /**
     * @param filterFieldNames the filterFieldName to set
     */
    public void setFilterFieldNames(Collection<String> filterFieldNames) {
        this.filterFieldNames = filterFieldNames;
    }

    @Override
    public String toString() {
        return "Facet [name=" + name
                + ", id="
                + id
                + ", uniqueLabels="
                + uniqueLabels
                + ", labels="
                + labels
                + ", facetSource="
                + facetSource
                + "]";
    }

    public int compareTo(Facet otherFacet) {
        return this.position - otherFacet.getPosition();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * @return the facetSource
     */
    public FacetSource getSource() {
        return facetSource;
    }

    public void setSource(FacetSource facetSource) {
        this.facetSource = facetSource;
    }

    /**
     * Returns the root terms of this facet. For most facets, the returned value will be all root terms in the database.
     * For facets with an extreme amount of roots, the result could be empty or considerably smaller than the number of
     * roots as stored in the database. If the result is empty, there are too many roots in the database and no roots
     * have yet been loaded individually. If the number of returned roots is not 0 but smaller than the amount of roots
     * in the database, the up to now individually loaded concept are returned.
     *
     * @return The roots of this facet, either all existing database roots or - for facets with large amount of roots -
     * those roots loaded until now.
     */
    @JsonIgnore
    public List<Concept> getFacetRoots() {
        if (isFlat())
            return Collections.emptyList();
        return termService.getFacetRoots(this);
    }

    /**
     * Assures that at least all facet roots whose IDs are among <tt>termIds</tt> are loaded for this facet and then
     * returns all loaded facet roots.
     *
     * @param termIds
     * @return
     */
    @JsonIgnore
    public List<Concept> getFacetRoots(List<String> termIds) throws ConceptLoadingException, ConceptCreationException {
        if (isFlat())
            return Collections.emptyList();
        if (allDBRootsLoaded())
            return getFacetRoots();
        Map<Facet, List<String>> requestedRootIds = new HashMap<>();
        requestedRootIds.put(this, termIds);
        termService.assureFacetRootsLoaded(requestedRootIds);
        return getFacetRoots();
    }

    @JsonIgnore
    public Collection<String> getFacetRootIds() {
        Set<String> termIds = new HashSet<>();
        for (Concept term : getFacetRoots()) {
            String termId = term.getId();
            termIds.add(termId);
        }
        return termIds;
    }

    @JsonIgnore
    public boolean isHierarchic() {
        return facetSource.isHierarchic();
    }

    @JsonIgnore
    public boolean isFlat() {
        return facetSource.isFlat();
    }

    @JsonIgnore
    public boolean isAnyAuthorFacet() {
        return uniqueLabels.contains(FacetLabels.Unique.AUTHORS) || uniqueLabels
                .contains(FacetLabels.Unique.FIRST_AUTHORS) || uniqueLabels.contains(FacetLabels.Unique.LAST_AUTHORS);
    }

    /**
     * Returns the exact author-related label for this facet, if this is any author facet. Returns <tt>null</tt>
     * otherwise.
     *
     * @return
     */
    @JsonIgnore
    public FacetLabels.Unique getAuthorLabel() {
        for (FacetLabels.Unique label : uniqueLabels) {
            if (label.equals(FacetLabels.Unique.AUTHORS) || uniqueLabels.contains(FacetLabels.Unique.FIRST_AUTHORS)
                    || uniqueLabels.contains(FacetLabels.Unique.LAST_AUTHORS))
                return label;
        }
        return null;
    }

    @JsonIgnore
    public boolean hasUniqueLabel(FacetLabels.Unique label) {
        return uniqueLabels.contains(label);
    }

    @JsonIgnore
    public boolean hasGeneralLabel(FacetLabels.General label) {
        return labels.contains(label);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object arg0) {
        if (!(arg0 instanceof Facet))
            return false;
        Facet otherFacet = (Facet) arg0;
        return this.id.equals(otherFacet.id);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @JsonIgnore
    public UIFacet getUiFacetCopy(Logger logger) {
        UIFacet facet = new UIFacet(logger, this);
        return facet;
    }

    /**
     * The number of root terms this facet has. This value is precomputed within the term database and then just loaded
     * from there. Only hierarchical facets have roots at all, so for flat facets this is <tt>null</tt>.
     */
    public int getNumRootsInDB() {
        return numRoots;
    }

    @JsonIgnore
    public int getNumRootsLoaded() {
        return termService.getNumLoadedRoots(id);
    }

    @JsonProperty(SemedicoFacetConstants.PROP_NUM_ROOT_TERMS)
    public void setNumRoots(Integer numRoots) {
        if (null == numRoots)
            this.numRoots = 0;
        else
            this.numRoots = numRoots;
    }

    public Set<de.julielab.semedico.commons.concepts.FacetLabels.General> getAggregationLabels() {
        return this.aggregationLabels;
    }

    public void setAggregationLabels(Set<de.julielab.semedico.commons.concepts.FacetLabels.General> aggregationLabels) {
        this.aggregationLabels = aggregationLabels;
    }

    /**
     * An "Aggregation Facet" is a facet whose terms are actually a union of terms from other facets
     * @return If this is an aggregation facet or not, <tt>true</tt> or <tt>false</tt>.
     */
    @JsonIgnore
    public boolean isAggregationFacet() {
        return facetSource.isAggregation();
    }

    public List<String> getAggregationFields() {
        return aggregationFields;
    }

    public void setAggregationFields(List<String> facetAggregationFields) {
        this.aggregationFields = facetAggregationFields;
    }

    public boolean allDBRootsLoaded() {
        return getNumRootsLoaded() - getNumRootsInDB() >= 0;
    }

    public String getInducingTermId() {
        return inducingTermId;
    }

    public void setInducingTermId(String inducingTermId) {
        this.inducingTermId = inducingTermId;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public static class SourceDeserializer extends StdDeserializer<FacetSource> {
        private String name;
        private FacetSource.SourceType type;

        public SourceDeserializer() {
            super((Class<?>) null);
        }


        @Override
        public FacetSource deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String currentName = p.getCurrentName();
            String currentValue = p.getValueAsString();
            if (currentName.equals(SemedicoFacetConstants.PROP_SOURCE_NAME))
                name = currentValue;
            if (currentName.equals(SemedicoFacetConstants.PROP_SOURCE_TYPE)) {
                FacetSource.SourceType sourceType;
                switch (currentValue) {
                    case SemedicoFacetConstants.SRC_TYPE_FLAT:
                        sourceType = FacetSource.SourceType.FIELD_FLAT_TERMS;
                        break;
                    case SemedicoFacetConstants.SRC_TYPE_HIERARCHICAL:
                        sourceType = FacetSource.SourceType.FIELD_TAXONOMIC_TERMS;

                        break;
                    case SemedicoFacetConstants.SRC_TYPE_STRINGS:
                        sourceType = FacetSource.SourceType.FIELD_STRINGS;
                        break;
                    case SemedicoFacetConstants.SRC_TYPE_FACET_AGGREGATION:
                        sourceType = FacetSource.SourceType.FACET_AGGREGATION;
//                        if (facetAggregationLabels.isEmpty() && facetAggregationFields.isEmpty())
//                            throw new IllegalStateException("Facet with ID " + id + " has facetSource type " + sourceTypeString
//                                    + " but does not define labels to identify facets to be part of the aggregation or aggregation fields.");
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown facet facet source type \"" + currentValue + "\".");
                }
                type = sourceType;
            }


            return new FacetSource(type, name);
        }
    }

}
