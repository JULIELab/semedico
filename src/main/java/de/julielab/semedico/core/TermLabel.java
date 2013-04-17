package de.julielab.semedico.core;

import java.util.HashSet;
import java.util.Set;

import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;

/**
 * 
 * @author faessler
 * 
 */
public class TermLabel extends Label {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1478645955280470324L;

	transient private IFacetTerm term;

	transient private Set<Facet> hasChildHits;

	public TermLabel(IFacetTerm term) {
		super(term.getName(), term.getId());
		this.term = term;
		this.hasChildHits = new HashSet<Facet>();
	}

	public IFacetTerm getTerm() {
		return term;
	}

	/**
	 * Gets the term belonging to this <tt>TermLabel</tt> instance from
	 * <tt>termService</tt> and sets it back to this label's <tt>term</tt>
	 * field. This is required after deserialization since the term objects are
	 * not serialized with the label.
	 * 
	 * @param termService
	 */
	public void recoverFromSerialization(ITermService termService) {
		IFacetTerm term = termService.getNode(id);
		this.term = term;
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
