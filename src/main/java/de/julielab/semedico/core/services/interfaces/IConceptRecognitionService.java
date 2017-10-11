package de.julielab.semedico.core.services.interfaces;

import java.io.IOException;
import java.util.List;

import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.services.ReconfigurablesService;

public interface IConceptRecognitionService extends ReconfigurablesService {

	/**
	 * Recognize terms by combining adjunct text tokens and matching longest
	 * dictionary entries. Will keep multiple terms for ambigue parts of the
	 * query String.
	 * 
	 * @param tokens
	 *            A list of QueryTokens for the query as retrieved from the
	 *            lexer.
	 * @param searchStateId
	 * @return A list of modified QueryTokens for the query. Adjunct text tokens
	 *         may have been combined and matched to (multiple) terms.
	 */
	public List<QueryToken> recognizeTerms(List<QueryToken> tokens, long searchStateId) throws IOException;
}
