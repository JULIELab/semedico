package de.julielab.semedico.components;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.parsing.TextNode;

public class TermQueryUnit {
	@Parameter(required=true)
	private TextNode parseNode;
	
	@Parameter
	@Property
	private boolean showPath;
	
	@Parameter
	@Property
	private int conceptNodeId;
	
	public IConcept getTerm() {
		return parseNode.getConcepts().get(0);
	}
	
	public Facet getMappedFacet() {
		return parseNode.getMappedFacet(getTerm());
	}
}
