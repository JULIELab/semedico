package de.julielab.semedico.core.search.components.data;

import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.IConceptService;

import java.util.HashSet;
import java.util.Set;

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

	transient private Concept term;

	transient private Set<Facet> hasChildHits;

	public TermLabel(Concept term) {
		this.term = term;
		this.hasChildHits = new HashSet<Facet>();
	}

	public Concept getTerm() {
		return term;
	}
	
	

	@Override
	public String getName() {
//		return term.getDisplayName() != null ? term.getDisplayName() : term.getPreferredName();
		return term.getPreferredName();
	}

	@Override
	public String getId() {
		return term.getId();
	}

	/**
	 * Gets the term belonging to this <tt>TermLabel</tt> instance from
	 * <tt>conceptService</tt> and sets it back to this label's <tt>term</tt>
	 * field. This is required after deserialization since the term objects are
	 * not serialized with the label.
	 * 
	 * @param termService
	 */
	public void recoverFromSerialization(IConceptService termService) {
		Concept term = (Concept) termService.getTerm(getId());
		this.term = term;
	}

	@Deprecated
	@Override
	public boolean hasChildHitsInFacet(Facet facet) {
		return hasChildHits.contains(facet);
	}

	@Deprecated
	public void setHasChildHitsInFacet(Facet facet) {
		hasChildHits.add(facet);
	}

	public void reset() {
		super.reset();
		hasChildHits.clear();
	}

	@Override
	public boolean isTermLabel() {
		return true;
	}

	@Override
	public boolean isStringLabel() {
		return false;
	}

	@Override
	public boolean isMessageLabel() {
		return false;
	}

}
