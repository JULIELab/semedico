package de.julielab.semedico.core.services.interfaces;

import java.io.IOException;
import java.util.List;

import de.julielab.semedico.core.query.QueryToken;

@FunctionalInterface
public interface ITermRecognitionService {

	/**
	 * Recognize terms by combining adjunct text tokens and matching longest
	 * dictionary entries. Will keep multiple terms for ambiguous parts of the
	 * query String.
	 * 
	 * @param tokens
	 *            A list of QueryTokens for the query as retrieved from the
	 *            lexer.
	 * @return A list of modified QueryTokens for the query. Adjunct text tokens
	 *         may have been combined and matched to (multiple) terms.
	 */
	public List<QueryToken> recognizeTerms(List<QueryToken> tokens) throws IOException;
}
