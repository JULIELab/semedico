package de.julielab.semedico.components;

import java.util.Collection;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;

import de.julielab.semedico.core.FacetTerm;

public class DisambiguationPanel {


	@Property
	@Parameter
	private Collection<FacetTerm> mappedTerms;

	@Property
	@Parameter
	private String queryTerm;

	@Property
	@Parameter	
	private FacetTerm selectedTerm;
	
}
