package de.julielab.scicopia.core.parsing;


import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
public class ParserRelationTest {

	@Test
	public void simpleRelationTest() {
		CodePointCharStream stream = CharStreams.fromString("blood -> cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (token (relation (term blood) -> (term cancer))))");
	}

	@Test
	public void quotesFirstRelationTest() {
		CodePointCharStream stream = CharStreams.fromString("'Harald zur Hausen' -> cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (token (relation (quotes (singlequotes ' Harald zur Hausen ')) -> (term cancer))))");
	}

	@Test
	public void quotesSecondRelationTest() {
		CodePointCharStream stream = CharStreams.fromString("cancer <- 'Harald zur Hausen'");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (token (relation (term cancer) <- (quotes (singlequotes ' Harald zur Hausen ')))))");
	}

	@Test
	public void quotedRelationTest() {
		CodePointCharStream stream = CharStreams.fromString("\"Allan McLeod Cormack\" <--> 'Harald zur Hausen'");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (token (relation (quotes (doublequotes \" Allan McLeod Cormack \")) <--> (quotes (singlequotes ' Harald zur Hausen ')))))");
	}

	@Test
	public void chargedRelationWithoutSpaceTest() {
		CodePointCharStream stream = CharStreams.fromString("BrO4-->BrO3-");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (token (relation (term BrO4) --> (term (charged BrO3 -)))))");
	}

	@Test
	public void chargedRelationWithSpaceTest() {
		CodePointCharStream stream = CharStreams.fromString("BrO4- -> BrO3-");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (token (relation (term (charged BrO4 -)) -> (term (charged BrO3 -)))))");
	}

}
