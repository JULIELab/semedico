package de.julielab.scicopia.core.elasticsearch;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.julielab.semedico.core.search.query.QueryToken;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.scicopia.core.parsing.DisambiguatingRangeChunker;
import de.julielab.semedico.core.services.StopWordService;
import de.julielab.semedico.core.services.interfaces.IConceptService;


public class ElasticsearchQueryBuilderTest {

	ElasticsearchQueryBuilder builder;
	final static Logger log = LoggerFactory.getLogger(ElasticsearchQueryBuilderTest.class);
	StopWordService stopwordService;
	private DisambiguatingRangeChunker chunker;
	private IConceptService termService;
	
	@Before
	public void setup() {
//		this.stopwordService = new StopWordService(log, new File("src/test/resources/test_stopwords.txt"));
		this.chunker = null;
		this.termService = null;
		this.builder = new ElasticsearchQueryBuilder(log, stopwordService, chunker, termService);

	}
	
	@Test
	public void testPrefixQuery() {
		QueryToken token1 = new QueryToken("author:death");
		List<QueryToken> tokens = new ArrayList<>();
		tokens.add(token1);
		QueryBuilder query = builder.analyseQueryString(tokens);
		assertEquals(TermQueryBuilder.class, query.getClass());
	}
	
	@Test
	public void testPrefixQuerySettings() {
		QueryToken token1 = new QueryToken("author:death");
		List<QueryToken> tokens = new ArrayList<>();
		tokens.add(token1);
		TermQueryBuilder query = (TermQueryBuilder) builder.analyseQueryString(tokens);
		String field = query.fieldName();
		assertEquals("authors", field);
	}

	@Test
	public void testTerm() {
		QueryToken token1 = new QueryToken("death");
		List<QueryToken> tokens = new ArrayList<>();
		tokens.add(token1);
		QueryBuilder query = builder.analyseQueryString(tokens);
		assertEquals("multi_match", query.getName());
	}

	@Test
	public void testImplicitShould() {
		QueryToken token = new QueryToken("blood->cancer title:death");
		List<QueryToken> tokens = new ArrayList<>();
		tokens.add(token);
		BoolQueryBuilder query = (BoolQueryBuilder) builder.analyseQueryString(tokens);
		assertEquals(2, query.must().size());
	}

}
