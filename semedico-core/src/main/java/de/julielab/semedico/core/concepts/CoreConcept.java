package de.julielab.semedico.core.concepts;

import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.util.SemedicoRuntimeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// TODO perhaps this would better implemnt IConcept directly? There is no hierarchy here.
public class CoreConcept implements IConcept {


    private String preferredName;
    private String id;
    private List<String> synonyms = Collections.emptyList();
    private List<String> writingVariants = Collections.emptyList();
    private List<String> descriptions = Collections.emptyList();
    private CoreConceptType coreConceptType;

    public CoreConcept(String id, String preferredName) {
        this.id = id;
        this.preferredName = preferredName;
    }

    public List<String> getWritingVariants() {

        return writingVariants;
    }

    public void setWritingVariants(List<String> writingVariants) {
        this.writingVariants = writingVariants;
    }

    @Override
    public ConceptType getConceptType() {
        return ConceptType.CORE;
    }

    @Override
    public void addFacet(Facet facet) {
        throw new SemedicoRuntimeException("Not implemented for CoreConcepts.");
    }

    @Override
    public Facet getFirstFacet() {
        return Facet.CORE_TERMS_FACET;
    }

    @Override
    public List<Facet> getFacets() {
        return Arrays.asList(getFirstFacet());
    }

    @Override
    public void setFacets(List<Facet> facets) {
        throw new SemedicoRuntimeException("Not implemented for CoreConcepts.");
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
    public boolean isContainedInFacet(Facet otherFacet) {
        return otherFacet == Facet.CORE_TERMS_FACET;
    }

    @Override
    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    @Override
    public List<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }

    @Override
    public List<String>  getOccurrences() {
        List<String> occurrences = new ArrayList<>(1 + synonyms.size() + writingVariants.size());
        occurrences.add(preferredName);
        occurrences.addAll(synonyms);
        occurrences.addAll(writingVariants);
        return occurrences;
    }

    @Override
    public String getDescription() {
        return descriptions.isEmpty() ? null : descriptions.get(0);
    }

    @Override
    public void setDescription(List<String> description) {
        this.descriptions = description;
    }

    @Override
    public boolean isNonDatabaseConcept() {
        return true;
    }

    @Override
    public void setNonDatabaseConcept(boolean isNonDatabaseTerm) {
        throw new SemedicoRuntimeException("Not implemented for CoreConcepts.");
    }

    @Override
    public String getDisplayName() {
        return preferredName;
    }

    @Override
    public String[] getQualifiers() {
        return new String[0];
    }

    public CoreConceptType getCoreConceptType() {
        return coreConceptType;
    }

    public void setCoreConceptType(CoreConceptType coreConceptType) {
        this.coreConceptType = coreConceptType;
    }

    @Override
    public boolean isCoreTerm() {
        return true;
    }

    public enum CoreConceptType {
        /**
         * The core concept of this type is the 'any' wildcard. It does not only match concepts but any index term.
         */
        ANY_TERM
    }

}
