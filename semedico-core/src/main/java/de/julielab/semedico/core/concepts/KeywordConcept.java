package de.julielab.semedico.core.concepts;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;

/**
 * A very simple, non-hierarchical concept represented by a specific keyword. The semantics of this class is the
 * string match without trying to resolve the keyword as a knowledge base concept.
 */
public class KeywordConcept implements IConcept {

    private String id;
    private String preferredName;
    public KeywordConcept() {
    }

    public KeywordConcept(String id, String preferredName) {
        this.id = id;
        this.preferredName = preferredName;
    }

    @Override
    public String toString() {
        return "KeywordConcept [id=" + id + ", preferredName=" + preferredName + "]";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getPreferredName() {
        return preferredName;
    }

    @Override
    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    @Override
    public void addFacet(Facet facet) {
        throw new IllegalAccessError("Keyword terms cannot be added a facet.");
    }

    @Override
    public Facet getFirstFacet() {
        return Facet.KEYWORD_FACET;
    }

    @Override
    public List<String> getSynonyms() {
        return null;
    }

    @Override
    public List<String> getDescriptions() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void setDescription(List<String> description) {
        throw new IllegalAccessError("Keyword terms cannot be set a description.");
    }

    @Override
    public List<Facet> getFacets() {
        return Lists.newArrayList(Facet.KEYWORD_FACET);
    }

    @Override
    public void setFacets(List<Facet> facets) {
        throw new IllegalAccessError("Keyword terms cannot be set facets.");
    }

    @Override
    public boolean isContainedInFacet(Facet facet) {
        return facet.equals(Facet.KEYWORD_FACET);
    }

    @Override
    public List<String> getOccurrences() {
        return null;
    }

    @Override
    public boolean isNonDatabaseConcept() {
        return true;
    }

    @Override
    public void setNonDatabaseConcept(boolean isNonDatabaseTerm) {
        throw new IllegalAccessError("Keyword terms cannot be set to be a database term.");
    }

    @Override
    public String getDisplayName() {
        return preferredName;
    }

    @Override
    public String[] getQualifiers() {
        return null;
    }

    @Override
    public ConceptType getConceptType() {
        return ConceptType.KEYWORD;
    }

    @Override
    public boolean isCoreTerm() {
        return false;
    }

}
