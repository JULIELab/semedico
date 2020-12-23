package de.julielab.semedico.core.concepts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.julielab.neo4j.plugins.datarepresentation.constants.ConceptConstants;

@JsonIgnoreProperties(value={ConceptConstants.PROP_UNIQUE_SRC_ID, ConceptConstants.AGGREGATE_INCLUDE_IN_HIERARCHY})
public class ConceptDescription {
    @JsonProperty(ConceptConstants.PROP_ID)
    private String id;
    @JsonProperty(ConceptConstants.PROP_PREF_NAME)
    private String preferredName;
    @JsonProperty(ConceptConstants.PROP_DISPLAY_NAME)
    private String displayName;
    @JsonProperty(ConceptConstants.PROP_DESCRIPTIONS)
    private String[] descriptions;
    @JsonProperty(ConceptConstants.PROP_SYNONYMS)
    private String[] synonyms;
    @JsonProperty(ConceptConstants.PROP_WRITING_VARIANTS)
    private String[] writingVariants;
    @JsonProperty(ConceptConstants.PROP_FACETS)
    private String[] facetIds;
    @JsonProperty(ConceptConstants.PROP_ORG_ID)
    private String originalId;
    @JsonProperty(ConceptConstants.PROP_ORG_SRC)
    private String originalSource;
    @JsonProperty(ConceptConstants.PROP_SRC_IDS)
    private String[] sourceIds;
    @JsonProperty(ConceptConstants.PROP_SOURCES)
    private String[] sources;
    @JsonProperty(ConceptConstants.PROP_CHILDREN_IN_FACETS)
    private String[] childrenInFacets;
    @JsonProperty(ConceptConstants.PROP_LABELS)
    private String[] labels;
    @JsonProperty(ConceptConstants.AGGREGATE)
    private boolean aggregate;

    public boolean isAggregate() {
        return aggregate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String[] getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(String[] descriptions) {
        this.descriptions = descriptions;
    }

    public String[] getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String[] synonyms) {
        this.synonyms = synonyms;
    }

    public String[] getWritingVariants() {
        return writingVariants;
    }

    public void setWritingVariants(String[] writingVariants) {
        this.writingVariants = writingVariants;
    }

    public String[] getFacetIds() {
        return facetIds;
    }

    public void setFacetIds(String[] facetIds) {
        this.facetIds = facetIds;
    }

    public String getOriginalId() {
        return originalId;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    public String getOriginalSource() {
        return originalSource;
    }

    public void setOriginalSource(String originalSource) {
        this.originalSource = originalSource;
    }

    public String[] getSourceIds() {
        return sourceIds;
    }

    public void setSourceIds(String[] sourceIds) {
        this.sourceIds = sourceIds;
    }

    public String[] getSources() {
        return sources;
    }

    public void setSources(String[] sources) {
        this.sources = sources;
    }

    public String[] getChildrenInFacets() {
        return childrenInFacets;
    }

    public void setChildrenInFacets(String[] childrenInFacets) {
        this.childrenInFacets = childrenInFacets;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    @Override
    public String toString() {
        return "ConceptDescription{" +
                "id='" + id + '\'' +
                ", preferredName='" + preferredName + '\'' +
                '}';
    }
}
