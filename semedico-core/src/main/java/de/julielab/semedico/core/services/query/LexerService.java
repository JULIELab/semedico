package de.julielab.semedico.core.services.query;

import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.AND_ALPHANUM;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.AND_OPERATOR;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.NOT_ALPHANUM;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.NOT_OPERATOR;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.NOT_WS_OPERATOR;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.OR_ALPHANUM;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.OR_OPERATOR;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;

import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.SemedicoCoreModule;
import de.julielab.semedico.core.services.interfaces.IStopWordService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;
import java_cup.runtime.Symbol;

/**
 * A simple lexer to tokenize a query String.
 * 
 * @author engelmann
 * 
 */
public class LexerService implements ILexerService
{
	private Logger logger;
	private Set<String> stopwords;
	private Stack<QueryToken> pendingTokens;

	public LexerService(Logger logger, IStopWordService stopWordService)
	{
		this.logger = logger;
		if (null != stopWordService)
		{
			stopwords = stopWordService.getAsSet();
		}
		pendingTokens = new Stack<>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.query.ILexerService#lex(java.lang
	 * .String)
	 */
	@Override
	public List<QueryToken> lex(String query) throws IOException
	{
		List<QueryToken> tokens = new ArrayList<QueryToken>();

		String lexerQuery = " " + query;
		QueryTokenizerImpl lexer = new QueryTokenizerImpl(new StringReader(lexerQuery));
		QueryToken qt = generateNextQueryToken(lexer);
		
		while (qt != null)
		{
			logger.debug("Next token from lexer = " + qt.getOriginalValue());
			qt = normalizeBoolean(qt);
			qt.setInputTokenType(determineBooleanInputType(qt));
			if (qt.getType() == QueryTokenizerImpl.HASHTAG)
			    qt.setInputTokenType(TokenType.TOPIC_TAG);
			tokens.add(qt);
			qt = generateNextQueryToken(lexer);
		}
		return tokens;
	}

	private TokenType determineBooleanInputType(QueryToken qt)
	{
		TokenType tokenType = qt.getInputTokenType();
		switch (qt.getType())
		{
			case QueryTokenizerImpl.AND_ALPHANUM:
			case QueryTokenizerImpl.AND_OPERATOR:
			{
				tokenType = TokenType.AND;
				break;
			}
			case QueryTokenizerImpl.OR_ALPHANUM:
			case QueryTokenizerImpl.OR_OPERATOR:
			{
				tokenType = TokenType.OR;
				break;
			}
			case QueryTokenizerImpl.NOT_ALPHANUM:
			case QueryTokenizerImpl.NOT_OPERATOR:
			{
				tokenType = TokenType.NOT;
				break;
			}
			case QueryTokenizerImpl.LEFT_PARENTHESIS:
			{
				tokenType = TokenType.LEFT_PARENTHESIS;
				break;
			}
			case QueryTokenizerImpl.RIGHT_PARENTHESIS:
			{
				tokenType = TokenType.RIGHT_PARENTHESIS;
				break;
			}
		}
		return tokenType;
	}

	/**
	 * Generate QueryTokens out of Symbols retrieved from lexer.
	 * 
	 * @return The next QueryToken.
	 */
	private QueryToken generateNextQueryToken(QueryTokenizerImpl lexer) throws IOException
	{
		if (!pendingTokens.isEmpty())
		{
			return pendingTokens.pop();
		}
		
		Symbol symbol = lexer.getNextToken();

		int qtType = -1;
		String qtString = null;
		int symBegin = -1;
		int symEnd = -1;

		if (symbol != null)
		{
			qtType = symbol.sym;
			if (symbol.value != null && symbol.value.getClass() == String.class)
			{
				qtString = (String) symbol.value;
			}
			// TODO: The QueryTokenizer uses the method correctOffset()
			// from the Tokenizer; is this in any way relevant?
			symBegin = lexer.yychar();
			symEnd = lexer.yychar() + lexer.yylength();
		}

		QueryToken qt = null;
		// For alphabetical OR and AND operators, the grammar has a composite rule to capture those operators when they follow immediately on a right parentheses. There reason is that normally, we require a whitespace before the operator. 
		if (qtType == QueryTokenizerImpl.RIGHT_PAREN_OR)
		{
			// The right parenthesis becomes a token of its own.
			QueryToken rightParenToken = new QueryToken(symBegin, symBegin + 1);
			rightParenToken.setType(QueryTokenizerImpl.RIGHT_PARENTHESIS);
			rightParenToken.setOriginalValue(qtString.substring(0, 1));

			// The rest of the token is actually the boolean operator part.
			QueryToken orToken = new QueryToken(symBegin + 1, symEnd);
			// This composite rule is only necessary for alphanumerical
			// operators.
			orToken.setType(QueryTokenizerImpl.OR_ALPHANUM);
			orToken.setOriginalValue(qtString.substring(1));
			// Push the operator token to the stack to be retrieved next.
			pendingTokens.push(orToken);

			qt = rightParenToken;
		}
		else if (qtType != -1)
		{
			qt = new QueryToken(symBegin, symEnd);
			qt.setType(qtType);
			// remove the quotes from phrases
			if (qtType == QueryTokenizerImpl.PHRASE)
			{
				qt.setOriginalValue(qtString.substring(1, qtString.length() - 1));
			}
			else
			{
				qt.setOriginalValue(qtString);
			}
		}
		if (null != qt)
		{
			qt.setBeginOffset(qt.getBeginOffset() - 1);
			qt.setEndOffset(qt.getEndOffset() - 1);
		}
		return qt;
	}

	/**
	 * Normalize boolean QueryToken types. The grammar makes a difference
	 * between a alphanumerical AND and the operator writings & or &&. We
	 * currently make no use of this information and normalize this to avoid
	 * problems later in the query analysis pipeline.
	 * 
	 * @param qt
	 *            The QueryToken.
	 * @return The QueryToken with normalized type.
	 */
	private QueryToken normalizeBoolean(QueryToken qt)
	{
		if (qt == null)
		{
			return null;
		}
		if (qt.getType() == AND_ALPHANUM || qt.getType() == AND_OPERATOR)
		{
			qt.setType(AND_OPERATOR);
		}
		if (qt.getType() == OR_ALPHANUM || qt.getType() == OR_OPERATOR)
		{
			qt.setType(OR_OPERATOR);
		}
		if (qt.getType() == NOT_ALPHANUM || qt.getType() == NOT_OPERATOR || qt.getType() == NOT_WS_OPERATOR)
		{
			qt.setType(NOT_OPERATOR);
		}
		return qt;
	}

	/**
	 * Removes <tt>QueryTokens</tt> that appear in the stopword list. Ignores
	 * concept tokens. NOTE: This method should be used AFTER all applications
	 * of the concept recognition Chunker since concept dictionary entries may
	 * contain stopwords and wouldn't match anymore.
	 */
	@Override
	public List<QueryToken> filterStopTokens(List<QueryToken> queryTokens)
	{
		if (null == stopwords || stopwords.isEmpty())
		{
			return queryTokens;
		}
		List<QueryToken> filteredList = new ArrayList<>();
		for (QueryToken token : queryTokens)
		{
			if (token.isConceptToken()
				|| !stopwords.contains(token.getOriginalValue().toLowerCase())
				|| token.getOriginalValue().equals("(")
				|| token.getOriginalValue().equals(")")
				|| token.getType() == QueryTokenizerImpl.AND_OPERATOR
				|| token.getType() == QueryTokenizerImpl.OR_OPERATOR
				|| token.getType() == QueryTokenizerImpl.NOT_OPERATOR)
			{
				filteredList.add(token);
			}
			else {
				logger.debug("Filtering query token {} because it is a stopword.", token);
				SemedicoCoreModule.searchTraceLog.debug("Filtering query token {} because it is a stopword.", token);
			}
		}
		return filteredList;
	}
}