package de.julielab.semedico.core.services.query;

import static de.julielab.semedico.core.services.SemedicoCoreModule.searchTraceLog;

import java.util.ArrayList;
import java.util.List;

import de.julielab.semedico.core.services.interfaces.IStopWordService;
import org.slf4j.Logger;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.ParseTree.Serialization;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.search.query.UserQuery;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

public class QueryAnalysisService implements IQueryAnalysisService {

	private ILexerService lexerService;
	private IStopWordService stopWordService;
	private IConceptRecognitionService termRecognitionService;
	private IParsingService parsingService;
	private Logger log;

	public QueryAnalysisService(Logger log, ILexerService lexerService, IStopWordService stopWordService,
			IConceptRecognitionService termRecognitionService, IParsingService parsingService) {
		this.log = log;
		this.lexerService = lexerService;
		this.stopWordService = stopWordService;
		this.termRecognitionService = termRecognitionService;
		this.parsingService = parsingService;
	}

	@Override
	public ParseTree analyseQueryString(UserQuery userQuery, long searchStateId, boolean compress) {
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
					if (userToken.getInputTokenType() == TokenType.FREETEXT) {
						String freetext = userToken.getOriginalValue();
						// Tokenize freetext query and recognize terms.
						List<QueryToken> freetextLex = lexerService.lex(freetext);
						for (QueryToken lexerToken : freetextLex) {
							lexerToken.setBeginOffset(lexerToken.getBeginOffset() + userToken.getBeginOffset());
							lexerToken.setEndOffset(lexerToken.getEndOffset() + userToken.getBeginOffset());
						}

						List<QueryToken> tokensTerms = termRecognitionService.recognizeTerms(freetextLex,
								searchStateId);

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
//		QueryToken freetextToken = new QueryToken(0, userQuery.length());
//		freetextToken.setOriginalValue(userQuery);
//		freetextToken.setInputTokenType(TokenType.FREETEXT);
//		// uq.freetextQuery = userQuery;
//		uq.tokens = Arrays.asList(freetextToken);
		return analyseQueryString(uq, 0, false);
	}

	@Override
	public ParseTree analyseQueryString(UserQuery userQuery, long id) {
		return analyseQueryString(userQuery, 0, false);
	}
}
