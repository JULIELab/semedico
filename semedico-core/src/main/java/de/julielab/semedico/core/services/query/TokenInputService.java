package de.julielab.semedico.core.services.query;

import de.julielab.scicopia.core.parsing.QueryPriority;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

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
			QueryToken.Category lexerType = QueryToken.Category.ALPHANUM;
			String queryString = null;
			QueryPriority priority = QueryPriority.MUST;
			TokenType tokenType = null;
			// set to 'true' by default because everything starts with the user
			// input; not-user-selected terms can only occur by intervention of
			// the system during query analysis of freetext tokens
			boolean userSelected = true;
			if (token.has(TOKEN_TYPE)) {
				tokenType = TokenType.valueOf(token.getString(TOKEN_TYPE));
			}
			if (token.has(FACET_ID)) {
				facetId = token.getString(FACET_ID);
			}
			if (token.has(TERM_ID)) {
				tokenId = token.getString(TERM_ID);
			}
			if (token.has(USER_SELECTED)) {
				userSelected = token.getBoolean(USER_SELECTED);
			}
			if (token.has(LEXER_TYPE)) {
				if (!((String) token.get(LEXER_TYPE)).equals("")) {
					lexerType = QueryToken.Category.LEXER_TYPE;
				}
			}
			if (tokenType == TokenType.FREETEXT) {
				tokenId = name;
				facetId = Facet.KEYWORD_FACET.getId();
			} else if (tokenType == TokenType.KEYWORD) {
				facetId = Facet.KEYWORD_FACET.getId();
			}
			
			if (null != lexerType) {
				switch(lexerType) {
				case AND:
					tokenType = TokenType.AND;
					break;
				case OR:
					tokenType = TokenType.OR;
					break;
				case NOT:
					tokenType = TokenType.NOT;
					break;
				case LPAR:
					tokenType = TokenType.LEFT_PARENTHESIS;
					break;
				case RPAR:
					tokenType = TokenType.RIGHT_PARENTHESIS;
					break;
				default: break;
				}
			}
			if (token.has(QUERY)) {
				queryString = token.getString(QUERY);
				priority = QueryPriority.valueOf(token.getString(PRIORITY));
			}
			if (token.has(TOKEN_TYPE)) {
				tokenType = TokenType.valueOf(token.getString(TOKEN_TYPE));
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
			qt.setType(lexerType);
			qt.setInputTokenType(tokenType);

			Facet facet = facetService.getFacetById(facetId);
			if (null == facet) {
				log.warn("Term with facet ID {} was suggested but this facet does not exist. "
						+ "The employed suggestion index and the term database are out of sync."
						+ " The query token is ignored. It was: {}", facetId, token);
				continue;
			}

//			if (queryString != null) {
//				 QueryBuilder query = QueryBuilders.wrapperQuery(queryString);
//				 qt.setQuery(query);
//				 qt.setPriority(priority);
//			}

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
			} else if (tokenType == TokenType.AMBIGUOUS_CONCEPT && token.has("disambiguationOptions")) {
				JSONArray options = token.getJSONArray("disambiguationOptions");
				for (int j = 0; j < options.length(); ++j) {
					String termId = options.getString(j); 
					IConcept concept = termService.getTerm(termId);
					if (null == concept) {
						log.warn(
								"Concept with ID {} part of an ambiguous query token but this concept does not exist. ",
								tokenId);
						continue;
					}
					qt.addConceptToList(concept);
				}
			}

			tokens.add(qt);
		}
		log.debug("Converted JSONArray of input terms {} to query tokens: {}.", tokenInput, tokens);
		return tokens;
	}

	@Override
	public JSONArray convertQueryToJson(List<QueryToken> queryTokens, String showDialogLink, String getConceptTokensLink) {
		try {
			if (queryTokens != null) {
				JSONArray jsonTokens = new JSONArray();

				if (log.isDebugEnabled()) {
					StringBuilder sb = new StringBuilder();

					for (QueryToken node : queryTokens) // lohr - Bearbeitung
					// aller eingeg. Token
					// (durch Leerzeichen
					// getrennt)
					{
						sb.append(node.getOriginalValue());

						sb.append(" ");
					}

					sb.deleteCharAt(sb.length() - 1);
					log.debug("Filling 'token' parameter for prepopulation of AutoComplete mixin with nodes: {}",
							sb.toString());
				}

				for (QueryToken qt : queryTokens) {
					log.debug("Now converting query token '{}'", qt.getOriginalValue());
					JSONObject currentObject = new JSONObject();
					ITokenInputService.TokenType tokenType = qt.getInputTokenType();
//					QueryBuilder query = qt.getQuery();
//					if (query != null) {
//						String queryString = query.toString();
//						currentObject.put("query", queryString);
//						String priority = qt.getPriority().toString();
//						currentObject.put("priority", priority);
//					}

					switch (qt.getInputTokenType()) {
						case AMBIGUOUS_CONCEPT:
							// disambiguationOptions
							JSONArray disambiguationOptions = new JSONArray();

							for (IConcept concept : qt.getConceptList()) {
								disambiguationOptions.put(concept.getId());
							}

							// TODO instead pass the values to this method from the frontend
							//currentObject.put("showDialogLink", disambiguationDialog.getShowDialogLink().toAbsoluteURI());
							//currentObject.put("getConceptTokensLink",
							//		resources.createEventLink("getConceptTokens").toAbsoluteURI());
							currentObject.put("disambiguationOptions", disambiguationOptions);
							currentObject.put("name", qt.getOriginalValue());
							break;
						case CONCEPT:
							currentObject.put("termid", qt.getConceptList().get(0).getId());

							if (null != qt.getMatchedSynonym()) {
								currentObject.put("name", qt.getMatchedSynonym());

								if (!qt.getMatchedSynonym().equals(qt.getConceptList().get(0).getPreferredName())) {
									currentObject.put(ITokenInputService.PREFERRED_NAME,
											qt.getConceptList().get(0).getPreferredName());
								}
							} else if (null != qt.getOriginalValue()) {
								currentObject.put("name", qt.getOriginalValue());
								if (!qt.getOriginalValue().equals(qt.getConceptList().get(0).getPreferredName())) {
									currentObject.put(ITokenInputService.PREFERRED_NAME,
											qt.getConceptList().get(0).getPreferredName());
								}
							} else {
								currentObject.put("name", qt.getConceptList().get(0).getPreferredName());
							}

							currentObject.put(ITokenInputService.USER_SELECTED, qt.isUserSelected());
							JSONArray synonyms = new JSONArray();

							for (String synonym : qt.getConceptList().get(0).getSynonyms()) {
								synonyms.put(synonym);
							}

							currentObject.put("synonyms", synonyms);

							if (null != qt.getConceptList().get(0).getDescriptions()
									&& qt.getConceptList().get(0).getDescriptions().size() > 0) {
								JSONArray descriptions = new JSONArray();
								for (String description : qt.getConceptList().get(0).getDescriptions()) {
									descriptions.put(description);
								}
								currentObject.put("descriptions", descriptions);
							}

							currentObject.put(ITokenInputService.FACET_NAME,
									qt.getConceptList().get(0).getFirstFacet().getName());
							break;

						case KEYWORD:
							currentObject.put(ITokenInputService.USER_SELECTED, qt.isUserSelected());
							currentObject.put("name", qt.getOriginalValue());
							currentObject.put(ITokenInputService.FACET_NAME, Facet.KEYWORD_FACET.getName());
							break;
						case AND:
						case OR:
						case NOT:
						case LEXER:
							currentObject.put(ITokenInputService.LEXER_TYPE, String.valueOf(qt.getType()));
							currentObject.put("name", qt.getInputTokenType().name());
							currentObject.put(ITokenInputService.FACET_NAME, Facet.BOOLEAN_OPERATORS_FACET.getName());
							break;
						case LEFT_PARENTHESIS:
							currentObject.put(ITokenInputService.LEXER_TYPE, String.valueOf(qt.getType()));
							currentObject.put("name", "(");
							currentObject.put(ITokenInputService.FACET_NAME, Facet.BOOLEAN_OPERATORS_FACET.getName());
							break;
						case RIGHT_PARENTHESIS:
							currentObject.put(ITokenInputService.LEXER_TYPE, String.valueOf(qt.getType()));
							currentObject.put("name", ")");
							currentObject.put(ITokenInputService.FACET_NAME, Facet.BOOLEAN_OPERATORS_FACET.getName());
							break;
						default:
							tokenType = TokenType.LEXER;
							currentObject.put(ITokenInputService.LEXER_TYPE, String.valueOf(qt.getType()));
							currentObject.put("name", qt.getOriginalValue());
							break;
					}
					currentObject.put(ITokenInputService.TOKEN_TYPE, tokenType.name());
					jsonTokens.put(currentObject);
					log.debug("Adding JSON to search field: {}", currentObject);
				}
				return jsonTokens;
			}
		} catch (Exception e) {
			log.error(
					"Exception occurred during conversion of query tokens into JSON format for token input field prepopulation:",
					e);
			// something went wrong with query translation; this could be due to
			// a corrupted query. Shouldn't happen, of course, but better reset
			// the query or we won't ever recover
			// TODO handle this in the search class
			//sessionState.getDocumentRetrievalSearchState().setDisambiguatedQuery(null);
		}
		return null;
	}
}
