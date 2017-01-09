package de.julielab.semedico.core.services.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.query.UserQuery;
import de.julielab.semedico.core.services.interfaces.ILexerService;
import de.julielab.semedico.core.services.interfaces.IParsingService;
import de.julielab.semedico.core.services.interfaces.IQueryAnalysisService;
import de.julielab.semedico.core.services.interfaces.ITermRecognitionService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;



public class QueryAnalysisService implements IQueryAnalysisService {

	private ILexerService lexerService;
	private ITermRecognitionService termRecognitionService;
	private ITermRecognitionService eventRecognitionService;
//	private IQueryTokenAlignmentService queryTokenAlignmentService;
	private IParsingService parsingService;
	/**
	 * Only required as long as we do the user token input merging here, which shoud go somewhere else
	 * (quertokenalignmentservice?)
	 */
	private ITermService termService;
	private Logger log;

	public QueryAnalysisService(Logger log, ILexerService lexerService,
			ITermRecognitionService termRecognitionService,
			IParsingService parsingService,
			ITermService termService) {
		this.log = log;
		this.lexerService = lexerService;
		this.termRecognitionService = termRecognitionService;
		this.parsingService = parsingService;
		this.termService = termService;
	}

	@Override
	public ParseTree analyseQueryString(UserQuery userQuery, long searchStateId, boolean compress) {
		try {
			List<QueryToken> finalQueryTokens = new ArrayList<>();
			log.debug("Got user query tokens: {}", userQuery.tokens);
			if (!userQuery.tokens.isEmpty()) {
//				List<QueryToken> eventTokens = new ArrayList<>();
				List<QueryToken> termTokens = new ArrayList<>();
				for (QueryToken userToken : userQuery.tokens) {
					if (userToken.getInputTokenType() == TokenType.FREETEXT) {
						String freetext = userToken.getOriginalValue();
						// Tokenize freetext query and recognize terms.
						List<QueryToken> freetextLex = lexerService.lex(freetext);
						for (QueryToken lexerToken : freetextLex) {
							lexerToken.setBeginOffset(lexerToken.getBeginOffset() + userToken.getBeginOffset());
							lexerToken.setEndOffset(lexerToken.getEndOffset() + userToken.getBeginOffset());
						}
						
//						List<QueryToken> tokensCopy = QueryToken.copyQueryTokenList(freetextLex);
						// TODO perhaps the whole UserQuery object should be passed as parameter, think that through
						List<QueryToken> tokensTerms =
								termRecognitionService.recognizeTerms(freetextLex, searchStateId);
//						List<QueryToken> tokensEvents =
//								eventRecognitionService.recognizeTerms(tokensCopy, searchStateId, null, null);
						
						termTokens.addAll(tokensTerms);
//						eventTokens.addAll(tokensEvents);
					} else {
						termTokens.add(userToken);
					}
				}
				
//				List<QueryToken> alignedQueryTokens = queryTokenAlignmentService.alignQueryTokens(eventTokens, termTokens);
//				finalQueryTokens = lexerService.filterStopTokens(alignedQueryTokens);
				finalQueryTokens = lexerService.filterStopTokens(termTokens);

			 }

			log.debug("Final query tokens: {}", finalQueryTokens);
			ParseTree parseTree = parsingService.parse(finalQueryTokens);
			log.debug("Uncompressed query ParseTree: {}", parseTree);
			if (compress) {
				parseTree = parseTree.compress();
				log.debug("Compressed query ParseTree: {}", parseTree); 
			}
			else {
				log.debug("Compression of query ParseTree is deactivated.");
			}
			return parseTree;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ParseTree analyseQueryString(String userQuery) {
		UserQuery uq = new UserQuery();
		QueryToken freetextToken = new QueryToken(0, userQuery.length());
		freetextToken.setOriginalValue(userQuery);
		freetextToken.setFreetext(true);
//		uq.freetextQuery = userQuery;
		uq.tokens = Arrays.asList(freetextToken);
		return analyseQueryString(uq, 0, false);
	}

	@Override
	public ParseTree analyseQueryString(UserQuery userQuery, long id) {
		return analyseQueryString(userQuery, 0, false);
	}

}
