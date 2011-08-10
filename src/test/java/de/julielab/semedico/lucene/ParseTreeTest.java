package de.julielab.semedico.lucene;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.junit.Test;

import de.julielab.lucene.ParseTree;
import de.julielab.lucene.Parser;

/**
 * Some simple tests for the ParseTree and Parser.
 * 
 * @author hellrich
 *
 */
public class ParseTreeTest {
	
	@Test
	public void testSimpleParse() throws Exception{
		String toParse = "\"u\" OR (x y)";
		Parser parser = new Parser(toParse);
		ParseTree parseTree = parser.parse();
		assertEquals("(\"u\" OR (x AND y))", parseTree.toString());
	}
	
	@Test
	public void testManipulation() throws Exception{
		String toParse = "\"u\" OR (x y)";
		Parser parser = new Parser(toParse);
		ParseTree parseTree = parser.parse();
		
		parseTree.expandTerm("x", "v");
		assertEquals("(\"u\" OR (v AND y))", parseTree.toString());
		
		parseTree.expandTerm("v", "(Tom AND Jerry)");
		assertEquals("(\"u\" OR ((Tom AND Jerry) AND y))", parseTree.toString());
	}
		
}
