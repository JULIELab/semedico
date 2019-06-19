package de.julielab.scicopia.core.parsing;

import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.query.ILexerService;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import java.util.ArrayList;
import java.util.List;

public class LegacyLexerService implements ILexerService {

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


}
