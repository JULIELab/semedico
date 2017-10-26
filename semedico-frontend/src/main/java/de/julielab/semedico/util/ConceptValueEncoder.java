package de.julielab.semedico.util;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.services.interfaces.ITermService;

public class ConceptValueEncoder implements ValueEncoder<IConcept>, ValueEncoderFactory<IConcept> {

	private ITermService termService;

	public ConceptValueEncoder(ITermService termService) {
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
