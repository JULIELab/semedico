package de.julielab.semedico.core.entities;

import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.interfaces.IConceptRelation;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.util.LatchSynchronizer;

public class ConceptRelation extends LatchSynchronizer implements
        IConceptRelation {

	private ConceptRelationKey key;
	private ITermService conceptService;

	public ConceptRelation(ConceptRelationKey key, ITermService conceptService) {
		this.key = key;
		this.conceptService = conceptService;

	}

	@Override
	public ConceptRelationKey getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConceptRelation other = (ConceptRelation) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public Concept getStartNode() {
		return (Concept) conceptService.getTerm(key.getStartId());
	}

	@Override
	public Concept getEndNode() {
		return (Concept) conceptService.getTerm(key.getEndId());
	}
	
	@Override
	public String getType() {
		return key.getRelationType();
	}

	@Override
	public String getStartTermId() {
		return key.getStartId();
	}

	@Override
	public String getEndTermId() {
		return key.getEndId();
	}

}
