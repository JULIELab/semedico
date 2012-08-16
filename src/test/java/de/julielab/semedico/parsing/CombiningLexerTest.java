package de.julielab.semedico.parsing;



import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.StringReader;

import java_cup.runtime.Symbol;

import org.junit.Test;

import de.julielab.parsing.CombiningLexer;
import de.julielab.parsing.QueryTokenizer;
import de.julielab.semedico.query.QueryDisambiguationService;
import de.julielab.semedico.query.QueryDisambiguationServiceTest;


public class CombiningLexerTest {
	private CombiningLexer tokenizer;

	@Test
	public void testABC() throws Exception {
		tokenize("a b c");
		assertEquals("a", ((String[]) tokenizer.getNextToken().value)[0]);
		assertEquals("b", ((String[]) tokenizer.getNextToken().value)[0]);
		assertEquals("c", ((String[]) tokenizer.getNextToken().value)[0]);
	}
	
	@Test
	public void testAND() throws Exception {
		tokenize("a b c AND x y");
		assertEquals("a", ((String[]) tokenizer.getNextToken().value)[0]);	
		assertEquals("b", ((String[]) tokenizer.getNextToken().value)[0]);
		assertEquals("c", ((String[]) tokenizer.getNextToken().value)[0]);
		assertEquals(QueryTokenizer.AND, tokenizer.getNextToken().sym);
		assertEquals("x", ((String[]) tokenizer.getNextToken().value)[0]);
		assertEquals("y", ((String[]) tokenizer.getNextToken().value)[0]);
	}
	
	@Test
	public void testSyntax() throws Exception {
		tokenize("a NOT  b   ((    c AND x OR y");
		assertEquals("a", ((String[]) tokenizer.getNextToken().value)[0]);	
		assertEquals(QueryTokenizer.NOT, tokenizer.getNextToken().sym);
		assertEquals("b", ((String[]) tokenizer.getNextToken().value)[0]);	
		assertEquals(QueryTokenizer.LEFT_PARENTHESIS, tokenizer.getNextToken().sym);
		assertEquals(QueryTokenizer.LEFT_PARENTHESIS, tokenizer.getNextToken().sym);
		assertEquals("c", ((String[]) tokenizer.getNextToken().value)[0]);
		assertEquals(QueryTokenizer.AND, tokenizer.getNextToken().sym);
		assertEquals("x", ((String[]) tokenizer.getNextToken().value)[0]);
		assertEquals(QueryTokenizer.OR, tokenizer.getNextToken().sym);
		assertEquals("y", ((String[]) tokenizer.getNextToken().value)[0]);
	}
	
	@Test
	public void testCombining() throws Exception {
		tokenize("foo bar \"test\"");
		String[] payload = (String[]) tokenizer.getNextToken().value;
		//mapping from QueryDisambiguationServiceTest
		assertEquals("foo bar", payload[QueryDisambiguationService.TEXT]);
		assertEquals("mapped stuff", payload[QueryDisambiguationService.MAPPED_TEXT]); 
		assertEquals("\"test\"", (String) tokenizer.getNextToken().value);
	}
	
	@Test
	public void testCombiningSyntax() throws Exception {
		tokenize("foo bar (\"test\" || \"mouse\")");
		String[] payload = (String[]) tokenizer.getNextToken().value;
		//mapping from QueryDisambiguationServiceTest
		assertEquals("foo bar", payload[QueryDisambiguationService.TEXT]);
		assertEquals("mapped stuff", payload[QueryDisambiguationService.MAPPED_TEXT]); 
		assertEquals(QueryTokenizer.LEFT_PARENTHESIS, tokenizer.getNextToken().sym);
		assertEquals("\"test\"", (String) tokenizer.getNextToken().value);
		assertEquals(QueryTokenizer.OR, tokenizer.getNextToken().sym);
		assertEquals("\"mouse\"", (String) tokenizer.getNextToken().value);
		assertEquals(QueryTokenizer.RIGHT_PARENTHESIS, tokenizer.getNextToken().sym);
		assertEquals(null, tokenizer.getNextToken());
	}
	
	@Test
	public void testCombiningRelation() throws Exception {
		tokenize("foo bar Binding mouse");
		String[] payload = (String[]) tokenizer.getNextToken().value;
		//mapping from QueryDisambiguationServiceTest
		assertEquals("foo bar", payload[QueryDisambiguationService.TEXT]);
		assertEquals("mapped stuff", payload[QueryDisambiguationService.MAPPED_TEXT]); 
		Symbol s = tokenizer.getNextToken();
		assertEquals(QueryTokenizer.RELATION, s.sym);
		assertEquals("Binding", s.value);
		assertEquals("mouse", ((String[])tokenizer.getNextToken().value)[QueryDisambiguationService.TEXT]);
		assertEquals(null, tokenizer.getNextToken());
	}
	
	@Test
	public void testCombiningRelationStar() throws Exception {
		tokenize("foo bar Binding *");
		String[] payload = (String[]) tokenizer.getNextToken().value;
		//mapping from QueryDisambiguationServiceTest
		assertEquals("foo bar", payload[QueryDisambiguationService.TEXT]);
		assertEquals("mapped stuff", payload[QueryDisambiguationService.MAPPED_TEXT]); 
		Symbol s = tokenizer.getNextToken();
		assertEquals(QueryTokenizer.RELATION, s.sym);
		assertEquals("Binding", s.value);
		assertEquals("*", ((String[])tokenizer.getNextToken().value)[QueryDisambiguationService.TEXT]);
		assertEquals(null, tokenizer.getNextToken());
	}
	
	@SuppressWarnings("deprecation")
	/**
	 * marked as deprecated as it's a special testing constructor
	 */
	private void tokenize(String s) throws IOException{
		tokenizer = new CombiningLexer(new StringReader(s), QueryDisambiguationServiceTest.getMockService());
	}
}
