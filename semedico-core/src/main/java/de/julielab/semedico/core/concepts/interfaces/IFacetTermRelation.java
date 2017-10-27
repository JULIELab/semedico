package de.julielab.semedico.core.concepts.interfaces;

import de.julielab.semedico.core.TermRelationKey;
import de.julielab.semedico.core.concepts.Concept;

public interface IFacetTermRelation extends LatchSynchronized {

	public enum Type {
		IS_BROADER_THAN, HAS_ROOT_TERM, HAS_ELEMENT
	};

	public TermRelationKey getKey();

	public Concept getStartNode();

	public Concept getEndNode();

	public String getStartTermId();

	public String getEndTermId();

	public String getType();
}
