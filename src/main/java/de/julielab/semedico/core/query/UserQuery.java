package de.julielab.semedico.core.query;

import java.util.ArrayList;
import java.util.List;

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
		QueryToken freetextQuery = new QueryToken(0, userQuery.length());
		tokens = new ArrayList<>();
		tokens.add(freetextQuery);
	}

}
