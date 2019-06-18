package de.julielab.scicopia.core.parsing;

import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.query.ILexerService;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import java.util.ArrayList;
import java.util.List;

import static de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType.*;


/**
 * <p>Uses the ANTLR lexer to produce ANTLR tokens which are then converted into {@link QueryToken} objects.</p>
 * <p>The QueryTokens get their offset set relative to the input string and the substring at these offsets.
 * Additionally, two important properties for subsequent query analysis are set, namely the <tt>type</tt> (a {@link de.julielab.semedico.core.search.query.QueryToken.Category})
 * and the <tt>inputType</tt> (a value of {@link de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType}).
 * of the QueryTokens. The first reflects the lexer token category. Since the ANTLR grammar knows more states thatn the {@link de.julielab.semedico.core.search.query.QueryToken.Category},
 * the <tt>type</tt> is not always a direct reflection of the original lexer type but somewhat reduced. To simplify subsequent query
 * analysis, multiple original lexer types may be mapped to a single Category.</p>
 * <p>The second property, the <tt>inputType</tt> relates to the different meanings of the visualized input tokens in the
 * search bar of the frontend. The user query is always captured in 'input tokens' created by the jQuery
 * TokenInput plugin (which has been adapted quite a lot for Semedico). The input token type is meant to specify
 * the token semantics as specifically as possible. Thus, when the user selects a concept suggestion, the input token
 * type will be set to {@link de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType.CONCEPT} and
 * further analysis will not be required. Indeed, this lexer is only required for input tokens of type
 * {@link de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType.FREETEXT} which explicitly
 * reflects the fact that the user did not choose any suggestion and also did not create the token as a keyword
 * tokens (those would be searched as a classical verbatim text term in the inverted index without concept
 * resolution).</p>
 */
public class LexerService implements ILexerService {

    @Override
    public List<QueryToken> lex(String query) {
        List<QueryToken> tokens = new ArrayList<>();

        CodePointCharStream stream = CharStreams.fromString(query);
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        Vocabulary voc = lexer.getVocabulary();
        while (!lexer._hitEOF) {
            Token token = lexer.nextToken();
            int tokenStart = token.getStartIndex();
            int tokenEnd = token.getStopIndex() + 1;
            String name = voc.getDisplayName(token.getType());
            // Quotes indicate phrases. We need to handle those and make all token between a matching pair of quotes a phrase.
            if (name.equals("'''") || name.equals("'\"'")) {
                ++tokenStart;
                ++tokenEnd;
                token = lexer.nextToken();
                while (!lexer._hitEOF && !voc.getDisplayName(token.getType()).equals(name)) {
                    token = lexer.nextToken();
                    tokenEnd = token.getStopIndex();
                }
                name = "PHRASE";
            }
            QueryToken qt = new QueryToken(tokenStart, tokenEnd, query.substring(tokenStart, tokenEnd));
            qt.setType(getTokenCategory(name));
            qt.setInputTokenType(getInputType(qt.getType()));
            tokens.add(qt);
        }
        return tokens;
    }

    /**
     * <p>
     * From the lexer vocabulary, the following tokenDisplayNames are possible:
     * <ul>
     * <li>ABBREV</li>
     * <li>x ALPHA</li>
     * <li>x ALPHANUM</li>
     * <li>x AND</li>
     * <li>x APOSTROPHE</li>
     * <li>ARROW</li>
     * <li>ARROWBOTH</li>
     * <li>ARROWLEFT</li>
     * <li>ARROWRIGHT</li>
     * <li>x COMPOUND</li>
     * <li>x DASH</li>
     * <li>x DIGITS</li>
     * <li>EOF</li>
     * <li>x IRI</li>
     * <li>x NOT</li>
     * <li>x NUM</li>
     * <li>x OR</li>
     * <li>SPECIAL</li>
     * <li>WHITESPACE</li>
     * <li>'"'</li>
     * <li>'''</li>
     * <li>x '('</li>
     * <li>x '('</li>
     * <li>x ')'</li>
     * <li>x ')'</li>
     * <li>'+'</li>
     * <li>'-'</li>
     * <li>':'</li>
     * </ul>
     * The entries marked with a leading 'x' are matched directly to a {@link de.julielab.semedico.core.search.query.QueryToken.Category} enum constant
     * or are handled specifically. All others are mapped to {@link de.julielab.semedico.core.search.query.QueryToken.Category#ALPHANUM}.
     * </p>
     *
     * @param tokenDisplayName
     * @return
     */
    private QueryToken.Category getTokenCategory(String tokenDisplayName) {
        QueryToken.Category category;
        // A number of display names match the respective Category enum constant directly.
        // Thus, first try to derive the constant directly.
        try {
            category = QueryToken.Category.valueOf(tokenDisplayName);
        } catch (IllegalArgumentException e) {
            // If we came here, the tokenDisplayName is not a Category enum constant itself.
            switch (tokenDisplayName) {
                case "'('":
                    category = QueryToken.Category.LPAR;
                    break;
                case "')'":
                    category = QueryToken.Category.RPAR;
                    break;
                case "DIGITS":
                    category = QueryToken.Category.NUM;
                    break;
                default:
                    category = QueryToken.Category.ALPHANUM;
            }
        }
        return category;
    }

    /**
     * <p>
     * Returns a preliminary input token type given the lexer category. This is - at this stage of query analysis - only
     * possible for tokens where the lexer type matches the input token type which are the boolean operators
     * and, or, not, the parenthesis and topic model search tags. All other lexer types are mapped to {@link de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType.FREETEXT}
     * which means that automatic concept- and keyword recognition should be run on the text of these tokens.
     * </p>
     *
     * @param name
     * @return
     */
    private ITokenInputService.TokenType getInputType(QueryToken.Category name) {
        ITokenInputService.TokenType tokenType;
        switch (name) {
            case AND:
                tokenType = AND;
                break;
            case OR:
                tokenType = OR;
                break;
            case NOT:
                tokenType = NOT;
                break;
            case LPAR:
                tokenType = LEFT_PARENTHESIS;
                break;
            case RPAR:
                tokenType = RIGHT_PARENTHESIS;
                break;
            case HASHTAG:
                tokenType = TOPIC_TAG;
                break;
            default:
                tokenType = FREETEXT;
        }
        return tokenType;
    }

}
