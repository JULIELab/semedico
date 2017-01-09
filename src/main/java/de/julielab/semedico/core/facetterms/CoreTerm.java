package de.julielab.semedico.core.facetterms;

import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.ConceptType;

public class CoreTerm extends Concept {

	public enum CoreTermType {
		ANY_TERM, ANY_MOLECULAR_INTERACTION
	}

	private CoreTermType coreTermType;

	public CoreTerm(String id, String preferredName) {
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
