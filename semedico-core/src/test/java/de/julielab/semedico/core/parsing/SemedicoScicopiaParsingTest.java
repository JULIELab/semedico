package de.julielab.semedico.core.parsing;

import de.julielab.elastic.query.components.ISearchComponent;
import de.julielab.scicopia.core.search.ElasticsearchQueryComponent;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.DatabaseConcept;
import de.julielab.semedico.core.entities.state.SearchState;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.search.components.QueryAnalysisComponent;
import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.query.TokenInputService;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.services.ServiceOverride;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.easymock.EasyMock;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class SemedicoScicopiaParsingTest {
    private static Registry registry;

    @AfterTest
    public static void shutdown() {
        registry.shutdown();
    }
    @Test
    public void testParse() {
        registry = TestUtils.createTestRegistry(SemedicoScicopiaParsingTestModule.class);
        final ITokenInputService tokenInputService = registry.getService(ITokenInputService.class);
        final ISearchComponent queryAnalysisComponent = registry.getService(ISearchComponent.class, QueryAnalysisComponent.QueryAnalysis.class);
        final ISearchComponent esQueryComponent = registry.getService(ISearchComponent.class, ElasticsearchQueryComponent.ElasticsearchQuery.class);

        final JSONArray tokens = new JSONArray();
        final JSONObject token = new JSONObject();
        token.put(ITokenInputService.NAME, "aquifer");
        token.put(ITokenInputService.TOKEN_TYPE, ITokenInputService.TokenType.FREETEXT.name());
        tokens.put(token);

        final List<QueryToken> queryTokens = tokenInputService.convertToQueryTokens(tokens);

    }

    @ImportModule(SemedicoCoreTestModule.class)
    public static class SemedicoScicopiaParsingTestModule {
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
}
