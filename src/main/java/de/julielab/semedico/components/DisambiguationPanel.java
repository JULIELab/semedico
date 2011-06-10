package de.julielab.semedico.components;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;

import de.julielab.semedico.core.FacetTerm;

public class DisambiguationPanel {


	@Property
	@Parameter
	private Collection<FacetTerm> mappedTerms;
	
	@Property
	@Parameter
	private MultiMap sortedTerms;	

	@Property
	@Parameter
	private String queryTerm;

	@Property
	@Parameter	
	private FacetTerm selectedTerm;
	
	@Property
	@Parameter	
	private int ambigueTermIndex;	
	
	@Property
	@Parameter	
	private FacetTerm facetItem;
	
	@Property
	@Parameter	
	private int facetIndex;	
	
	@Property
	private Object currentKey;
	
	public List<FacetTerm> getCurrentTermSet() {
	     return (List<FacetTerm>) sortedTerms.get(this.currentKey);
	}
	
}
