package de.julielab.semedico.core.query;

import java.util.ArrayList;
import java.util.List;

import de.julielab.semedico.core.services.query.QueryTokenizerImpl;

/**
 * This object holds all information we got from the user at the input text
 * field for search. It currently includes kind of deprecated information
 * because we want to switch to token input soon.
 * 
 * @author faessler
 * 
 */
public class UserQuery {
	@Deprecated
	public String freetextQuery;
	@Deprecated
	public String termId;
	@Deprecated
	public String facetId;
	@Deprecated
	public List<InputEventQuery> eventQueries;
	@Deprecated
	public String tokenString;
	public List<QueryToken> tokens;

	public UserQuery() {
	}

	/**
	 * Wraps <tt>userQuery</tt> into a single {@link QueryToken} with
	 * <tt>freetext</tt> set to true.
	 * 
	 * @param userQuery
	 */
	public UserQuery(String userQuery) {
		QueryToken freetextQuery = new QueryToken(0, userQuery.length());
		freetextQuery.setOriginalValue(userQuery);
		freetextQuery.setFreetext(true);
		tokens = new ArrayList<>();
		tokens.add(freetextQuery);
	}

	@Override
	public String toString() {
		return "UserQuery [queryString=" + freetextQuery + ", termId=" + termId + ", facetId="
				+ facetId + ", eventQueries=" + eventQueries + ", tokenString=" + tokenString + "]";
	}

	public List<QueryToken> getFreetextTokens() {
		List<QueryToken> freetextTokens = new ArrayList<>();
		for (QueryToken qt : tokens) {
			if (qt.isFreetext()) {
				freetextTokens.add(qt);
			}
		}
		return freetextTokens;
	}
}
