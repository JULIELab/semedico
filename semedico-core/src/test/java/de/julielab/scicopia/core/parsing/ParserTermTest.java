package de.julielab.scicopia.core.parsing;


import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
public class ParserTermTest {

	@Test
	public void dashTest() {
		CodePointCharStream stream = CharStreams.fromString("methyl p-toluate");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals( tree.toStringTree(parser), "(query (token (term methyl)) (token (term p-toluate)))");
	}

	@Test
	public void numTest() {
		CodePointCharStream stream = CharStreams.fromString("1,1,1-trichloroethane");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.term();
		assertEquals(tree.toStringTree(parser), "(term 1,1,1-trichloroethane)");
	}

	@Test
	public void compoundTest() {
		CodePointCharStream stream = CharStreams.fromString("cis-Muurola-4(14),5-diene");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.term();
		assertEquals(tree.toStringTree(parser), "(term cis-Muurola-4(14),5-diene)");
	}

	@Test
	public void alphaTest() {
		CodePointCharStream stream = CharStreams.fromString("Benzene");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.term();
		assertEquals(tree.toStringTree(parser), "(term Benzene)");
	}

	@Test
	public void abbrevTest() {
		CodePointCharStream stream = CharStreams.fromString("E. coli");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.query();
		assertEquals( tree.toStringTree(parser), "(query (token (term E.)) (token (term coli)))");
	}

	@Test
	public void apostropheTest() {
		CodePointCharStream stream = CharStreams.fromString("O'Reilly");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.term();
		assertEquals(tree.toStringTree(parser), "(term O'Reilly)");
	}

	@Test
	public void iriTest() {
		CodePointCharStream stream = CharStreams.fromString("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C1908");
		ScicopiaLexer lexer = new ScicopiaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ScicopiaParser parser = new ScicopiaParser(tokens);
		ParseTree tree = parser.token();
		assertEquals(tree.toStringTree(parser), "(token http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C1908)");
	}

}
