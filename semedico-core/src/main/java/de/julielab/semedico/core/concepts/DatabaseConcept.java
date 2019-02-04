package de.julielab.semedico.core.concepts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.julielab.semedico.core.concepts.interfaces.IConceptRelation;
import de.julielab.semedico.core.concepts.interfaces.IHierarchicalConcept;
import de.julielab.semedico.core.services.interfaces.IConceptService;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatabaseConcept extends Concept implements IHierarchicalConcept {
    protected IConceptService conceptService;
    private List<String> sourceIds;
    private List<String> sources;
    private String originalId;
    private String originalSource;

    public DatabaseConcept() {
        super();
        this.sourceIds = Collections.emptyList();
    }

    public DatabaseConcept(String stringTermId, String conceptName) {
        super(stringTermId, conceptName);
    }

    /**
     * For unit tests.
     *
     * @param id
     * @param conceptService
     */
    public DatabaseConcept(String id, IConceptService conceptService) {
        this.id = id;
        this.conceptService = conceptService;
    }
    /**
     * For unit tests.
     *
     * @param id
     */
    public DatabaseConcept(String id) {
        this.id = id;
    }

    public DatabaseConcept(ConceptDescription description) {
        initializeFromDescription(description);
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public String getOriginalSource() {
        return originalSource;
    }

    public void setOriginalSource(String originalSource) {
        this.originalSource = originalSource;
    }

    @Override
    public void initializeFromDescription(ConceptDescription description) {
        super.initializeFromDescription(description);
        if (description.getSourceIds() != null)
            this.sourceIds = Arrays.asList(description.getSourceIds());
        this.originalId = description.getOriginalId();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof DatabaseConcept))
            return false;
        DatabaseConcept otherTerm = (DatabaseConcept) otherObject;
        return this.id.equals(otherTerm.id);
    }

    @Override
    public String getOriginalId() {
        return originalId;
    }

    @Override
    public void setOriginalId(String originalId) {
        this.originalId = originalId;

    }

    @Override
    public List<String> getSourceIds() {
        return sourceIds;
    }

    @Override
    public void setSourceIds(List<String> sourceIds) {
        this.sourceIds = sourceIds;

    }

    @Override
    public String toString() {
        return "FacetTerm [id=" + id + ", preferredName=" + preferredName + "]";
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public ConceptType getConceptType() {
        return ConceptType.TERM;
    }

    @Override
    public boolean isCoreTerm() {
        return false;
    }


    protected void loadChildren() {
        if ((getConceptType() == ConceptType.AGGREGATE_CONCEPT || childrenInFacets.size() > 0) && outgoingRelationships.size() == 0
                && !childrenHaveBeenLoaded) {
            childrenHaveBeenLoaded = true;
            conceptService.loadChildrenOfTerm(this);
        }
    }

    public Concept getChild(int i) {
        loadChildren();
        return super.getChild(i);
    }

    protected Map<String, List<IConceptRelation>> outgoingRelationships() {
        loadChildren();
        return super.outgoingRelationships();
    }

    public void setConceptService(IConceptService conceptService) {
        this.conceptService = conceptService;
    }

}
