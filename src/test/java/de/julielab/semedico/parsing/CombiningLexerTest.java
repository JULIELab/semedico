package de.julielab.semedico.parsing;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.StringReader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Test;

import de.julielab.parsing.CombiningLexer;
import de.julielab.parsing.QueryTokenizer;


public class CombiningLexerTest {
	private CombiningLexer tokenizer;

	@Test
	public void testABC() throws Exception {
		tokenize("a b c");
		assertEquals("a", (String) tokenizer.getNextToken().value);
		assertEquals("b", (String) tokenizer.getNextToken().value);
		assertEquals("c", (String) tokenizer.getNextToken().value);
	}
	
	@Test
	public void testAND() throws Exception {
		tokenize("a b c AND x y");
		assertEquals("a", (String) tokenizer.getNextToken().value);	
		assertEquals("b", (String) tokenizer.getNextToken().value);
		assertEquals("c", (String) tokenizer.getNextToken().value);
		assertEquals(QueryTokenizer.AND, tokenizer.getNextToken().sym);
		assertEquals("x", (String) tokenizer.getNextToken().value);
		assertEquals("y", (String) tokenizer.getNextToken().value);
	}
	
	@Test
	public void testSyntax() throws Exception {
		tokenize("a NOT  b   ((    c AND x OR y");
		assertEquals("a", (String) tokenizer.getNextToken().value);	
		assertEquals(QueryTokenizer.NOT, tokenizer.getNextToken().sym);
		assertEquals("b", (String) tokenizer.getNextToken().value);	
		assertEquals(QueryTokenizer.LEFT_PARENTHESIS, tokenizer.getNextToken().sym);
		assertEquals(QueryTokenizer.LEFT_PARENTHESIS, tokenizer.getNextToken().sym);
		assertEquals("c", (String) tokenizer.getNextToken().value);
		assertEquals(QueryTokenizer.AND, tokenizer.getNextToken().sym);
		assertEquals("x", (String) tokenizer.getNextToken().value);
		assertEquals(QueryTokenizer.OR, tokenizer.getNextToken().sym);
		assertEquals("y", (String) tokenizer.getNextToken().value);
	}
	
	@Test
	public void testIL2() throws Exception {
		tokenize("IL 2");
		assertEquals("IL2", (String) tokenizer.getNextToken().value);
	}
	
	
	
	private void tokenize(String s){
		tokenizer = new CombiningLexer(new StringReader(s));
	}
}
