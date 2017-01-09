package de.julielab.semedico.core.services.interfaces;

import java.util.List;

import de.julielab.semedico.core.facetterms.Event;
import de.julielab.semedico.core.parsing.EventNode;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.QueryToken;

public interface IParsingService {

	/**
	 * Build a ParseTree from a list of QueryTokens.
	 * 
	 * @param tokens
	 * 			A list of QueryTokens.
	 * @return The ParseTree.
	 * @throws Exception
	 */
	public ParseTree parse(List<QueryToken> tokens) throws Exception;
	
	@Deprecated
	public EventNode createEventNode(Event eventTerm);
}
