package de.julielab.scicopia.core.parsing;

import de.julielab.scicopia.core.elasticsearch.IElasticsearchQueryBuilder;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import org.apache.tapestry5.ioc.Registry;
import org.elasticsearch.index.query.QueryBuilder;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScicopiaQueryListenerTest {
    @Test
    public void testListener() {
        final Registry testRegistry = TestUtils.createTestRegistry();
        final IElasticsearchQueryBuilder qb = testRegistry.getService(IElasticsearchQueryBuilder.class);
        List<QueryToken> tokens = new ArrayList<>();
        final QueryToken qt1 = new QueryToken(0, 6, "bonsai");
        qt1.setType(QueryToken.Category.ALPHA);
        qt1.setInputTokenType(ITokenInputService.TokenType.KEYWORD);
        final QueryToken qt2 = new QueryToken(7, 10, "and");
        qt2.setType(QueryToken.Category.AND);
        qt2.setInputTokenType(ITokenInputService.TokenType.AND);
        final QueryToken qt3 = new QueryToken(11, 12, "(");
        qt3.setType(QueryToken.Category.LPAR);
        qt3.setInputTokenType(ITokenInputService.TokenType.LEFT_PARENTHESIS);
        final QueryToken qt4 = new QueryToken(12, 17, "tulip");
        qt4.setType(QueryToken.Category.ALPHA);
        qt4.setInputTokenType(ITokenInputService.TokenType.KEYWORD);
        final QueryToken qt5 = new QueryToken(18, 21, "or");
        qt5.setType(QueryToken.Category.OR);
        qt5.setInputTokenType(ITokenInputService.TokenType.OR);
        final QueryToken qt6 = new QueryToken(22, 26, "rose");
        qt6.setType(QueryToken.Category.ALPHA);
        qt6.setInputTokenType(ITokenInputService.TokenType.KEYWORD);
        final QueryToken qt7 = new QueryToken(26, 27, ")");
        qt7.setType(QueryToken.Category.RPAR);
        qt7.setInputTokenType(ITokenInputService.TokenType.RIGHT_PARENTHESIS);
        final QueryBuilder queryBuilder = qb.analyseQueryString(new ArrayList<>(Arrays.asList(qt1, qt2, qt3, qt4, qt5, qt6, qt7)));
        System.out.println(queryBuilder.toString());

//        CodePointCharStream stream = CharStreams.fromString("bonsai and (tulip or rose)");
//        ScicopiaLexer lexer = new ScicopiaLexer(stream);
//        CommonTokenStream tokenstream = new CommonTokenStream(lexer);
//        ScicopiaParser parser = new ScicopiaParser(tokenstream);
//        ParseTree tree = parser.phrase();
//        ParseTreeWalker walker = new ParseTreeWalker();
        // ScicopiaQueryListener listener = new ScicopiaQueryListener(Collections.emptyList(), tokens, chunker, termService, stopWordService, log);
    }
}
