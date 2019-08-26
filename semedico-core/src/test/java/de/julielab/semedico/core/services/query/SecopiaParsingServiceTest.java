package de.julielab.semedico.core.services.query;

import de.julielab.semedico.core.parsing.SecopiaParse;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class SecopiaParsingServiceTest {
    @Test
    public void testParse() {
        QueryToken t1 = new QueryToken(0, 4, "MTOR");
        t1.setInputTokenType(ITokenInputService.TokenType.FREETEXT);
        QueryToken t2 = new QueryToken(5, 8, "and");
        t2.setInputTokenType(ITokenInputService.TokenType.AND);
        QueryToken t3 = new QueryToken(9, 14, "MOUSE");
        t3.setInputTokenType(ITokenInputService.TokenType.FREETEXT);
        QueryToken t4 = new QueryToken(15, 18, "and");
        t4.setInputTokenType(ITokenInputService.TokenType.AND);
        QueryToken t5 = new QueryToken(19, 36, "CAT");
        t5.setInputTokenType(ITokenInputService.TokenType.FREETEXT);
        QueryToken t6 = new QueryToken(37, 39, "or");
        t2.setInputTokenType(ITokenInputService.TokenType.OR);
        QueryToken t7 = new QueryToken(40, 43, "not");
        t2.setInputTokenType(ITokenInputService.TokenType.NOT);
        QueryToken t8 = new QueryToken(44, 48, "FISH");

        final SecopiaParsingService parsingService = new SecopiaParsingService(LoggerFactory.getLogger(SecopiaParsingService.class));

        final List<QueryToken> tokens = Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8);
        final ParseTree parseTree = parsingService.parseQueryTokens(tokens).getParseTree();

        final ToStringListener l = new ToStringListener();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(l, parseTree);
        assertEquals(l.getStringBuilder().toString(), "((MTOR and MOUSE) and CAT) or not FISH");
    }

    @Test
    public void testParse2() {
        QueryToken t1 = new QueryToken(0, 4, "MTOR");
        t1.setInputTokenType(ITokenInputService.TokenType.FREETEXT);
        QueryToken t2 = new QueryToken(5, 8, "and");
        t2.setInputTokenType(ITokenInputService.TokenType.AND);
        QueryToken t3 = new QueryToken(9, 14, "MOUSE");
        t3.setInputTokenType(ITokenInputService.TokenType.FREETEXT);
        QueryToken t4 = new QueryToken(15, 18, "and");
        t4.setInputTokenType(ITokenInputService.TokenType.AND);
        QueryToken t5 = new QueryToken(19, 36, "CAT");
        t5.setInputTokenType(ITokenInputService.TokenType.FREETEXT);
        QueryToken t7 = new QueryToken(37, 40, "not");
        t2.setInputTokenType(ITokenInputService.TokenType.NOT);
        QueryToken t8 = new QueryToken(40, 44, "FISH");

        final SecopiaParsingService parsingService = new SecopiaParsingService(LoggerFactory.getLogger(SecopiaParsingService.class));

        final List<QueryToken> tokens = Arrays.asList(t1, t2, t3, t4, t5, t7, t8);
        final ParseTree parseTree = parsingService.parseQueryTokens(tokens).getParseTree();

        final ToStringListener l = new ToStringListener();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(l, parseTree);
        assertEquals(l.getStringBuilder().toString(), "((MTOR and MOUSE) and CAT) not FISH");
    }

    @Test
    public void testParseWildcard() {
        QueryToken t1 = new QueryToken(0, 1, "*");
        t1.setLexerType(QueryToken.Category.WILDCARD);
        t1.setInputTokenType(ITokenInputService.TokenType.WILDCARD);

        final SecopiaParsingService parsingService = new SecopiaParsingService(LoggerFactory.getLogger(SecopiaParsingService.class));

        final List<QueryToken> tokens = Collections.singletonList(t1);
        final SecopiaParse parse = parsingService.parseQueryTokens(tokens);
        final ParseTree parseTree = parse.getParseTree();

        final ToStringListener l = new ToStringListener();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(l, parseTree);
        assertEquals(l.getStringBuilder().toString(), "*");
    }
}
