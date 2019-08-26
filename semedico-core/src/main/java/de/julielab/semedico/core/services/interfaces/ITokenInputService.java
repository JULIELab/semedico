package de.julielab.semedico.core.services.interfaces;

import de.julielab.semedico.core.search.query.QueryToken;
import org.apache.tapestry5.json.JSONArray;

import java.util.List;

public interface ITokenInputService {
    String CONCEPT_ID = "conceptid";
    String FACET_ID = "facetid";
    String FACET_NAME = "facetname";
    String NAME = "name";
    String PREFERRED_NAME = "preferredname";
    String SYNONYMS = "synonyms";
    String TOKEN_TYPE = "tokentype";
    String LEXER_TYPE = "lexertype";
    String USER_SELECTED = "userselected";
    String BEGIN = "tokenBegin";
    String QUERY = "query";
    String PRIORITY = "priority";

    JSONArray convertQueryToJson(List<QueryToken> queryTokens, String showDialogLink, String getConceptTokensLink);

    List<QueryToken> convertToQueryTokens(JSONArray tokenInput);

    /**
     * These are the possible types of tokens in the token input field. They are
     * read directly from the tokens in the search field when running a Semedico
     * query. FREETEXT is special in the way that consecutive FREETEXT tokens
     * undergo a concept recognition process in the search process. For all
     * other token input types, their meaning is fixed by their type.
     *
     * @author faessler
     */
    enum TokenType {
        FREETEXT, KEYWORD, CONCEPT, AMBIGUOUS_CONCEPT, AND, OR, NOT, LEFT_PARENTHESIS, RIGHT_PARENTHESIS,
        /**
         * This token is not to be searched verbatim or to be resolved to a particular concept but describes a topic of
         * discourse. This is used together with the topic modeling module.
         */
        TOPIC_TAG, WILDCARD,
        /**
         * Refer to the {@link ITokenInputService#LEXER_TYPE} property.
         */
        LEXER
    }
}
