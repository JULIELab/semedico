package de.julielab.semedico.core.services;
import de.julielab.parsing.ParseTree;

/**
 * Used to make Parser/Lexer compatible with dependency injection
 * @author hellrich
 *
 */
public interface IParsingService {
	/**
	 * Parses a query and returns its parse tree. Tokens will be combined if possible.
	 * @param query
	 * 		gets parsed
	 * @return
	 * 		a parse tree
	 * @throws Exception
	 */
	public ParseTree parse(String query) throws Exception;
	

}
