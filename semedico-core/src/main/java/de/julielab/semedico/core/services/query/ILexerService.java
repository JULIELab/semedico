package de.julielab.semedico.core.services.query;

import java.io.IOException;
import java.util.List;

import de.julielab.semedico.core.search.query.QueryToken;

public interface ILexerService {

	/**
	 * Tokenize a query String using a simple lexer.
	 * 
	 * @param query
	 * 			The query String.
	 * @return A list of QueryTokens for the query.
	 * @throws IOException
	 */
	public List<QueryToken> lex(String query) throws IOException;	
	
	public List<QueryToken> filterStopTokens(List<QueryToken> queryTokens);
}
