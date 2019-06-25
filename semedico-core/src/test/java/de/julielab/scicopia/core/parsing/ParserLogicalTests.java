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
		assertEquals(tree.toStringTree(parser), "(query (query (part (term blood))) or (query (part (term death))))");
	}

	@Test
	public void andTest() {
		CodePointCharStream stream = CharStreams.fromString("blood or death and cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (query (part (term blood))) or (query (query (part (term death))) and (query (part (term cancer)))))");
	}

	/**
	 * This construction will be a hard case for the term recognition
	 * and should later be resolved as follows: (E. coli) and (quorum sensing)
	 */
	@Test
	public void andAbbreviationTest() {
		CodePointCharStream stream = CharStreams.fromString("E. coli and quorum sensing");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (query (part (term E.)) (part (term coli))) and (query (part (term quorum)) (part (term sensing))))");
	}

	@Test
	public void doubleOrTest() {
		CodePointCharStream stream = CharStreams.fromString("blood OR death || cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (query (query (part (term blood))) OR (query (part (term death)))) || (query (part (term cancer))))");
	}

	@Test
	public void negationTest() {
		CodePointCharStream stream = CharStreams.fromString("not cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals( tree.toStringTree(parser), "(query query not (query (part (term cancer))))");
	}
	
	@Test
	public void doubleNegationTest() {
		CodePointCharStream stream = CharStreams.fromString("not not cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query not (query not (query (part (term cancer)))))");
	}
	
	@Test
	public void negativeConjunctionTest() {
		CodePointCharStream stream = CharStreams.fromString("blood and not cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals("(logical (part (term blood)) and (logical not (part (term cancer))))",
				tree.toStringTree(parser));
	}

	@Test
	public void logicalOfLogicalsTest() {
		CodePointCharStream stream = CharStreams.fromString("blood or death and cancer or hiv");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals("(logical (logical (part (term blood)) or (part (term death))) and "
				+ "(logical (part (term cancer)) or (part (term hiv))))",
				tree.toStringTree(parser));
	}

	@Test
	public void quotesTest() {
		CodePointCharStream stream = CharStreams.fromString("\"Harald zur Hausen\" and cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals("(logical (part (quotes \" Harald zur Hausen \")) and (part (term cancer)))",
				tree.toStringTree(parser));
	}

	@Test
	public void prefixedTest() {
		CodePointCharStream stream = CharStreams.fromString("author:\"Harald zur Hausen\" and cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals("(logical (part (prefixed author : (quotes \" Harald zur Hausen \"))) and (part (term cancer)))",
				tree.toStringTree(parser));
	}

}
