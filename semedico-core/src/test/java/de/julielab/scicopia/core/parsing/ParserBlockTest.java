package de.julielab.scicopia.core.parsing;


import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
public class ParserBlockTest {

	@Test
	public void implicitNegativeConjunctionTest() {
		CodePointCharStream stream = CharStreams.fromString("blood not cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.phrase();
		assertEquals("(phrase (block (part (term blood))) (block (logical not (part (term cancer)))))"
				,tree.toStringTree(parser));
	}

	@Test
	public void implicitNegativeConjunctionBlockTest() {
		CodePointCharStream stream = CharStreams.fromString("(blood not cancer)");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.phrase();
		assertEquals("(phrase (block ( (block (part (term blood))) (block (logical not (part (term cancer)))) )))"
				,tree.toStringTree(parser));
	}
	
	@Test
	public void leftBlockAndLogicalTest() {
		CodePointCharStream stream = CharStreams.fromString("(blood cancer) or death");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.block();
		assertEquals("(block ( (block (part (term blood)) (part (term cancer))) ) or (part (term death)))"
				,tree.toStringTree(parser));
	}
	
	@Test
	public void rightBlockAndLogicalTest() {
		CodePointCharStream stream = CharStreams.fromString("death and (blood cancer)");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.block();
		assertEquals("(block (part (term death)) and ( (block (part (term blood)) (part (term cancer))) ))"
				,tree.toStringTree(parser));
	}
	
	@Test
	public void termsAndRightBlockTest() {
		CodePointCharStream stream = CharStreams.fromString("lung cancer or (blood death)");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.block();
		assertEquals("(block (part (term lung)) (part (term cancer)) or ( (block (part (term blood)) (part (term death))) ))"
				,tree.toStringTree(parser));
	}

	@Test
	public void blockOfBlocksTest() {
		CodePointCharStream stream = CharStreams.fromString("(lung cancer) and (brain cancer)");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.block();
		assertEquals("(block ( (block (part (term lung)) (part (term cancer))) ) and (block ( (block (part (term brain)) (part (term cancer))) )))",
				tree.toStringTree(parser));
	}

	@Test
	public void unbalancedLeftParanthesisTest() {
		//The unbalanced parenthesis will lead to an empty block
		CodePointCharStream stream = CharStreams.fromString("( blood cancer");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.phrase();
		assertEquals("(phrase block (block () (block (part (term blood)) (part (term cancer))))",
				tree.toStringTree(parser));
	}

	@Test
	public void unbalancedRightParanthesisTest() {
		CodePointCharStream stream = CharStreams.fromString("blood cancer )");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.block();
		assertEquals("(block (part (term blood)) (part (term cancer)))",
				tree.toStringTree(parser));
	}
	
	@Test
	public void unrecognizedCommaTest() {
		//Provoking: line 1:3 token recognition error at: ','
		CodePointCharStream stream = CharStreams.fromString("sex, drugs & rock 'n' roll");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.phrase();
		assertEquals("(phrase (block (part (term sex))) "
				+ "(block (logical (part (term drugs)) & "
				+ "(part (term rock)))) (block (part (quotes ' n ')) (part (term roll))))",
				tree.toStringTree(parser));
	}
		

}
