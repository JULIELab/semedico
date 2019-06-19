package de.julielab.semedico.core.services.query;

import de.julielab.semedico.core.search.query.QueryToken;

import java.util.List;

public interface ILexerService {
	/**
	 * Tokenize a query String using a simple lexer.
	 * 
	 * @param query
	 * 			The query String.
	 * @return A list of QueryTokens for the query.
	 */
	 List<QueryToken> lex(String query);
	
}
