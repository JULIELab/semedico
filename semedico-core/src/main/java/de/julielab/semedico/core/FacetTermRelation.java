package de.julielab.semedico.core;

import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.interfaces.IFacetTermRelation;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.util.LatchSynchronizer;

public class FacetTermRelation extends LatchSynchronizer implements
		IFacetTermRelation {

	private TermRelationKey key;
	private ITermService termService;

	public FacetTermRelation(TermRelationKey key, ITermService termService) {
		this.key = key;
		this.termService = termService;

	}

	@Override
	public TermRelationKey getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FacetTermRelation other = (FacetTermRelation) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public Concept getStartNode() {
		return (Concept) termService.getTerm(key.getStartId());
	}

	@Override
	public Concept getEndNode() {
		return (Concept) termService.getTerm(key.getEndId());
	}
	
	@Override
	public String getType() {
		return key.getRelationType();
	}

	@Override
	public String getStartTermId() {
		return key.getStartId();
	}

	@Override
	public String getEndTermId() {
		return key.getEndId();
	}

}
