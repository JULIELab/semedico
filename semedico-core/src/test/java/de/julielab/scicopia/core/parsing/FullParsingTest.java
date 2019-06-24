package de.julielab.scicopia.core.parsing;

import de.julielab.scicopia.core.elasticsearch.legacy.ISearchComponent;
import de.julielab.scicopia.core.search.components.ElasticsearchQueryComponent;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facetterms.FacetTerm;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.search.components.QueryAnalysisComponent;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCommand;
import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import de.julielab.semedico.core.services.TokenInputService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.services.ServiceOverride;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.easymock.EasyMock;
import org.elasticsearch.index.query.QueryBuilder;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * This class tests user input parsing from the input tokens to the ES query.
 */
public class FullParsingTest {


    @Test
    public void testFullParsingFreetextExpression() {
        Registry registry = TestUtils.createTestRegistry(FullParsingTestModule.class);
        final ITokenInputService tokenInputService = registry.getService(ITokenInputService.class);
        final ISearchComponent queryAnalysisComponent = registry.getService(ISearchComponent.class, QueryAnalysisComponent.QueryAnalysis.class);
        final ISearchComponent esQueryComponent = registry.getService(ISearchComponent.class, ElasticsearchQueryComponent.ElasticsearchQuery.class);

        final JSONArray tokens = new JSONArray();
        final JSONObject token = new JSONObject();
        token.put(ITokenInputService.NAME, "aquifer");
        token.put(ITokenInputService.TOKEN_TYPE, ITokenInputService.TokenType.FREETEXT.name());
        tokens.put(token);

        final List<QueryToken> queryTokens = tokenInputService.convertToQueryTokens(tokens);

        final SemedicoSearchCarrier semedicoSearchCarrier = new SemedicoSearchCarrier("FullParsingTestChain");
        semedicoSearchCarrier.setSearchState(new SearchState());
        semedicoSearchCarrier.setUserQuery(queryTokens);
        semedicoSearchCarrier.setSearchCommand(new SemedicoSearchCommand());
        esQueryComponent.process(semedicoSearchCarrier);
        queryAnalysisComponent.process(semedicoSearchCarrier);

        assertEquals(StringUtils.normalizeSpace("{\n" +
                "  \"multi_match\" : {\n" +
                "    \"query\" : \"aquifer\",\n" +
                "    \"fields\" : [\n" +
                "      \"alltext^1.0\",\n" +
                "      \"docmeta^1.0\",\n" +
                "      \"mesh^1.0\"\n" +
                "    ],\n" +
                "    \"type\" : \"best_fields\",\n" +
                "    \"operator\" : \"OR\",\n" +
                "    \"slop\" : 0,\n" +
                "    \"prefix_length\" : 0,\n" +
                "    \"max_expansions\" : 50,\n" +
                "    \"lenient\" : false,\n" +
                "    \"zero_terms_query\" : \"NONE\",\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}"), StringUtils.normalizeSpace(semedicoSearchCarrier.serverCmds.get(0).query.toString()));
        registry.shutdown();
    }

    @Test
    public void testFullParsingConcept() {
        Registry registry = TestUtils.createTestRegistry(FullParsingTestModuleAquiferConcept.class);
        final ITokenInputService tokenInputService = registry.getService(ITokenInputService.class);
        final ISearchComponent queryAnalysisComponent = registry.getService(ISearchComponent.class, QueryAnalysisComponent.QueryAnalysis.class);
        final ISearchComponent esQueryComponent = registry.getService(ISearchComponent.class, ElasticsearchQueryComponent.ElasticsearchQuery.class);

        final JSONArray tokens = new JSONArray();
        final JSONObject token = new JSONObject();
        token.put(ITokenInputService.NAME, "aquifer");
        token.put(ITokenInputService.TOKEN_TYPE, ITokenInputService.TokenType.CONCEPT.name());
        token.put(ITokenInputService.TERM_ID, "tid1");
        tokens.put(token);

        final List<QueryToken> queryTokens = tokenInputService.convertToQueryTokens(tokens);

        final SemedicoSearchCarrier semedicoSearchCarrier = new SemedicoSearchCarrier("FullParsingTestChain");
        semedicoSearchCarrier.setSearchState(new SearchState());
        semedicoSearchCarrier.setUserQuery(queryTokens);
        semedicoSearchCarrier.setSearchCommand(new SemedicoSearchCommand());
        esQueryComponent.process(semedicoSearchCarrier);
        queryAnalysisComponent.process(semedicoSearchCarrier);

        assertEquals(StringUtils.normalizeSpace("{\n" +
                "  \"bool\" : {\n" +
                "    \"should\" : [\n" +
                "      {\n" +
                "        \"multi_match\" : {\n" +
                "          \"query\" : \"aquifer\",\n" +
                "          \"fields\" : [\n" +
                "            \"alltext^1.0\",\n" +
                "            \"docmeta^1.0\",\n" +
                "            \"mesh^1.0\"\n" +
                "          ],\n" +
                "          \"type\" : \"best_fields\",\n" +
                "          \"operator\" : \"OR\",\n" +
                "          \"slop\" : 0,\n" +
                "          \"prefix_length\" : 0,\n" +
                "          \"max_expansions\" : 50,\n" +
                "          \"lenient\" : false,\n" +
                "          \"zero_terms_query\" : \"NONE\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"term\" : {\n" +
                "          \"alltext\" : {\n" +
                "            \"value\" : \"tid1\",\n" +
                "            \"boost\" : 1.0\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"term\" : {\n" +
                "          \"mesh\" : {\n" +
                "            \"value\" : \"tid1\",\n" +
                "            \"boost\" : 1.0\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"disable_coord\" : false,\n" +
                "    \"adjust_pure_negative\" : true,\n" +
                "    \"minimum_should_match\" : \"1\",\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}"), StringUtils.normalizeSpace(semedicoSearchCarrier.serverCmds.get(0).query.toString()));
        registry.shutdown();
    }

    @Test
    public void testFullParsingConceptRecognition() {
        Registry registry = TestUtils.createTestRegistry(FullParsingTestModule.class);
        final ITokenInputService tokenInputService = registry.getService(ITokenInputService.class);
        final ISearchComponent queryAnalysisComponent = registry.getService(ISearchComponent.class, QueryAnalysisComponent.QueryAnalysis.class);
        final ISearchComponent esQueryComponent = registry.getService(ISearchComponent.class, ElasticsearchQueryComponent.ElasticsearchQuery.class);

        final JSONArray tokens = new JSONArray();
        final JSONObject token = new JSONObject();
        token.put(ITokenInputService.NAME, "Cell Survival");
        token.put(ITokenInputService.TOKEN_TYPE, ITokenInputService.TokenType.FREETEXT.name());
        tokens.put(token);

        final List<QueryToken> queryTokens = tokenInputService.convertToQueryTokens(tokens);

        final QueryToken qt = new QueryToken(0, 7, "Cell Survival");
        qt.setType(QueryToken.Category.ALPHA);

        final SemedicoSearchCarrier semedicoSearchCarrier = new SemedicoSearchCarrier("FullParsingTestChain");
        semedicoSearchCarrier.setSearchState(new SearchState());
        semedicoSearchCarrier.setUserQuery(queryTokens);
        semedicoSearchCarrier.setSearchCommand(new SemedicoSearchCommand());
        esQueryComponent.process(semedicoSearchCarrier);
        queryAnalysisComponent.process(semedicoSearchCarrier);

        final QueryBuilder query = semedicoSearchCarrier.serverCmds.get(0).query;
        assertEquals(StringUtils.normalizeSpace("{\n" +
                "  \"bool\" : {\n" +
                "    \"should\" : [\n" +
                "      {\n" +
                "        \"multi_match\" : {\n" +
                "          \"query\" : \"Cell Survival\",\n" +
                "          \"fields\" : [\n" +
                "            \"alltext^1.0\",\n" +
                "            \"docmeta^1.0\",\n" +
                "            \"mesh^1.0\"\n" +
                "          ],\n" +
                "          \"type\" : \"best_fields\",\n" +
                "          \"operator\" : \"OR\",\n" +
                "          \"slop\" : 0,\n" +
                "          \"prefix_length\" : 0,\n" +
                "          \"max_expansions\" : 50,\n" +
                "          \"lenient\" : false,\n" +
                "          \"zero_terms_query\" : \"NONE\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"term\" : {\n" +
                "          \"alltext\" : {\n" +
                "            \"value\" : \"tid56\",\n" +
                "            \"boost\" : 1.0\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"term\" : {\n" +
                "          \"mesh\" : {\n" +
                "            \"value\" : \"tid56\",\n" +
                "            \"boost\" : 1.0\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"disable_coord\" : false,\n" +
                "    \"adjust_pure_negative\" : true,\n" +
                "    \"minimum_should_match\" : \"1\",\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}"), StringUtils.normalizeSpace(query.toString()));
        registry.shutdown();
    }

    @Test
    public void testFullParsingBooleanConceptRecognition1() {
        Registry registry = TestUtils.createTestRegistry(FullParsingTestModule.class);
        final ITokenInputService tokenInputService = registry.getService(ITokenInputService.class);
        final ISearchComponent queryAnalysisComponent = registry.getService(ISearchComponent.class, QueryAnalysisComponent.QueryAnalysis.class);
        final ISearchComponent esQueryComponent = registry.getService(ISearchComponent.class, ElasticsearchQueryComponent.ElasticsearchQuery.class);

        final JSONArray tokens = new JSONArray();
        final JSONObject token = new JSONObject();
        token.put(ITokenInputService.NAME, "Cell Survival and frap");
        token.put(ITokenInputService.TOKEN_TYPE, ITokenInputService.TokenType.FREETEXT.name());
        tokens.put(token);

        final List<QueryToken> queryTokens = tokenInputService.convertToQueryTokens(tokens);


        final SemedicoSearchCarrier semedicoSearchCarrier = new SemedicoSearchCarrier("FullParsingTestChain");
        semedicoSearchCarrier.setSearchState(new SearchState());
        semedicoSearchCarrier.setUserQuery(queryTokens);
        semedicoSearchCarrier.setSearchCommand(new SemedicoSearchCommand());
        esQueryComponent.process(semedicoSearchCarrier);
        queryAnalysisComponent.process(semedicoSearchCarrier);
        final QueryBuilder query = semedicoSearchCarrier.serverCmds.get(0).query;
        assertTrue("The term 'frap' is missing from the query.", query.toString().contains("frap"));
        assertTrue("The concept ID 'tid56' is missing from the query.", query.toString().contains("tid56"));
        // I am actually not sure that the query should look exactly like this. But at least there should be "frap" and "tid56" in it
        // because that is the ID for "Cell Survival" in the test dictionary.
        assertEquals("The produced query does not match the expected one", StringUtils.normalizeSpace("{\n" +
                "  \"bool\" : {\n" +
                "    \"must\" : [\n" +
                "      {\n" +
                "        \"multi_match\" : {\n" +
                "          \"query\" : \"frap\",\n" +
                "          \"fields\" : [\n" +
                "            \"alltext^1.0\",\n" +
                "            \"docmeta^1.0\",\n" +
                "            \"mesh^1.0\"\n" +
                "          ],\n" +
                "          \"type\" : \"best_fields\",\n" +
                "          \"operator\" : \"OR\",\n" +
                "          \"slop\" : 0,\n" +
                "          \"prefix_length\" : 0,\n" +
                "          \"max_expansions\" : 50,\n" +
                "          \"lenient\" : false,\n" +
                "          \"zero_terms_query\" : \"NONE\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"bool\" : {\n" +
                "          \"should\" : [\n" +
                "            {\n" +
                "              \"multi_match\" : {\n" +
                "                \"query\" : \"Cell Survival\",\n" +
                "                \"fields\" : [\n" +
                "                  \"alltext^1.0\",\n" +
                "                  \"docmeta^1.0\",\n" +
                "                  \"mesh^1.0\"\n" +
                "                ],\n" +
                "                \"type\" : \"best_fields\",\n" +
                "                \"operator\" : \"OR\",\n" +
                "                \"slop\" : 0,\n" +
                "                \"prefix_length\" : 0,\n" +
                "                \"max_expansions\" : 50,\n" +
                "                \"lenient\" : false,\n" +
                "                \"zero_terms_query\" : \"NONE\",\n" +
                "                \"boost\" : 1.0\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"term\" : {\n" +
                "                \"alltext\" : {\n" +
                "                  \"value\" : \"tid56\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"term\" : {\n" +
                "                \"mesh\" : {\n" +
                "                  \"value\" : \"tid56\",\n" +
                "                  \"boost\" : 1.0\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"disable_coord\" : false,\n" +
                "          \"adjust_pure_negative\" : true,\n" +
                "          \"minimum_should_match\" : \"1\",\n" +
                "          \"boost\" : 1.0\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"disable_coord\" : false,\n" +
                "    \"adjust_pure_negative\" : true,\n" +
                "    \"boost\" : 1.0\n" +
                "  }\n" +
                "}"), StringUtils.normalizeSpace(query.toString()));
        registry.shutdown();
    }

    @Test
    public void testPhrase() {
        Registry registry = TestUtils.createTestRegistry(FullParsingTestModule.class);
        final ITokenInputService tokenInputService = registry.getService(ITokenInputService.class);
        final ISearchComponent esQueryComponent = registry.getService(ISearchComponent.class, ElasticsearchQueryComponent.ElasticsearchQuery.class);

        final JSONArray tokens = new JSONArray();
        final JSONObject token = new JSONObject();
        token.put(ITokenInputService.NAME, "\" breast cancer\"");
        token.put(ITokenInputService.TOKEN_TYPE, ITokenInputService.TokenType.FREETEXT.name());
        tokens.put(token);

        final List<QueryToken> queryTokens = tokenInputService.convertToQueryTokens(tokens);


        final SemedicoSearchCarrier semedicoSearchCarrier = new SemedicoSearchCarrier("FullParsingTestChain");
        semedicoSearchCarrier.setSearchState(new SearchState());
        semedicoSearchCarrier.setUserQuery(queryTokens);
        semedicoSearchCarrier.setSearchCommand(new SemedicoSearchCommand());
        esQueryComponent.process(semedicoSearchCarrier);
        final QueryBuilder query = semedicoSearchCarrier.serverCmds.get(0).query;
        // The two words should be found in the query as one phrase expression
        assertTrue("The substring 'breast cancer' was not found in the query", query.toString().contains("breast cancer"));
        // The query should contain some kind of phrase search.
        assertTrue("The substring 'phrase' was not found in the query. ", query.toString().contains("phrase"));
        registry.shutdown();

    }


    @ImportModule(SemedicoCoreTestModule.class)
    public static class FullParsingTestModule {
        public static void bind(ServiceBinder binder) {
            binder.bind(ITokenInputService.class, TokenInputService.class);
        }

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
    }

    @ImportModule(SemedicoCoreTestModule.class)
    public static class FullParsingTestModuleAquiferConcept {
        public static void bind(ServiceBinder binder) {
            binder.bind(ITokenInputService.class, TokenInputService.class);
        }

        @Contribute(ServiceOverride.class)
        public void overrideConceptService(MappedConfiguration<Class, Object> configuration) {
            final ITermService termService = EasyMock.createStrictMock(ITermService.class);
            final FacetTerm ft = new FacetTerm("tid1");
            final Facet f = new Facet("fid1", "TestFacet");
            ft.setFacets(Arrays.asList(f));
            EasyMock.expect(termService.getTerm("tid1")).andReturn(ft);
            EasyMock.replay(termService);
            configuration.add(ITermService.class, termService);
        }
    }
}
