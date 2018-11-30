package de.julielab.semedico.core.concepts.interfaces;

import de.julielab.semedico.core.entities.ConceptRelationKey;
import de.julielab.semedico.core.concepts.Concept;

public interface IConceptRelation extends LatchSynchronized {

    ConceptRelationKey getKey();

    Concept getStartNode();

    Concept getEndNode();

    String getStartTermId();

    String getEndTermId();

    String getType();

    enum Type {
        IS_BROADER_THAN, HAS_ROOT_CONCEPT, HAS_ELEMENT
    }
}
