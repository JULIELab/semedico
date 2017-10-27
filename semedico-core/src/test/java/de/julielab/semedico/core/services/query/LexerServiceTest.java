package de.julielab.semedico.core.services.query;

import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

public class LexerServiceTest {

	private LexerService lexer;

	private List<QueryToken> lex(String s) throws IOException {
		lexer = new LexerService(LoggerFactory.getLogger(LexerService.class), null);
		List<QueryToken> tokens = lexer.lex(s);
		return tokens;
	}

	@Test
	public void testLexing() throws Exception {
		List<QueryToken> tokens = lex("a b c");

		assertEquals(ALPHANUM, tokens.get(0).getType());
		assertEquals("a", tokens.get(0).getOriginalValue());
		assertEquals("b", tokens.get(1).getOriginalValue());
		assertEquals("c", tokens.get(2).getOriginalValue());
	}

	@Test
	public void testAND() throws Exception {
		List<QueryToken> tokens = lex("a b c AND x || y");

		assertEquals("a", tokens.get(0).getOriginalValue());
		assertEquals("b", tokens.get(1).getOriginalValue());
		assertEquals("c", tokens.get(2).getOriginalValue());
		assertEquals(AND_OPERATOR, tokens.get(3).getType());
		assertEquals("x", tokens.get(4).getOriginalValue());
		assertEquals(OR_OPERATOR, tokens.get(5).getType());
		assertEquals("y", tokens.get(6).getOriginalValue());
	}

	@Test
	public void testSyntax() throws Exception {
		List<QueryToken> tokens = lex("a NOT  b   ((    c and x OR y");

		assertEquals("a", tokens.get(0).getOriginalValue());
		assertEquals(NOT_OPERATOR, tokens.get(1).getType());
		assertEquals("b", tokens.get(2).getOriginalValue());
		assertEquals(LEFT_PARENTHESIS, tokens.get(3).getType());
		assertEquals(LEFT_PARENTHESIS, tokens.get(4).getType());
		assertEquals("c", tokens.get(5).getOriginalValue());
		assertEquals(AND_OPERATOR, tokens.get(6).getType());
		assertEquals("x", tokens.get(7).getOriginalValue());
		assertEquals(OR_OPERATOR, tokens.get(8).getType());
		assertEquals("y", tokens.get(9).getOriginalValue());
	}

	@Test
	public void testSyntax2() throws Exception {
		// boolean operators directly after or before parenthesis do not make
		// much sense so we take them to be normal words
		List<QueryToken> tokens = lex("(and or)");

		assertEquals(LEFT_PARENTHESIS, tokens.get(0).getType());
		assertEquals(ALPHANUM, tokens.get(1).getType());
		assertEquals(ALPHANUM, tokens.get(2).getType());
		assertEquals(RIGHT_PARENTHESIS, tokens.get(3).getType());
	}

	@Test
	public void testOffsets() throws Exception {
		String query = "foo bar (\"test\" || \"mouse\"  )";
		List<QueryToken> tokens = lex(query);

		assertEquals("foo", tokens.get(0).getOriginalValue());
		assertEquals(0, tokens.get(0).getBeginOffset());
		assertEquals(3, tokens.get(0).getEndOffset());
		assertEquals("test", tokens.get(3).getOriginalValue());
		assertEquals(9, tokens.get(3).getBeginOffset());
		assertEquals(15, tokens.get(3).getEndOffset());
		assertEquals(")", tokens.get(6).getOriginalValue());
		assertEquals(28, tokens.get(6).getBeginOffset());
		assertEquals(29, tokens.get(6).getEndOffset());
	}

	@Test
	public void testSpecialTerms() throws Exception {
		List<QueryToken> tokens = lex("* not word");
		assertEquals("*", tokens.get(0).getOriginalValue());
		assertEquals(0, tokens.get(0).getBeginOffset());
		assertEquals(1, tokens.get(0).getEndOffset());
		assertEquals(WILDCARD_TOKEN, tokens.get(0).getType());

		assertEquals("not", tokens.get(1).getOriginalValue());
		assertEquals(2, tokens.get(1).getBeginOffset());
		assertEquals(5, tokens.get(1).getEndOffset());
		// This is wrong, of course, it should be alphanum. But it isn't and it
		// doesn't really hurt until now, but it's
		// strange.
		assertEquals(NOT_OPERATOR, tokens.get(1).getType());

		assertEquals("word", tokens.get(2).getOriginalValue());
		assertEquals(6, tokens.get(2).getBeginOffset());
		assertEquals(10, tokens.get(2).getEndOffset());
	}

	@Test
	public void testNot() throws Exception {
		List<QueryToken> lex = lex("golf -volkswagen");
		assertEquals(3, lex.size());
		assertEquals(0, lex.get(0).getType());
		assertEquals("golf", lex.get(0).getOriginalValue());
		assertEquals(21, lex.get(1).getType());
		assertEquals("-", lex.get(1).getOriginalValue());
		assertEquals(0, lex.get(2).getType());
		assertEquals("volkswagen", lex.get(2).getOriginalValue());
	}

	@Test
	public void testDash() throws Exception {
		// when there are whitespaces around a dash, we don't make anything of
		// it
		List<QueryToken> lex = lex("plate - bound");
		assertEquals(2, lex.size());
		assertEquals(0, lex.get(0).getType());
		assertEquals(0, lex.get(1).getType());

		lex = lex("plate- bound");
		assertEquals(2, lex.size());
		assertEquals(0, lex.get(0).getType());
		assertEquals(0, lex.get(1).getType());

		// in THIS case we interpret as "plate NOT bound"
		lex = lex("plate -bound");
		assertEquals(3, lex.size());
	}

	@Test
	public void testDash2() throws Exception {
		// a dash embedded into a word means a compound
		List<QueryToken> lex = lex("plate-bound");
		assertEquals(1, lex.size());
		QueryToken qt = lex.get(0);
		assertEquals(QueryTokenizerImpl.DASH, qt.getType());
		System.out.println(qt.getInputTokenType());
	}

	@Test
	public void testDash3() throws Exception {
		// what about multiple dashes in the same word? Should still be a
		// compound.
		List<QueryToken> lex = lex("plate-bound-regulated");
		assertEquals(1, lex.size());
		assertEquals(QueryTokenizerImpl.DASH, lex.get(0).getType());
	}

	@Test
	public void testDash4() throws Exception {
		// what about multiple dashes in the same word? Should still be a
		// compound.
		List<QueryToken> lex = lex("gp41-induced");
		assertEquals(1, lex.size());
		assertEquals(QueryTokenizerImpl.DASH, lex.get(0).getType());
	}

	@Test
	public void testDash5() throws Exception {
		// what about multiple dashes in the same word? Should still be a
		// compound.
		List<QueryToken> lex = lex("il-10");
		assertEquals(1, lex.size());
		assertEquals(QueryTokenizerImpl.DASH, lex.get(0).getType());
	}

	@Test
	public void testDash6() throws Exception {
		// what about multiple dashes in the same word? Should still be a
		// compound.
		List<QueryToken> lex = lex("p70(S6)-kinase");
		for (QueryToken qt : lex)
			System.out.println(qt);
		assertEquals(1, lex.size());
		assertEquals(QueryTokenizerImpl.DASH, lex.get(0).getType());

		lex = lex("p70{S6}-kinase");
		for (QueryToken qt : lex)
			System.out.println(qt);
		assertEquals(1, lex.size());
		assertEquals(QueryTokenizerImpl.DASH, lex.get(0).getType());

		lex = lex("p70[S6]-kinase");
		for (QueryToken qt : lex)
			System.out.println(qt);
		assertEquals(1, lex.size());
		assertEquals(QueryTokenizerImpl.DASH, lex.get(0).getType());

		// this is kind of screwed up, we don't make anything of the punctuation
		lex = lex("p70[S6)-kinase");
		for (QueryToken qt : lex)
			System.out.println(qt);
		assertEquals(4, lex.size());
		assertEquals(QueryTokenizerImpl.ALPHANUM, lex.get(0).getType());
		assertEquals(QueryTokenizerImpl.ALPHANUM, lex.get(1).getType());
		assertEquals(QueryTokenizerImpl.RIGHT_PARENTHESIS, lex.get(2).getType());
		assertEquals(QueryTokenizerImpl.ALPHANUM, lex.get(3).getType());

		// basically the same as above
		lex = lex("p70(S6}-kinase");
		for (QueryToken qt : lex)
			System.out.println(qt);
		assertEquals(4, lex.size());
		assertEquals(QueryTokenizerImpl.ALPHANUM, lex.get(0).getType());
		assertEquals(QueryTokenizerImpl.LEFT_PARENTHESIS, lex.get(1).getType());
		assertEquals(QueryTokenizerImpl.ALPHANUM, lex.get(2).getType());
		assertEquals(QueryTokenizerImpl.ALPHANUM, lex.get(3).getType());
	}

	@Test
	public void testDash7() throws Exception {
		// what about multiple dashes in the same word? Should still be a
		// compound.
		List<QueryToken> lex = lex("4E-BP2");
		assertEquals(1, lex.size());
		assertEquals(QueryTokenizerImpl.DASH, lex.get(0).getType());
	}

	@Test
	public void testDash8() throws Exception {
		// what about multiple dashes in the same word? Should still be a
		// compound.
		List<QueryToken> lex = lex("r2-d2");
		assertEquals(1, lex.size());
		assertEquals(QueryTokenizerImpl.DASH, lex.get(0).getType());
	}

	@Test
	public void testNum() throws Exception {
		// dashes are also used to just separate columns of numbers. Those are
		// no compounds and we try to be sensible here
		List<QueryToken> lex = lex("35534-325254-23452345-234553");
		assertEquals(1, lex.size());
		assertEquals(QueryTokenizerImpl.NUM, lex.get(0).getType());
	}

	@Test
	public void testParenthesisExpression() throws IOException {
		List<QueryToken> lex = lex("p70(s6)k");
		for (QueryToken qt : lex)
			System.out.println(qt);
		assertEquals(1, lex.size());
		assertEquals(QueryTokenizerImpl.ALPHANUM_EMBEDDED_PAR, lex.get(0).getType());
	}

	@Test
	public void testCompactQuery() throws Exception {
		List<QueryToken> queryTokens = lex("NOT(x AND y)OR c");
		for (int i = 0; i < queryTokens.size(); i++) {
			QueryToken qt = queryTokens.get(i);
			switch (i) {
			case 0:
				assertEquals(QueryTokenizerImpl.NOT_OPERATOR, qt.getType());
				break;
			case 1:
				assertEquals(QueryTokenizerImpl.LEFT_PARENTHESIS, qt.getType());
				break;
			case 2:
				assertEquals(QueryTokenizerImpl.ALPHANUM, qt.getType());
				break;
			case 3:
				assertEquals(QueryTokenizerImpl.AND_OPERATOR, qt.getType());
				break;
			case 4:
				assertEquals(QueryTokenizerImpl.ALPHANUM, qt.getType());
				break;
			case 5:
				assertEquals(QueryTokenizerImpl.RIGHT_PARENTHESIS, qt.getType());
				break;
			case 6:
				assertEquals(QueryTokenizerImpl.OR_OPERATOR, qt.getType());
				break;
			case 7:
				assertEquals(QueryTokenizerImpl.ALPHANUM, qt.getType());
				break;
			}
		}
	}
	
	@Test
	public void testPhrase() throws IOException {
		List<QueryToken> lex = lex ("\"this is a phrase\"");
		assertEquals("Too many query tokens", 1, lex.size());
		QueryToken qt = lex.get(0);
		assertEquals("Wrong lexer type", QueryTokenizerImpl.PHRASE, qt.getType());
		assertEquals("Wrong input token type", TokenType.FREETEXT, qt.getInputTokenType());
	}

}
