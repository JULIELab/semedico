package de.julielab.semedico.core.services.query;

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
    private IConceptService conceptService;
    private IFacetService facetService;

    public TokenInputService(Logger log, IConceptService conceptService, IFacetService facetService) {
        this.log = log;
        this.conceptService = conceptService;
        this.facetService = facetService;

    }

    @Override
    public List<QueryToken> convertToQueryTokens(JSONArray tokenInput) {
        List<QueryToken> tokens = new ArrayList<>();
        int offset = 0;
        for (int i = 0; i < tokenInput.length(); ++i) {
            JSONObject token = tokenInput.getJSONObject(i);

            // the following properties have default values to deal with
            // incomplete / unknown information
            String facetId = Facet.KEYWORD_FACET.getId();
            String tokenId = null;
            TokenType tokenType = null;
            String name = null;
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
            if (token.has(CONCEPT_ID)) {
                tokenId = token.getString(CONCEPT_ID);
            }
            if (token.has(USER_SELECTED)) {
                userSelected = token.getBoolean(USER_SELECTED);
            }
            if (token.has(NAME)) {
                name = token.getString(NAME);
            }

            if (tokenType == TokenType.FREETEXT) {
                tokenId = name;
                facetId = Facet.KEYWORD_FACET.getId();
            } else if (tokenType == TokenType.KEYWORD) {
                facetId = Facet.KEYWORD_FACET.getId();
            }

            if (token.has(TOKEN_TYPE)) {
                tokenType = TokenType.valueOf(token.getString(TOKEN_TYPE));
            }

            QueryToken qt;
            if (tokenType != TokenType.CONCEPT_PHRASE) {
                qt = new QueryToken(offset, offset + name.length());
            } else {
                List<QueryToken> conceptTokens = convertToQueryTokens(token.getJSONArray(TOKENS));
                qt = new QueryToken(offset, !conceptTokens.isEmpty() ? conceptTokens.get(conceptTokens.size() - 1).getEnd() : offset);
                qt.setSubTokens(conceptTokens);
            }
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
            qt.setInputTokenType(tokenType);

            Facet facet = facetService.getFacetById(facetId);
            if (null == facet) {
                log.warn("Term with facet ID {} was suggested but this facet does not exist. "
                        + "The employed suggestion index and the term database are out of sync."
                        + " The query token is ignored. It was: {}", facetId, token);
                continue;
            }

            if (tokenType == TokenType.CONCEPT || tokenType == TokenType.WILDCARD) {
                IConcept term = conceptService.getTerm(tokenId);
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
                    IConcept concept = conceptService.getTerm(termId);
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

                    switch (qt.getInputTokenType()) {
                        case AMBIGUOUS_CONCEPT:
                            // disambiguationOptions
                            JSONArray disambiguationOptions = new JSONArray();

                            for (IConcept concept : qt.getConceptList()) {
                                disambiguationOptions.put(concept.getId());
                            }

                            currentObject.put("disambiguationOptions", disambiguationOptions);
                            currentObject.put(NAME, qt.getOriginalValue());
                            break;
                        case CONCEPT:
                        case WILDCARD:
                            currentObject.put(CONCEPT_ID, qt.getConceptList().get(0).getId());

                            if (null != qt.getMatchedSynonym()) {
                                currentObject.put(NAME, qt.getMatchedSynonym());

                                if (!qt.getMatchedSynonym().equals(qt.getConceptList().get(0).getPreferredName())) {
                                    currentObject.put(ITokenInputService.PREFERRED_NAME,
                                            qt.getConceptList().get(0).getPreferredName());
                                }
                            } else if (null != qt.getOriginalValue()) {
                                currentObject.put(NAME, qt.getOriginalValue());
                                if (!qt.getOriginalValue().equals(qt.getConceptList().get(0).getPreferredName())) {
                                    currentObject.put(ITokenInputService.PREFERRED_NAME,
                                            qt.getConceptList().get(0).getPreferredName());
                                }
                            } else {
                                currentObject.put(NAME, qt.getConceptList().get(0).getPreferredName());
                            }

                            currentObject.put(ITokenInputService.USER_SELECTED, qt.isUserSelected());
                            JSONArray synonyms = new JSONArray();

                            for (String synonym : qt.getConceptList().get(0).getSynonyms()) {
                                synonyms.put(synonym);
                            }

                            currentObject.put(SYNONYMS, synonyms);

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
                        case CONCEPT_PHRASE:
                            currentObject.put(TOKENS, convertQueryToJson(qt.getSubTokens(), showDialogLink, getConceptTokensLink));
                            break;
                        case KEYWORD:
                            currentObject.put(ITokenInputService.USER_SELECTED, qt.isUserSelected());
                            currentObject.put(NAME, qt.getOriginalValue());
                            currentObject.put(ITokenInputService.FACET_NAME, Facet.KEYWORD_FACET.getName());
                            break;
                        case AND:
                        case OR:
                        case NOT:
                            currentObject.put(NAME, qt.getInputTokenType().name());
                            currentObject.put(ITokenInputService.FACET_NAME, Facet.BOOLEAN_OPERATORS_FACET.getName());
                            break;
                        case LEFT_PARENTHESIS:
                            currentObject.put(NAME, "(");
                            currentObject.put(ITokenInputService.FACET_NAME, Facet.BOOLEAN_OPERATORS_FACET.getName());
                            break;
                        case RIGHT_PARENTHESIS:
                            currentObject.put(NAME, ")");
                            currentObject.put(ITokenInputService.FACET_NAME, Facet.BOOLEAN_OPERATORS_FACET.getName());
                            break;
                        default:
                            throw new IllegalArgumentException("Unhandled token input type " + qt.getInputTokenType());
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
        }
        return null;
    }
}
