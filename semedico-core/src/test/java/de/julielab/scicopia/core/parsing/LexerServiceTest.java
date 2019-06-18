package de.julielab.scicopia.core.parsing;

import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.query.ILexerService;
import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
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
        assertThat(token.getBeginOffset()).isEqualTo(0);
        assertThat(token.getEndOffset()).isEqualTo(4);
        assertThat(token.getOriginalValue()).isEqualTo("well");
        assertThat(token.getType()).isEqualTo(QueryToken.Category.ALPHA);
        assertThat(token.getInputTokenType()).isEqualTo(ITokenInputService.TokenType.FREETEXT);
    }
}
