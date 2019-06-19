package de.julielab.semedico.core.services.query;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.QueryToken;

import java.util.List;

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
	
}
