package de.julielab.semedico.core;

import de.julielab.semedico.core.Taxonomy.IFacetTerm;


/**
 * 
 * @author faessler
 * 
 */
public class TermLabel extends Label {

	private IFacetTerm term;

	private boolean hasChildHits;

	public TermLabel(IFacetTerm term) {
		super(term.getName(), term.getId());
		this.term = term;
	}

	public IFacetTerm getTerm() {
		return term;
	}

	@Override
	public boolean hasChildHits() {
		return hasChildHits;
	}

	public void setHasChildHits() {
		this.hasChildHits = true;
	}
	
	public void clear() {
		super.clear();
		hasChildHits = false;
		term = null;
	}
	
}
