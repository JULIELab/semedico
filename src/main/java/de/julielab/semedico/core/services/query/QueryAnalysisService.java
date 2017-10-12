package de.julielab.semedico.core.services.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tapestry5.ioc.services.SymbolSource;
import org.slf4j.Logger;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.ParseTree.SERIALIZATION;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.query.UserQuery;
import de.julielab.semedico.core.services.ReconfigurablesService;
import de.julielab.semedico.core.services.interfaces.IConceptRecognitionService;
import de.julielab.semedico.core.services.interfaces.ILexerService;
import de.julielab.semedico.core.services.interfaces.IParsingService;
import de.julielab.semedico.core.services.interfaces.IQueryAnalysisService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

public class QueryAnalysisService implements IQueryAnalysisService, ReconfigurablesService {

	private ILexerService lexerService;
	private IConceptRecognitionService termRecognitionService;
	private IParsingService parsingService;
	private Logger log;

	public QueryAnalysisService(Logger log, ILexerService lexerService,
			IConceptRecognitionService termRecognitionService, IParsingService parsingService) {
		this.log = log;
		this.lexerService = lexerService;
		this.termRecognitionService = termRecognitionService;
		this.parsingService = parsingService;
	}

	@Override
	public ParseTree analyseQueryString(UserQuery userQuery, long searchStateId, boolean compress) {
		try {
			List<QueryToken> finalQueryTokens = new ArrayList<>();
			log.debug("Got user query tokens: {}", userQuery.tokens);
			if (!userQuery.tokens.isEmpty()) {
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

						List<QueryToken> tokensTerms = termRecognitionService.recognizeTerms(freetextLex,
								searchStateId);

						termTokens.addAll(tokensTerms);
					} else {
						termTokens.add(userToken);
					}
				}

				finalQueryTokens = lexerService.filterStopTokens(termTokens);

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
			log.debug("Final parse tree (IDs): {}", parseTree.toString(SERIALIZATION.IDS));
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
		freetextToken.setInputTokenType(TokenType.FREETEXT);
		// uq.freetextQuery = userQuery;
		uq.tokens = Arrays.asList(freetextToken);
		return analyseQueryString(uq, 0, false);
	}

	@Override
	public ParseTree analyseQueryString(UserQuery userQuery, long id) {
		return analyseQueryString(userQuery, 0, false);
	}

	@Override
	public void configure(SymbolSource symbolSource) {
	}

	@Override
	public void configure(SymbolSource symbolSource, boolean recursive) {
		configure(symbolSource);
		if (recursive) {
			termRecognitionService.configure(symbolSource, recursive);
		}

	}

}
