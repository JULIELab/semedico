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
		assertEquals(tree.toStringTree(parser), "(query (tokensequence (token (term blood))) (bool (negation not (token (term cancer)))))"
				);
	}

	@Test
	public void implicitNegativeConjunctionBlockTest() {
		CodePointCharStream stream = CharStreams.fromString("(blood not cancer)");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (bool (parenQuery ( (query (tokensequence (token (term blood))) (bool (negation not (token (term cancer))))) ))))");
	}
	
	@Test
	public void leftBlockAndLogicalTest() {
		CodePointCharStream stream = CharStreams.fromString("(blood cancer) or death");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (query ( (query (part (term blood)) (part (term cancer))) )) or (query (part (term death))))");
	}
	
	@Test
	public void rightBlockAndLogicalTest() {
		CodePointCharStream stream = CharStreams.fromString("death and (blood cancer)");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (query (part (term death))) and (query ( (query (part (term blood)) (part (term cancer))) )))");
	}
	
	@Test
	public void termsAndRightBlockTest() {
		CodePointCharStream stream = CharStreams.fromString("lung cancer or (blood death)");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (query (part (term lung)) (part (term cancer))) or (query ( (query (part (term blood)) (part (term death))) )))");
	}

	@Test
	public void blockOfBlocksTest() {
		CodePointCharStream stream = CharStreams.fromString("(lung cancer) and (brain cancer)");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (query ( (query (part (term lung)) (part (term cancer))) )) and (query ( (query (part (term brain)) (part (term cancer))) )))");
	}

	@Test
	public void unbalancedRightParanthesisTest() {
		CodePointCharStream stream = CharStreams.fromString("blood cancer )");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (part (term blood)) (part (term cancer)))");
	}
	
	@Test
	public void unrecognizedCommaTest() {
		//Provoking: line 1:3 token recognition error at: ','
		CodePointCharStream stream = CharStreams.fromString("sex, drugs & rock 'n' roll");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals(tree.toStringTree(parser), "(query (query (part (term sex)) (part (term drugs))) & (query (part (term rock)) (part (quotes ' n ')) (part (term roll))))");
	}
		

}
