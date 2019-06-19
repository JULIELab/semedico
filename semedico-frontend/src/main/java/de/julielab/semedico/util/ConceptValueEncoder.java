package de.julielab.semedico.util;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;

public class ConceptValueEncoder implements ValueEncoder<IConcept>, ValueEncoderFactory<IConcept> {

	private IConceptService termService;

	public ConceptValueEncoder(IConceptService termService) {
		this.termService = termService;
	}
	
	@Override
	public ValueEncoder<IConcept> create(Class<IConcept> type) {
		return this;
	}

	@Override
	public String toClient(IConcept value) {
		return value.getId();
	}

	@Override
	public IConcept toValue(String clientValue) {
		return termService.getTerm(clientValue);
	}

}
