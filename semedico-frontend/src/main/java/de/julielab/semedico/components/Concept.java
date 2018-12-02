package de.julielab.semedico.components;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;

public class Concept {

	@Parameter(required = true)
	@Property
	private IConcept concept;

	@Parameter(required=true)
	@Property
	private Facet facet;

	@Parameter(value="false")
	@Property
	private boolean showPath;
	
	@Parameter(value="-1")
	@Property
	private int conceptNodeId;
	
	@InjectComponent
	private Term term;
	
	public Object getConceptType() {
		switch(concept.getConceptType()) {
		case TERM:
		case AGGREGATE_CONCEPT:
		case KEYWORD:
			return term;
		case EVENT:
			break;
		default:
			break;
		
		}
		return null;
	}
}
