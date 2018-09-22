package de.julielab.semedico.core.concepts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.julielab.semedico.core.concepts.interfaces.IConceptRelation;
import de.julielab.semedico.core.concepts.interfaces.IHierarchicalConcept;
import de.julielab.semedico.core.services.interfaces.ITermService;

import java.util.*;

import static de.julielab.semedico.core.concepts.interfaces.IConceptRelation.Type.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatabaseConcept extends Concept implements IHierarchicalConcept {
    protected ITermService conceptService;
    private List<String> sourceIds;
    private String originalId;

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
    public DatabaseConcept(String id, ITermService conceptService) {
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

    @Override
    public void initializeFromDescription(ConceptDescription description) {
        super.initializeFromDescription(description);
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
    public boolean isKeyword() {
        // boolean isKeyword = true;
        // for (Facet facet : facets) {
        // if (facet != Facet.KEYWORD_FACET)
        // isKeyword = false;
        // }
        // return isKeyword;
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean isAggregate() {
        return false;
    }

    @Override
    public ConceptType getConceptType() {
        return ConceptType.TERM;
    }

    @Override
    public boolean isCoreTerm() {
        return false;
    }

    /**
     * A database term is currently event functional if it is an event trigger.
     */
    @Override
    public boolean isEventFunctional() {
        return isEventTrigger();
    }

    protected void loadChildren() {
        if ((isAggregate() || childrenInFacets.size() > 0) && outgoingRelationships.size() == 0
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

    public void setConceptService(ITermService conceptService) {
        this.conceptService = conceptService;
    }

}
