package de.julielab.semedico.core.search.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

/**
 * This object holds all information we got from the user at the input text
 * field for search. It currently includes kind of deprecated information
 * because we want to switch to token input soon.
 * 
 * @author faessler
 * 
 */
public class UserQuery {
	public List<QueryToken> tokens;

	public UserQuery() {
	}

	/**
	 * Wraps <tt>userQuery</tt> into a single {@link QueryToken} with
	 * <tt>freetext</tt> set to true.
	 * 
	 * @param userQuery the one-string query that should be searched for
	 */
	public UserQuery(String userQuery) {
		QueryToken freetextToken = new QueryToken(0, userQuery.length());
		freetextToken.setOriginalValue(userQuery);
		freetextToken.setInputTokenType(TokenType.FREETEXT);
		tokens = Arrays.asList(freetextToken);
	}

	public UserQuery(List<QueryToken> tokens) {
		this.tokens = tokens;
	}

}
