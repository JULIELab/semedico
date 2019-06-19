package de.julielab.semedico.core.services.query;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.ParseTree.Serialization;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.search.query.UserQuery;
import de.julielab.semedico.core.services.interfaces.IStopWordService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.julielab.semedico.core.services.SemedicoCoreModule.searchTraceLog;

public class QueryAnalysisService implements IQueryAnalysisService {

	private ILexerService lexerService;
	private IStopWordService stopWordService;
	private IConceptRecognitionService conceptRecognitionService;
	private IParsingService parsingService;
	private Logger log;

	public QueryAnalysisService(Logger log, ILexerService lexerService, IStopWordService stopWordService,
								IConceptRecognitionService conceptRecognitionService, IParsingService parsingService) {
		this.log = log;
		this.lexerService = lexerService;
		this.stopWordService = stopWordService;
		this.conceptRecognitionService = conceptRecognitionService;
		this.parsingService = parsingService;
	}

	@Override
	public ParseTree analyseQueryString(UserQuery userQuery, boolean compress) {
		try {
			searchTraceLog.info("Original user query is: {}", userQuery.tokens);
			List<QueryToken> finalQueryTokens = new ArrayList<>();
			log.debug("Got user query tokens: {}", userQuery.tokens);
			if (!userQuery.tokens.isEmpty()) {
				// conceptTokens will not really only contain contains eventually, but tokens that were
				// either set to a non-freetext type before entering this method or that have been
				// checked for concepts below
				List<QueryToken> conceptTokens = new ArrayList<>();
				for (QueryToken userToken : userQuery.tokens) {
					if (userToken.getInputTokenType() == TokenType.FREETEXT
                            || userToken.getInputTokenType() == TokenType.LEXER) {
						String freetext = userToken.getOriginalValue();
						// Tokenize freetext query and recognize terms.
						List<QueryToken> freetextLex = lexerService.lex(freetext);
						for (QueryToken lexerToken : freetextLex) {
							lexerToken.setBegin(lexerToken.getBegin() + userToken.getBegin());
							lexerToken.setEnd(lexerToken.getEnd() + userToken.getBegin());
						}

						List<QueryToken> tokensTerms = conceptRecognitionService.recognizeTerms(freetextLex);

						conceptTokens.addAll(tokensTerms);
					} else {
						conceptTokens.add(userToken);
					}
				}

				finalQueryTokens = stopWordService.filterStopTokens(conceptTokens);

			}

			log.debug("Final query tokens: {}", finalQueryTokens);
			ParseTree parseTree = parsingService.parse(finalQueryTokens);
			log.debug("Uncompressed query ParseTree: {}", parseTree);
			if (compress) {
				parseTree = parseTree.compress();
				log.debug("Compressed query ParseTree: {}", parseTree);
			} else {
				log.debug("Compression of query ParseTree is deactivated.");
			}
			if (log.isDebugEnabled())
				log.debug("Final parse tree (IDs): {}", parseTree.toString(Serialization.NODE_IDS));
			if (searchTraceLog.isInfoEnabled())
				searchTraceLog.info("Final parse tree: {}", parseTree.toString(Serialization.NODE_TEXT));
			return parseTree;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ParseTree analyseQueryString(String userQuery) {
		searchTraceLog.info("Search query given as string: {}", userQuery);
		UserQuery uq = new UserQuery(userQuery);
		return analyseQueryString(uq, false);
	}

	@Override
	public ParseTree analyseQueryString(UserQuery userQuery) {
		return analyseQueryString(userQuery, false);
	}

}
