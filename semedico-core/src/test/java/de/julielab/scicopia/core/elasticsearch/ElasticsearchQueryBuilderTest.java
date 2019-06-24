package de.julielab.scicopia.core.elasticsearch;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facetterms.FacetTerm;
import de.julielab.semedico.core.services.BaseConceptService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;

import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.services.ServiceOverride;
import org.easymock.EasyMock;
import org.easymock.IMockBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import de.julielab.scicopia.core.parsing.DisambiguatingRangeChunker;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import de.julielab.semedico.core.services.TokenInputService;
import de.julielab.semedico.core.services.interfaces.IStopWordService;
import de.julielab.semedico.core.services.interfaces.ITermService;

@ImportModule(SemedicoCoreTestModule.class)
public class ElasticsearchQueryBuilderTest {

	ElasticsearchQueryBuilder builder;
	final static Logger log = LoggerFactory.getLogger(ElasticsearchQueryBuilderTest.class);
	IStopWordService stopwordService;
	private DisambiguatingRangeChunker chunker;
	private ITermService termService;
	
    @ImportModule(SemedicoCoreTestModule.class)
    public static class ElasticsearchQueryBuilderTestModule {
        @Contribute(ServiceOverride.class)
        public void overrideConceptService(MappedConfiguration<Class, Object> configuration) {
            final ITermService termService = EasyMock.createStrictMock(ITermService.class);
            final FacetTerm ft = new FacetTerm("tid56");
            final Facet f = new Facet("fid1", "TestFacet");
            ft.setFacets(Arrays.asList(f));
            EasyMock.expect(termService.getTermSynchronously("tid56")).andReturn(ft);
            EasyMock.replay(termService);
            configuration.add(ITermService.class, termService);
        }

        public static void bind(ServiceBinder binder) {
            binder.bind(ITokenInputService.class, TokenInputService.class);
        }
    }

	
	@Before
	public void setup() {
		Registry registry = TestUtils.createTestRegistry(ElasticsearchQueryBuilderTestModule.class);
		this.stopwordService = registry.getService(IStopWordService.class);
		Multimap<String, String> dictionary = MultimapBuilder.hashKeys(5).arrayListValues(10).build();
        dictionary.put("and", "B");
        dictionary.put("breast-cancer", "BC");
		this.chunker = new DisambiguatingRangeChunker(dictionary);
        final IMockBuilder<BaseConceptService> builder = EasyMock.createMockBuilder(BaseConceptService.class);
        builder.addMockedMethod("getTermSynchronously");
        this.termService = builder.createMock();
        final Facet f = new Facet("fid1", "TestFacet");
        final FacetTerm b = new FacetTerm("B", "BREAST");
        b.addFacet(f);
        EasyMock.expect(termService.getTermSynchronously("B")).andReturn(b).anyTimes();
        final FacetTerm bc = new FacetTerm("BC", "BREAST_CANCER");
        bc.addFacet(f);
        EasyMock.expect(termService.getTermSynchronously("BC")).andReturn(bc).anyTimes();
        EasyMock.replay(termService);
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
		qt1.setInputTokenType(ITokenInputService.TokenType.CONCEPT);
		qt1.setTermList(Arrays.asList(new FacetTerm("C1", "BONASAI_CONCEPT")));
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
