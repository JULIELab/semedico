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
		ParseTree parseTree = parse("\"u\" OR (x y)");
		assertEquals("(\"u\" OR (x AND y))", parseTree.toString());

		parseTree = parse("-(x y)");
		assertEquals("(NOT (x AND y))", parseTree.toString());
		
		parseTree = parse("-y");
		assertEquals("(NOT y)", parseTree.toString());
	}
	
	@Test
	public void testManipulation() throws Exception{
		ParseTree parseTree = parse("\"u\" OR (x y)");
		
		parseTree.expandTerm("x", "v");
		assertEquals("(\"u\" OR (v AND y))", parseTree.toString());
		
		parseTree.expandTerm("v", "(Tom AND Jerry)");
		assertEquals("(\"u\" OR ((Tom AND Jerry) AND y))", parseTree.toString());
		
		parseTree.remove("\"u\"");
		assertEquals("((Tom AND Jerry) AND y)", parseTree.toString());
	}
	
	private ParseTree parse(String toParse) throws Exception{
		Parser parser = new Parser(toParse);
		return parser.parse();
	}
		
}
