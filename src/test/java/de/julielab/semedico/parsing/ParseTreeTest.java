package de.julielab.semedico.parsing;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.junit.Test;

import de.julielab.parsing.ParseTree;
import de.julielab.parsing.Parser;
import de.julielab.semedico.query.IQueryDisambiguationService;
import de.julielab.semedico.query.QueryDisambiguationService;

/**
 * Some simple tests for the ParseTree and Parser.
 * 
 * @author hellrich
 *
 */
public class ParseTreeTest {
	static Registry registryIoC;

	
	@Test
	public void testSimpleParse() throws Exception{
	
		/**
		 // Try to build a custom IoC registry for tests
		// --> Runtime Exception, unrecognized public methods
		registryIoC = RegistryBuilder.buildAndStartupRegistry(IQueryDisambiguationService.class, QueryDisambiguationService.class);
	   **/
		
		ParseTree parseTree = parse("\"u\" OR (x y)");
		assertEquals("(\"u\" OR (x AND y))", parseTree.toString());

		parseTree = parse("- (x y)");
		assertEquals("(NOT (x AND y))", parseTree.toString());
		
		parseTree = parse("-y");
		assertEquals("(NOT y)", parseTree.toString());
		
		parseTree = parse("-y AND x");
		assertEquals("((NOT y) AND x)", parseTree.toString());
		
		parseTree = parse("y Binding x");
		assertEquals("(y Binding x)", parseTree.toString());
		
		parseTree = parse("-y Binding x");
		assertEquals("((NOT y) Binding x)", parseTree.toString());
		
		parseTree = parse("\"foo\" OR NOT bar");
		assertEquals("(\"foo\" OR (NOT bar))", parseTree.toString());
		
		parseTree = parse("x y z AND (\"foo\" OR NOT bar)");
		assertEquals("(((x AND y) AND z) AND (\"foo\" OR (NOT bar)))", parseTree.toString());
		
		parseTree = parse("((x Or (y or z)");	//left parentheses errors are repaired
		assertEquals("(x OR (y OR z))", parseTree.toString());
		
		parseTree = parse("(x Or (y or (1 AND 2");	//right parentheses errors too!
		assertEquals("(x OR (y OR (1 AND 2)))", parseTree.toString());
		
		parseTree = parse("NOT(x AND y)OR c");	//lexer has special rules for tokens without whitespaces
		assertEquals("(NOT (x AND y)) OR c", parseTree.toString());
	}
	
	@Test
	public void testManipulation() throws Exception{
		/**
		// Try to build a custom IoC registry for tests
		// --> Runtime Exception, unrecognized public methods
		registryIoC = RegistryBuilder.buildAndStartupRegistry(QueryDisambiguationService.class);
		*/
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
