package de.julielab.scicopia.core.elasticsearch;


import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.julielab.scicopia.core.parsing.DisambiguatingRangeChunker;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.DatabaseConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import de.julielab.semedico.core.services.interfaces.IStopWordService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.query.TokenInputService;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.services.ServiceOverride;
import org.easymock.EasyMock;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;


public class ElasticsearchQueryBuilderTest {

	ElasticsearchQueryBuilder builder;
	final static Logger log = LoggerFactory.getLogger(ElasticsearchQueryBuilderTest.class);
	IStopWordService stopwordService;
	private DisambiguatingRangeChunker chunker;
	private IConceptService termService;
	
    @ImportModule(SemedicoCoreTestModule.class)
    public static class ElasticsearchQueryBuilderTestModule {
        @Contribute(ServiceOverride.class)
        public void overrideConceptService(MappedConfiguration<Class, Object> configuration) {
            final IConceptService termService = EasyMock.createStrictMock(IConceptService.class);
            final DatabaseConcept ft = new DatabaseConcept("tid56");
            final Facet f = new Facet("fid1", "TestFacet");
            ft.setFacets(Arrays.asList(f));
            EasyMock.expect(termService.getTermSynchronously("tid56")).andReturn(ft);
            EasyMock.replay(termService);
            configuration.add(IConceptService.class, termService);
        }

        public static void bind(ServiceBinder binder) {
            binder.bind(ITokenInputService.class, TokenInputService.class);
        }
    }


	@BeforeClass
	public void setup() {
		Registry registry = TestUtils.createTestRegistry(ElasticsearchQueryBuilderTestModule.class);
		this.stopwordService = registry.getService(IStopWordService.class);
		Multimap<String, String> dictionary = MultimapBuilder.hashKeys(5).arrayListValues(10).build();
		this.chunker = new DisambiguatingRangeChunker(dictionary);
		this.termService = null;
		this.builder = new ElasticsearchQueryBuilder(log, stopwordService, chunker, termService);

	}
	
	@Test
	public void testPrefixQuery() {
		QueryToken token1 = new QueryToken("author:death");
		List<QueryToken> tokens = new ArrayList<>();
		tokens.add(token1);
		QueryBuilder query = builder.analyseQueryString(tokens);
		assertEquals(MatchQueryBuilder.class, query.getClass());
	}
	
	@Test
	public void testPrefixQuerySettings() {
		QueryToken token1 = new QueryToken("author:death");
		List<QueryToken> tokens = new ArrayList<>();
		tokens.add(token1);
		MatchQueryBuilder query = (MatchQueryBuilder) builder.analyseQueryString(tokens);
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

	@Test
	public void testListener() {
		final Registry testRegistry = TestUtils.createTestRegistry();
		final IElasticsearchQueryBuilder qb = testRegistry.getService(IElasticsearchQueryBuilder.class);
		final QueryToken qt1 = new QueryToken(0, 6, "bonsai");
		qt1.setType(QueryToken.Category.ALPHA);
		qt1.setInputTokenType(ITokenInputService.TokenType.KEYWORD);
		final QueryToken qt2 = new QueryToken(7, 10, "and");
		qt2.setType(QueryToken.Category.AND);
		qt2.setInputTokenType(ITokenInputService.TokenType.AND);
		final QueryToken qt3 = new QueryToken(11, 12, "(");
		qt3.setType(QueryToken.Category.LPAR);
		qt3.setInputTokenType(ITokenInputService.TokenType.LEFT_PARENTHESIS);
		final QueryToken qt4 = new QueryToken(12, 17, "tulip");
		qt4.setType(QueryToken.Category.ALPHA);
		qt4.setInputTokenType(ITokenInputService.TokenType.KEYWORD);
		final QueryToken qt5 = new QueryToken(18, 21, "or");
		qt5.setType(QueryToken.Category.OR);
		qt5.setInputTokenType(ITokenInputService.TokenType.OR);
		final QueryToken qt6 = new QueryToken(22, 26, "rose");
		qt6.setType(QueryToken.Category.ALPHA);
		qt6.setInputTokenType(ITokenInputService.TokenType.KEYWORD);
		final QueryToken qt7 = new QueryToken(26, 27, ")");
		qt7.setType(QueryToken.Category.RPAR);
		qt7.setInputTokenType(ITokenInputService.TokenType.RIGHT_PARENTHESIS);
		final QueryBuilder queryBuilder = qb.analyseQueryString(new ArrayList<>(Arrays.asList(qt1, qt2, qt3, qt4, qt5, qt6, qt7)));
		System.out.println(queryBuilder.toString());
	}

}
