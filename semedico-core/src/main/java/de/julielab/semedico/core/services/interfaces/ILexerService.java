package de.julielab.semedico.core.services.interfaces;

import java.io.IOException;
import java.util.List;

import de.julielab.semedico.core.query.QueryToken;

@FunctionalInterface
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
}
