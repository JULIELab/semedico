package de.julielab.scicopia.core.parsing;


import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
public class ParserLogicalTests {

	@Test
	public void orTest() {
		CodePointCharStream stream = CharStreams.fromString("blood or death");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (bool (bool (token (term blood))) or (bool (token (term death)))))");
	}

	@Test
	public void andTest() {
		CodePointCharStream stream = CharStreams.fromString("blood or death and cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (bool (bool (token (term blood))) or (bool (bool (token (term death))) and (bool (token (term cancer))))))");
	}

	/**
	 * This construction will be a hard case for the term recognition
	 * and should later be resolved as follows: (E. coli) and (quorum sensing).
	 * Right now it is rather (E.) (coli) and (qorum) (sensing).
	 */
	@Test
	public void andAbbreviationTest() {
		CodePointCharStream stream = CharStreams.fromString("E. coli and quorum sensing");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (tokensequence (token (term E.))) (bool (bool (token (term coli))) and (bool (token (term quorum)))) (tokensequence (token (term sensing))))");
	}

	@Test
	public void doubleOrTest() {
		CodePointCharStream stream = CharStreams.fromString("blood OR death || cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (bool (bool (bool (token (term blood))) OR (bool (token (term death)))) || (bool (token (term cancer)))))");
	}

	@Test
	public void negationTest() {
		CodePointCharStream stream = CharStreams.fromString("not cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals( tree.toStringTree(parser), "(query (bool (negation not (token (term cancer)))))");
	}
	
	@Test
	public void doubleNegationTest() {
		CodePointCharStream stream = CharStreams.fromString("not not cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (bool (negation not (bool (negation not (token (term cancer)))))))");
	}
	
	@Test
	public void negativeConjunctionTest() {
		CodePointCharStream stream = CharStreams.fromString("blood and not cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (bool (bool (token (term blood))) and (bool (negation not (token (term cancer))))))");
	}

	@Test
	public void logicalOfLogicalsTest() {
		CodePointCharStream stream = CharStreams.fromString("blood or death and cancer or hiv");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (bool (bool (bool (token (term blood))) or (bool (bool (token (term death))) and (bool (token (term cancer))))) or (bool (token (term hiv)))))");
	}

	@Test
	public void quotesTest() {
		CodePointCharStream stream = CharStreams.fromString("\"Harald zur Hausen\" and cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (bool (bool (token (quotes \" Harald zur Hausen \"))) and (bool (token (term cancer)))))");
	}

	@Test
	public void prefixedTest() {
		CodePointCharStream stream = CharStreams.fromString("author:\"Harald zur Hausen\" and cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (bool (bool (token (prefixed author : (quotes \" Harald zur Hausen \")))) and (bool (token (term cancer)))))");
	}

}
