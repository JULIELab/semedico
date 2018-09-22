package de.julielab.semedico.components;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetLabels;
import de.julielab.semedico.core.parsing.EventNode;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.services.interfaces.IFacetService;

public class EventQueryUnit {
	@Parameter(required = true)
	@Property
	private ParseTree semedicoQuery;
	
	@Parameter(required = true)
	@Property
	private long conceptNodeId;
	
	@Property
	private Node eventArgItem;
	
	@Inject
	private IFacetService facetService;
	
	public Facet getMappedTermFacet() {
		EventNode eventNode = (EventNode) semedicoQuery.getNode(conceptNodeId);
		IConcept eventType = eventNode.getEventTypes().get(0);
		Facet inducedFacet = facetService.getInducedFacet(eventType.getId(), FacetLabels.General.EVENTS);
		if (null == inducedFacet)
			return eventType.getFirstFacet();
		return inducedFacet;
	}
	
	public boolean eventHasSecondArgument() {
		return getEventNode().getChildren().size() > 1;
	}
	
	public EventNode getEventNode() {
		// Should work because this component expects an event node
		return (EventNode) semedicoQuery.getNode(conceptNodeId);
	}
	
	public String getMappedTermClass() {
		Facet eventFacet = getMappedTermFacet();
		String cssId = eventFacet.getCssId();
		String termClass = cssId + " filterBox primaryFacetStyle";
		return termClass;
	}
	
	public IConcept getFirstTermOfEventArgument() {
		if (eventArgItem.isConceptNode()) {
			// We currently do not support nested events, so if this node is a concept node it has to be a TextNode.
			TextNode textNode = (TextNode) eventArgItem;
			return textNode.getConcepts().get(0);
		}
		return null;
	}
	
	public long getEventArgNodeId() {
		return eventArgItem.getId();
	}
	
	public IConcept getEventType() {
		// If there are multiple event types, we basically have some kind of ambiguity. That should be resolved by the ambiguous query unit component.
		return getEventNode().getEventTypes().get(0);
	}
	
}
