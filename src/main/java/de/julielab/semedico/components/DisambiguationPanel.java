package de.julielab.semedico.components;

import java.util.Collection;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;

import de.julielab.stemnet.core.Term;

public class DisambiguationPanel {


	@Property
	@Parameter
	private Collection<Term> mappedTerms;

	@Property
	@Parameter
	private String queryTerm;

	@Property
	@Parameter	
	private Term selectedTerm;
	
}
