package de.julielab.semedico.services;

import java.util.ArrayList;
import java.util.List;

import de.julielab.semedico.core.services.interfaces.IConceptService;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.slf4j.Logger;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.query.QueryTokenizerImpl;

public class TokenInputService implements ITokenInputService {

	private Logger log;
	private IConceptService termService;
	private IFacetService facetService;

	public TokenInputService(Logger log, IConceptService termService, IFacetService facetService) {
		this.log = log;
		this.termService = termService;
		this.facetService = facetService;

	}

	@Override
	public List<QueryToken> convertToQueryTokens(JSONArray tokenInput) {
		List<QueryToken> tokens = new ArrayList<>();
		int offset = 0;
		for (int i = 0; i < tokenInput.length(); ++i) {
			JSONObject token = tokenInput.getJSONObject(i);
			// mandatory properties
			String name = token.getString(NAME);

			// the following properties have default values to deal with
			// incomplete / unknown information
			String facetId = Facet.KEYWORD_FACET.getId();
			String tokenId = null;
			Integer lexerType = QueryTokenizerImpl.ALPHANUM;
			TokenType tokenType = null;
			// set to 'true' by default because everything starts with the user
			// input; not-user-selected terms can only occur by intervention of
			// the system during query analysis of freetext tokens
			boolean userSelected = true;
			if (token.has(TOKEN_TYPE))
				tokenType = TokenType.valueOf(token.getString(TOKEN_TYPE));
			if (token.has(FACET_ID))
				facetId = token.getString(FACET_ID);
			if (token.has(TERM_ID)) {
				tokenId = token.getString(TERM_ID);
			}
			if (token.has(USER_SELECTED)) {
				userSelected = token.getBoolean(USER_SELECTED);
			}
			if (token.has(LEXER_TYPE))
				lexerType = token.getInt(LEXER_TYPE);
			if (tokenType == TokenType.FREETEXT) {
				tokenId = name;
				facetId = Facet.KEYWORD_FACET.getId();
			}
			if (tokenType == TokenType.KEYWORD) {
				facetId = Facet.KEYWORD_FACET.getId();
			}
			if (null != lexerType) {
				switch(lexerType) {
				case QueryTokenizerImpl.AND_ALPHANUM:
				case QueryTokenizerImpl.AND_OPERATOR:
					tokenType = TokenType.AND;
					break;
				case QueryTokenizerImpl.OR_ALPHANUM:
				case QueryTokenizerImpl.OR_OPERATOR:
					tokenType = TokenType.OR;
					break;
				case QueryTokenizerImpl.NOT_ALPHANUM:
				case QueryTokenizerImpl.NOT_OPERATOR:
					tokenType = TokenType.NOT;
					break;
				case QueryTokenizerImpl.LEFT_PARENTHESIS:
					tokenType = TokenType.LEFT_PARENTHESIS;
					break;
				case QueryTokenizerImpl.RIGHT_PARENTHESIS:
					tokenType = TokenType.RIGHT_PARENTHESIS;
					break;
				}
			}

			QueryToken qt = new QueryToken(offset, offset + name.length());
			// at this point we just have what the user gave us, so yes, even
			// the freetext part has been given this way by the user. This
			// property is used later, after parsing, in query translation.
			// Concept QueryTokens selected by the user don't need to be also
			// searched as keywords / phrases in parallel because we know that
			// the user explicitly selected the concept.
			qt.setUserSelected(userSelected);
			// "whitespace"
			++offset;
			qt.setOriginalValue(name);
			//qt.setFreetext(tokenType == TokenType.FREETEXT);
			qt.setType(lexerType);
			qt.setInputTokenType(tokenType);

			Facet facet = facetService.getFacetById(facetId);
			if (null == facet) {
				log.warn("Term with facet ID {} was suggested but this facet does not exist. "
						+ "The employed suggestion index and the term database are out of sync."
						+ " The query token is ignored. It was: {}", facetId, token);
				continue;
			}

			if (tokenType == TokenType.CONCEPT) {
				IConcept term = termService.getTerm(tokenId);
				if (null == term) {
					log.warn("Term with ID {} was suggested but this term does not exist. "
							+ "The employed suggestion index and the term database are out of sync."
							+ " The query token is ignored. It was: {}", tokenId, token);
					continue;
				}
				qt.addConceptToList(term);
				qt.setFacetMapping(term, facet);
			} else if (tokenType == TokenType.AMBIGUOUS_CONCEPT) {
				if (token.has("disambiguationOptions")) {
					JSONArray options = token.getJSONArray("disambiguationOptions");
					for (int j = 0; j < options.length(); ++j) {
						String termId = options.getString(j); 
						IConcept concept = termService.getTerm(termId);
						if (null == concept) {
							log.warn(
									"Concept with ID {} part of an ambiguous query token but this concept does not exist. ",
									tokenId, token);
							continue;
						}
						qt.addConceptToList(concept);
					}
				}
			}

			tokens.add(qt);
		}
		log.debug("Converted JSONArray of input terms {} to query tokens: {}.", tokenInput, tokens);
		return tokens;
	}

}
