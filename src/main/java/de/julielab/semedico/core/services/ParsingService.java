package de.julielab.semedico.core.services;

import org.apache.tapestry5.ioc.annotations.Inject;

import de.julielab.parsing.ParseTree;
import de.julielab.parsing.Parser;
import de.julielab.semedico.query.IQueryDisambiguationService;


/**
 * Used to make Parser/Lexer compatible with dependency injection
 * @author hellrich
 *
 */
public class ParsingService implements IParsingService{
	@Inject
	private IQueryDisambiguationService queryDisambiguationService;
	
	public ParseTree parse(String query) throws Exception{
		/**
		 * Parses a query and returns its parse tree. Tokens will be combined if possible.
		 * @param query
		 * 		gets parsed
		 * @return
		 * 		a parse tree
		 * @throws Exception
		 */
		return new Parser(query, true, queryDisambiguationService).parse();
	}
}
