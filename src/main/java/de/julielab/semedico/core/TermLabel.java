package de.julielab.semedico.core;

import java.util.HashSet;
import java.util.Set;

import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;


/**
 * 
 * @author faessler
 * 
 */
public class TermLabel extends Label {

	private IFacetTerm term;

	private Set<Facet> hasChildHits;

	public TermLabel(IFacetTerm term) {
		super(term.getName(), term.getId());
		this.term = term;
		this.hasChildHits = new HashSet<Facet>();
	}

	public IFacetTerm getTerm() {
		return term;
	}

	@Override
	public boolean hasChildHitsInFacet(Facet facet) {
		return hasChildHits.contains(facet);
	}

	public void setHasChildHitsInFacet(Facet facet) {
		hasChildHits.add(facet);
	}
	
	public void reset() {
		super.reset();
		hasChildHits.clear();
	}
	
}
