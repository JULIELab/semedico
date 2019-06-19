package de.julielab.semedico.components;

import de.julielab.semedico.core.parsing.ConceptNode;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.parsing.ParseTree;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.runtime.Component;

public class EventArgumentQueryUnit {
	@Parameter(required = true)
	@Property
	private ParseTree semedicoQuery;

	@Parameter(required = true)
	@Property
	private long conceptNodeId;

	@InjectComponent
	private Component termQueryUnit;

	@InjectComponent
	private Component ambiguousQueryUnit;
	
	public Node getParseNode() {
		return semedicoQuery.getNode(conceptNodeId);
	}

	public Object getNodeRenderComponent() {
		Node parseNode = getParseNode();
		// This cast should work because this is the CONCEPT query unit component and thus should only be used for
		// ConceptNodes.
		ConceptNode conceptNode = (ConceptNode) parseNode;
		if (conceptNode.isAmbiguous())
			return ambiguousQueryUnit;
		switch (parseNode.getNodeType()) {
		case CONCEPT:
			return termQueryUnit;
		case EVENT:
		case AND:
		case NOT:
		case OR:
		default:
			return null;
		}
	}
}
