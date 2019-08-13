package de.julielab.semedico.core.services.query;

import de.julielab.semedico.core.parsing.SecopiaParse;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.search.query.UserQuery;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;

import java.util.ArrayList;
import java.util.List;

import static de.julielab.semedico.core.services.SemedicoCoreModule.searchTraceLog;

public class SecopiaQueryAnalysisService implements ISecopiaQueryAnalysisService {
    private ILexerService lexerService;
    private IConceptRecognitionService conceptRecognitionService;
    private ISecopiaParsingService parsingService;

    public SecopiaQueryAnalysisService(ILexerService lexerService, IConceptRecognitionService conceptRecognitionService, ISecopiaParsingService parsingService) {
        this.lexerService = lexerService;
        this.conceptRecognitionService = conceptRecognitionService;
        this.parsingService = parsingService;
    }

    @Override
    public SecopiaParse analyseQueryString(String queryString) {
        searchTraceLog.info("Search query given as string: {}", queryString);
        UserQuery uq = new UserQuery(queryString);
        return analyseQueryString(uq);
    }

    @Override
    public SecopiaParse analyseQueryString(UserQuery userQuery) {
        SecopiaParse tree = null;
        if (userQuery.tokens != null && !userQuery.tokens.isEmpty()) {
            List<QueryToken> tokensForParsing = new ArrayList<>();
            for (QueryToken qt : userQuery.tokens) {
                if (qt.getInputTokenType() == ITokenInputService.TokenType.FREETEXT) {
                    final List<QueryToken> lexedTokens = lexerService.lex(qt.getOriginalValue());
                    final List<QueryToken> conceptAnalyzedTokens = conceptRecognitionService.recognizeTerms(lexedTokens);
                    tokensForParsing.addAll(conceptAnalyzedTokens);
                } else {
                    tokensForParsing.add(qt);
                }
            }
            tree = parsingService.parseQueryTokens(tokensForParsing);
        }
        return tree;
    }

}
