package de.julielab.semedico.core.services.interfaces;

import java.util.List;

import org.apache.tapestry5.json.JSONArray;

import de.julielab.semedico.core.query.QueryToken;

public interface ITokenInputService
{
	/**
	 * These are the possible types of tokens in the token input field. They are
	 * read directly from the tokens in the search field when running a Semedico
	 * query. FREETEXT is special in the way that consecutive FREETEXT tokens
	 * undergo a concept recognition process in the search process. For all
	 * other token input types, their meaning is fixed by their type.
	 * 
	 * @author faessler/kampe
	 *
	 */
	public enum TokenType
	{
		FREETEXT, KEYWORD, CONCEPT, AMBIGUOUS_CONCEPT, AND, OR, NOT, LEFT_PARENTHESIS, RIGHT_PARENTHESIS,
		/**
		 * Refer to the {@link ITokenInputService#LEXER_TYPE} property.
		 */
		LEXER
	}

	public static final String TERM_ID = "termid";
	public static final String FACET_ID = "facetid";
	public static final String FACET_NAME = "facetname";
	public static final String NAME = "name";
	public static final String PREFERRED_NAME = "preferredname";
	public static final String SYNONYMS = "synonyms";
	public static final String TOKEN_TYPE = "tokentype";
	public static final String LEXER_TYPE = "lexertype";
	public static final String USER_SELECTED = "userselected";
	public static final String BEGIN = "tokenBegin";
	public static final String QUERY = "query";
	public static final String PRIORITY = "priority";

	List<QueryToken> convertToQueryTokens(JSONArray tokenInput);
}
