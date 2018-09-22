package de.julielab.semedico.core.concepts;

import de.julielab.semedico.core.services.ConceptNeo4jService;

/**
 * Concept classes implementing this interface may be initialized by an instance of {@link ConceptDescription}.
 * This is mostly used for concept loaded from the concept database. They are first represented by the
 * description as returned by the {@link de.julielab.semedico.core.services.interfaces.IConceptDatabaseService} and
 * then realized as {@link de.julielab.semedico.core.concepts.interfaces.IHierarchicalConcept} instances via the
 * {@link ConceptCreator} employed within the {@link ConceptNeo4jService}. This is
 * the typical usage but other usages might be seen or introduced as well. Just note that a <tt>ConceptDescription</tt>
 * is not always able to deliver all required information for a concept to enable its full functionality.
 * The {@link DatabaseConcept} class internally uses the <tt>ConceptNeo4jService</tt> which has to be set after
 * initialization from the description.
 */
public interface DescribableConcept {
    void initializeFromDescription(ConceptDescription description);
}
