package de.julielab.semedico.components;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facetterms.Event;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.parsing.TextNode;

public class EventComponent {
	@Parameter(required=true)
	@Property
	private Event event;

	@Parameter(required=true)
	@Property
	private Facet facet;
	
	@Property
	private Node eventArgItem;
	
	public IConcept getFirstTermOfEventArgument() {
		if (eventArgItem.isConceptNode()) {
			// We currently do not support nested events, so if this node is a concept node it has to be a TextNode.
			TextNode textNode = (TextNode) eventArgItem;
			return textNode.getConcepts().get(0);
		}
		return null;
	}
	
	public boolean eventHasSecondArgument() {
		return event.getArguments().size() > 1;
	}
	
}
