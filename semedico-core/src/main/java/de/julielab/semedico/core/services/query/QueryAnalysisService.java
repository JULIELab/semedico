package de.julielab.semedico.core.services.query;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.ParseTree.SERIALIZATION;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.ILexerService;
import de.julielab.semedico.core.services.interfaces.IParsingService;
import de.julielab.semedico.core.services.interfaces.IQueryAnalysisService;
import de.julielab.semedico.core.services.interfaces.IStopWordService;
import de.julielab.semedico.core.services.interfaces.ITermRecognitionService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

public class QueryAnalysisService implements IQueryAnalysisService {

	private ILexerService lexerService;
	private ITermRecognitionService termRecognitionService;
	private IParsingService parsingService;

	/**
	 * Only required as long as we do the user token input merging here, which should go somewhere else
	 * (quertokenalignmentservice?)
	 */
	private IStopWordService stopWordService;
	private Logger log;

	public QueryAnalysisService(Logger log, ILexerService lexerService,
			ITermRecognitionService termRecognitionService,
			IParsingService parsingService,
			IStopWordService stopWordService) {
		this.log = log;
		this.lexerService = lexerService;
		this.termRecognitionService = termRecognitionService;
		this.parsingService = parsingService;
		this.stopWordService = stopWordService;
		if (null != stopWordService) {
			this.stopWordService.loadStopWords();
		}
	}

	@Override
	public ParseTree analyseQueryString(List<QueryToken> tokens) {
		try {
			List<QueryToken> finalQueryTokens = new ArrayList<>();
			log.debug("Got user query tokens: {}", tokens);
			if (!tokens.isEmpty()) {
				List<QueryToken> termTokens = new ArrayList<>();
				for (QueryToken userToken : tokens) {
					System.out.println("Original: " + userToken.getOriginalValue());
					System.out.println(userToken.getInputTokenType());
					if (userToken.getInputTokenType() == TokenType.FREETEXT
							|| userToken.getInputTokenType() == TokenType.LEXER) {
						String freetext = userToken.getOriginalValue();
						// Tokenize freetext query and recognize terms.
						List<QueryToken> freetextLex = lexerService.lex(freetext);
						for (QueryToken lexerToken : freetextLex) {
							lexerToken.setBeginOffset(lexerToken.getBeginOffset() + userToken.getBeginOffset());
							lexerToken.setEndOffset(lexerToken.getEndOffset() + userToken.getBeginOffset());
							System.out.println(lexerToken.getOriginalValue() + ": " + lexerToken.getInputTokenType());
						}
						
						List<QueryToken> tokensTerms =
								termRecognitionService.recognizeTerms(freetextLex);
						
						termTokens.addAll(tokensTerms);
					} else {
						termTokens.add(userToken);
					}
				}
				
				finalQueryTokens = stopWordService.filterStopTokens(termTokens);

			 }

			log.debug("Final query tokens: {}", finalQueryTokens);
			ParseTree parseTree = parsingService.parse(finalQueryTokens);
			log.debug("ParseTree: {}", parseTree);

			log.debug("Final parse tree (IDs): {}", parseTree.toString(SERIALIZATION.IDS));
			return parseTree;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
