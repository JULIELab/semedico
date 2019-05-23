package de.julielab.scicopia.core.parsing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.ILexerService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

public class LegacyLexerService implements ILexerService {

	@Override
	public List<QueryToken> lex(String query) throws IOException {
		List<QueryToken> tokens = new ArrayList<>();
		
		CodePointCharStream stream = CharStreams.fromString(query);
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		Vocabulary voc = lexer.getVocabulary();
		while (!lexer._hitEOF) {
			Token token = lexer.nextToken();
			int tokenStart = token.getStartIndex();
			int tokenEnd = token.getStopIndex() + 1;
			String name = voc.getDisplayName(token.getType());
			if (name.equals("'''") || name.equals("'\"'")) {
				++tokenStart;
				++tokenEnd;
				token = lexer.nextToken();
				while (!lexer._hitEOF && !voc.getDisplayName(token.getType()).equals("'''")) {
					token = lexer.nextToken();
					tokenEnd = token.getStopIndex();
				}
			}
			QueryToken qt = new QueryToken(tokenStart, tokenEnd, query.substring(tokenStart, tokenEnd));
//			qt.setType(ScicopiaLexer.ruleNames[token.getType()-1]);
//			tokens.add(setInputType(qt, name));
		}
		return tokens;
	}

//	private QueryToken setInputType(QueryToken qt, String name) {
//		switch (name) {
//			case "OR": 	qt.setType("OR");
//						qt.setInputTokenType(TokenType.OR);
//						break;
//			case "AND": qt.setType("AND");
//						qt.setInputTokenType(TokenType.AND);
//						break;
//			case "NOT": qt.setType("NOT");
//						qt.setInputTokenType(TokenType.NOT);
//						break;
//			case "'('": qt.setInputTokenType(TokenType.LEFT_PARENTHESIS);
//						break;
//			case "')'": qt.setInputTokenType(TokenType.RIGHT_PARENTHESIS);
//						break;
//		}
//		return qt;
//	}

}
