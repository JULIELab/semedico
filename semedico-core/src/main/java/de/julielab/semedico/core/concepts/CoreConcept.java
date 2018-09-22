package de.julielab.semedico.core.concepts;

import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.ConceptType;

// TODO perhaps this would better implemnt IConcept directly? There is no hierarchy here.
public class CoreConcept extends Concept {

	public enum CoreTermType {
		ANY_TERM, ANY_MOLECULAR_INTERACTION
	}

	private CoreTermType coreTermType;

	public CoreConcept(String id, String preferredName) {
		super(id, preferredName);
	}

	@Override
	public ConceptType getConceptType() {
		return ConceptType.CORE;
	}

	@Override
	public boolean isKeyword() {
		return false;
	}

	@Override
	public boolean isAggregate() {
		return false;
	}

	public CoreTermType getCoreTermType() {
		return coreTermType;
	}

	public void setCoreTermType(CoreTermType coreTermType) {
		this.coreTermType = coreTermType;
	}

	@Override
	public boolean isCoreTerm() {
		return true;
	}

	@Override
	public boolean isEventFunctional() {
		switch (coreTermType) {
		case ANY_TERM:
		case ANY_MOLECULAR_INTERACTION:
			return true;
		default:
			return false;
		}
	}

}
