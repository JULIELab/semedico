package de.julielab.semedico.core.services.query;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facetterms.KeywordTerm;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.services.StopWordService;
import de.julielab.scicopia.core.parsing.LegacyLexerService;
import de.julielab.semedico.core.services.interfaces.ILexerService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

public class StopwordRemovalTest {

	StopWordService filter;
	ILexerService lexer;
	
	@Before
	public void setup() {
		filter = new StopWordService(LoggerFactory.getLogger(StopWordService.class), "src/test/resources/stopwords.txt");
		filter.loadStopWords();
		lexer = new LegacyLexerService();
	}
	
	@Test
	public void test() throws IOException {
		String test = "any protein";
		List<QueryToken> tokens = lexer.lex(test);
		List<QueryToken> filtered = filter.filterStopTokens(tokens);
		assertEquals(1, filtered.size());
	}

	@Test
	public void leftParenthesisImmediatelyTest() throws IOException {
		String test = "any (protein";
		List<QueryToken> tokens = lexer.lex(test);
		List<QueryToken> filtered = filter.filterStopTokens(tokens);
		assertEquals(2, filtered.size());
	}

	@Test
	public void leftParenthesisTest() throws IOException {
		String test = "any ( protein";
		List<QueryToken> tokens = lexer.lex(test);
		List<QueryToken> filtered = filter.filterStopTokens(tokens);
		assertEquals(2, filtered.size());
	}

	@Test
	public void rightParenthesisImmediatelyTest() throws IOException {
		String test = "any protein)";
		List<QueryToken> tokens = lexer.lex(test);
		List<QueryToken> filtered = filter.filterStopTokens(tokens);
		assertEquals(2, filtered.size());
	}

	@Test
	public void rightParenthesisTest() throws IOException {
		String test = "any protein )";
		List<QueryToken> tokens = lexer.lex(test);
		List<QueryToken> filtered = filter.filterStopTokens(tokens);
		assertEquals(2, filtered.size());
	}
	
	@Test
	public void andTest() throws IOException {
		String test = "& any protein";
		List<QueryToken> tokens = lexer.lex(test);
		List<QueryToken> filtered = filter.filterStopTokens(tokens);
		assertEquals(2, filtered.size());
	}

	@Test
	public void orTest() throws IOException {
		String test = "OR any protein";
		List<QueryToken> tokens = lexer.lex(test);
		List<QueryToken> filtered = filter.filterStopTokens(tokens);
		assertEquals(2, filtered.size());
	}

	@Test
	public void notTest() throws IOException {
		String test = "Not any protein";
		List<QueryToken> tokens = lexer.lex(test);
		List<QueryToken> filtered = filter.filterStopTokens(tokens);
		assertEquals(2, filtered.size());
	}

	@Test
	public void conceptTest() throws IOException {
		List<QueryToken> tokens = new ArrayList<>();
		QueryToken well = new QueryToken(0, 4, "well");
		well.setInputTokenType(TokenType.CONCEPT);
		IConcept concept = new KeywordTerm("tid500", "well");
		well.addTermToList(concept);
		tokens.add(well);
		tokens.add(new QueryToken(5, 11, "boring"));
		List<QueryToken> filtered = filter.filterStopTokens(tokens);
		System.out.println(filtered.stream().map(QueryToken::toString).collect(Collectors.joining(", ")));
		assertEquals(2, filtered.size());
	}

}
