package de.julielab.scicopia.core.parsing;


import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
public class ParserQueryTest {

	@Test
	public void implicitNegativeConjunctionTest() {
		CodePointCharStream stream = CharStreams.fromString("blood not cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (token (term blood)) (bool (negation not (token (term cancer)))))");
	}

	@Test
	public void implicitNegativeConjunctionBlockTest() {
		CodePointCharStream stream = CharStreams.fromString("(blood not cancer)");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query ( (query (token (term blood)) (bool (negation not (token (term cancer))))) ))");
	}
	
	@Test
	public void leftBlockAndLogicalTest() {
		CodePointCharStream stream = CharStreams.fromString("(blood cancer) or death");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (bool (bool ( (query (token (term blood)) (token (term cancer))) )) or (bool (token (term death)))))");
	}
	
	@Test
	public void rightBlockAndLogicalTest() {
		CodePointCharStream stream = CharStreams.fromString("death and (blood cancer)");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (bool (bool (token (term death))) and (bool ( (query (token (term blood)) (token (term cancer))) ))))");
	}
	
	@Test
	public void termsAndRightBlockTest() {
		CodePointCharStream stream = CharStreams.fromString("lung cancer or (blood death)");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (token (term lung)) (bool (bool (token (term cancer))) or (bool ( (query (token (term blood)) (token (term death))) ))))");
	}

	@Test
	public void blockOfBlocksTest() {
		CodePointCharStream stream = CharStreams.fromString("(lung cancer) and (brain cancer)");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (bool (bool ( (query (token (term lung)) (token (term cancer))) )) and (bool ( (query (token (term brain)) (token (term cancer))) ))))");
	}

	@Test
	public void unbalancedRightParanthesisTest() {
		CodePointCharStream stream = CharStreams.fromString("blood cancer )");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (token (term blood)) (token (term cancer)))");
	}
	
	@Test
	public void unrecognizedCommaTest() {
		//Provoking: line 1:3 token recognition error at: ','
		CodePointCharStream stream = CharStreams.fromString("sex, drugs & rock 'n' roll");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (token (term sex)) (bool (bool (token (term drugs))) & (bool (token (term rock)))) (token (quotes (singlequotes ' n '))) (token (term roll)))");
	}
		

}
