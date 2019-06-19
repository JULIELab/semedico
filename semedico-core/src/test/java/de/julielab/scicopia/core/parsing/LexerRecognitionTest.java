package de.julielab.scicopia.core.parsing;


import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LexerRecognitionTest {

    @Test
    public void alphaTest() {
        CodePointCharStream stream = CharStreams.fromString("Trichodiene");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("ALPHA", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void dashTest() {
        CodePointCharStream stream = CharStreams.fromString("alpha-Copaene");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("DASH", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void alphanumericDashTest() {
        CodePointCharStream stream = CharStreams.fromString("Delta6-Protoilludene");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("DASH", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void dashNumberPrefixTest() {
        CodePointCharStream stream = CharStreams.fromString("5-Epiaristolochene");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("DASH", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void multiDashTest() {
        CodePointCharStream stream = CharStreams.fromString("7-epi-Sesquithujene");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("DASH", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void numTest() {
        CodePointCharStream stream = CharStreams.fromString("cis-Muurola-3,5-diene");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("NUM", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void eCompoundTest() {
        CodePointCharStream stream = CharStreams.fromString("(E)-2-epi-beta-Caryophyllene");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("COMPOUND", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void sCompoundTest() {
        CodePointCharStream stream = CharStreams.fromString("(S)-beta-Macrocarpene");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("COMPOUND", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void minusCompoundTest() {
        CodePointCharStream stream = CharStreams.fromString("(-)-delta-Cadinene");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("COMPOUND", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void plusCompundTest() {
        CodePointCharStream stream = CharStreams.fromString("(+)-gamma-Cadinene");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("COMPOUND", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void commaAfterParenthesisTest() {
        CodePointCharStream stream = CharStreams.fromString("cis-Muurola-4(14),5-diene");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("COMPOUND", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void parenthesisAfterCommaTest() {
        CodePointCharStream stream = CharStreams.fromString("Valerena-4,7(11)-diene");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("COMPOUND", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void apostropheTest() {
        CodePointCharStream stream = CharStreams.fromString("O'Reilly");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("APOSTROPHE", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void multiApostropheTest() {
        CodePointCharStream stream = CharStreams.fromString("O'Reilly's");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("APOSTROPHE", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void LeftArrowTest() {
        CodePointCharStream stream = CharStreams.fromString("<-");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("ARROW", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void longLeftArrowTest() {
        CodePointCharStream stream = CharStreams.fromString("<--");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("ARROW", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void rightArrowTest() {
        CodePointCharStream stream = CharStreams.fromString("->");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("ARROW", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void longRightArrowTest() {
        CodePointCharStream stream = CharStreams.fromString("-->");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("ARROW", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void bidirectionalArrowTest() {
        CodePointCharStream stream = CharStreams.fromString("<->");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("ARROW", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void longBidirectionalArrowTest() {
        CodePointCharStream stream = CharStreams.fromString("<-->");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("ARROW", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void lowerCaseAndTest() {
        CodePointCharStream stream = CharStreams.fromString("and");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("AND", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void uppercaseAndTest() {
        CodePointCharStream stream = CharStreams.fromString("AND");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("AND", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void titlecaseAndTest() {
        CodePointCharStream stream = CharStreams.fromString("And");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("AND", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void symbolicAndTest() {
        CodePointCharStream stream = CharStreams.fromString("&");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("AND", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void symbolicDoubleAndTest() {
        CodePointCharStream stream = CharStreams.fromString("&&");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("AND", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void lowercaseOrTest() {
        CodePointCharStream stream = CharStreams.fromString("or");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("OR", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void uppercaseOrTest() {
        CodePointCharStream stream = CharStreams.fromString("OR");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("OR", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void titlecaseOrTest() {
        CodePointCharStream stream = CharStreams.fromString("Or");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("OR", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void symbolicOrTest() {
        CodePointCharStream stream = CharStreams.fromString("|");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("OR", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void symbolicDoubleOrTest() {
        CodePointCharStream stream = CharStreams.fromString("||");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("OR", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void lowercaseNotTest() {
        CodePointCharStream stream = CharStreams.fromString("not");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("NOT", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void uppercaseNotTest() {
        CodePointCharStream stream = CharStreams.fromString("NOT");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("NOT", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void titlecaseNotTest() {
        CodePointCharStream stream = CharStreams.fromString("Not");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("NOT", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void symbolicNotTest() {
        CodePointCharStream stream = CharStreams.fromString("!");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("NOT", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void IRITest() {
        CodePointCharStream stream = CharStreams.fromString("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C1908");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("IRI", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void encodedIRITest() {
        CodePointCharStream stream = CharStreams.fromString("http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FMESH%2FD002477");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("IRI", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void leftParenthesisTest() {
        CodePointCharStream stream = CharStreams.fromString("(");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("LPAR", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void specialTest() {
        CodePointCharStream stream = CharStreams.fromString("⌨0⌨");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("SPECIAL", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void hashtagTest() {
        CodePointCharStream stream = CharStreams.fromString("#aging");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        int type = lexer.nextToken().getType();
        assertEquals("HASHTAG", lexer.getVocabulary().getSymbolicName(type));
    }

    @Test
    public void multipleTokensTest() {
        CodePointCharStream stream = CharStreams.fromString("melanoma or 'skin cancer'");
        ScicopiaLexer lexer = new ScicopiaLexer(stream);
        StringBuilder sb = new StringBuilder();
        Token t = lexer.nextToken();
        do {
            sb.append(lexer.getVocabulary().getSymbolicName(t.getType())).append(" ");
            t = lexer.nextToken();
        } while (t.getType() != -1);
        assertEquals("ALPHA OR null ALPHA ALPHA null", sb.toString().trim());

        final Vocabulary voc = lexer.getVocabulary();
        lexer.getTokenTypeMap().values().stream().map(type -> voc.getDisplayName(type)).forEach(System.out::println);
    }

}
