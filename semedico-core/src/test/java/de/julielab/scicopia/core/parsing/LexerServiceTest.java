package de.julielab.scicopia.core.parsing;

import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.query.ILexerService;
import org.apache.tapestry5.ioc.Registry;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
public class LexerServiceTest {

    private static Registry registry;

    @BeforeClass
    public static void beforeClass() {
        registry = TestUtils.createTestRegistry();
    }

    @AfterClass
    public static void afterClass() {
        registry.shutdown();
    }

    @Test
    public void testLexerService() {
        final ILexerService lexerService = registry.getService(ILexerService.class);
        final List<QueryToken> tokens = lexerService.lex("well");
        assertThat(tokens).hasSize(1);
        final QueryToken token = tokens.get(0);
        assertThat(token.getBegin()).isEqualTo(0);
        assertThat(token.getEnd()).isEqualTo(4);
        assertThat(token.getOriginalValue()).isEqualTo("well");
        assertThat(token.getType()).isEqualTo(QueryToken.Category.ALPHA);
        assertThat(token.getInputTokenType()).isEqualTo(ITokenInputService.TokenType.FREETEXT);
    }

    @Test
    public void testTopicTag() {
        final ILexerService lexerService = registry.getService(ILexerService.class);
        final List<QueryToken> tokens = lexerService.lex("#groundwater");
        assertThat(tokens).hasSize(1);
        final QueryToken token = tokens.get(0);
        assertThat(token.getOriginalValue()).isEqualTo("#groundwater");
        assertThat(token.getType()).isEqualTo(QueryToken.Category.HASHTAG);
        assertThat(token.getInputTokenType()).isEqualTo(ITokenInputService.TokenType.TOPIC_TAG);
    }

    @Test
    public void testPhrase() {
        final ILexerService lexerService = registry.getService(ILexerService.class);
        final List<QueryToken> tokens = lexerService.lex("\"male mice\"");
        assertThat(tokens).hasSize(1);
        final QueryToken token = tokens.get(0);
        assertThat(token.getOriginalValue()).isEqualTo("male mice");
        assertThat(token.getType()).isEqualTo(QueryToken.Category.PHRASE);
        assertThat(token.getInputTokenType()).isEqualTo(ITokenInputService.TokenType.FREETEXT);
    }


    @Test
    public void testBooleanOperators() {
        final ILexerService lexerService = registry.getService(ILexerService.class);
        final List<QueryToken> tokens = lexerService.lex("(a or b) and (c or not d5)");
        assertThat(tokens).hasSize(12);
         QueryToken token = tokens.get(0);
        assertThat(token.getBegin()).isEqualTo(0);
        assertThat(token.getEnd()).isEqualTo(1);
        assertThat(token.getType()).isEqualTo(QueryToken.Category.LPAR);

        token = tokens.get(1);
        assertThat(token.getBegin()).isEqualTo(1);
        assertThat(token.getEnd()).isEqualTo(2);
        assertThat(token.getType()).isEqualTo(QueryToken.Category.ALPHA);

        token = tokens.get(2);
        assertThat(token.getBegin()).isEqualTo(3);
        assertThat(token.getEnd()).isEqualTo(5);
        assertThat(token.getType()).isEqualTo(QueryToken.Category.OR);

        token = tokens.get(3);
        assertThat(token.getBegin()).isEqualTo(6);
        assertThat(token.getEnd()).isEqualTo(7);
        assertThat(token.getType()).isEqualTo(QueryToken.Category.ALPHA);

        token = tokens.get(4);
        assertThat(token.getBegin()).isEqualTo(7);
        assertThat(token.getEnd()).isEqualTo(8);
        assertThat(token.getType()).isEqualTo(QueryToken.Category.RPAR);

        token = tokens.get(5);
        assertThat(token.getBegin()).isEqualTo(9);
        assertThat(token.getEnd()).isEqualTo(12);
        assertThat(token.getType()).isEqualTo(QueryToken.Category.AND);

        token = tokens.get(6);
        assertThat(token.getBegin()).isEqualTo(13);
        assertThat(token.getEnd()).isEqualTo(14);
        assertThat(token.getType()).isEqualTo(QueryToken.Category.LPAR);

        token = tokens.get(7);
        assertThat(token.getBegin()).isEqualTo(14);
        assertThat(token.getEnd()).isEqualTo(15);
        assertThat(token.getType()).isEqualTo(QueryToken.Category.ALPHA);

        token = tokens.get(8);
        assertThat(token.getBegin()).isEqualTo(16);
        assertThat(token.getEnd()).isEqualTo(18);
        assertThat(token.getType()).isEqualTo(QueryToken.Category.OR);

        token = tokens.get(9);
        assertThat(token.getBegin()).isEqualTo(19);
        assertThat(token.getEnd()).isEqualTo(22);
        assertThat(token.getType()).isEqualTo(QueryToken.Category.NOT);

        token = tokens.get(10);
        assertThat(token.getBegin()).isEqualTo(23);
        assertThat(token.getEnd()).isEqualTo(25);
        assertThat(token.getType()).isEqualTo(QueryToken.Category.ALPHANUM);

        token = tokens.get(11);
        assertThat(token.getBegin()).isEqualTo(25);
        assertThat(token.getEnd()).isEqualTo(26);
        assertThat(token.getType()).isEqualTo(QueryToken.Category.RPAR);
    }
}
