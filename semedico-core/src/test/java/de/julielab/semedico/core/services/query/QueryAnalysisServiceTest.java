package de.julielab.semedico.core.services.query;

import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.DatabaseConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.parsing.BranchNode;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.BaseConceptService;
import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.services.ServiceOverride;
import org.easymock.EasyMock;
import org.easymock.IMockBuilder;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
public class QueryAnalysisServiceTest {


    @Test
    public void testAnalysisKeywords() {
        final Registry registry = TestUtils.createTestRegistry();
        final IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
        final ParseTree parseTree = queryAnalysisService.analyseQueryString("mtor and mice");
        assertThat(parseTree).extracting(ParseTree::toString).isEqualTo("(mtor AND mice)");
        assertThat(parseTree.getNumberNodes()).isEqualTo(3);
        assertThat(parseTree).extracting(ParseTree::getRoot).isInstanceOf(BranchNode.class);
        assertThat(parseTree).extracting(t -> ((BranchNode)t.getRoot()).getFirstChild()).isInstanceOf(TextNode.class);
        assertThat(parseTree).extracting(t -> ((BranchNode)t.getRoot()).getFirstChild()).isInstanceOf(TextNode.class);
        final QueryToken qtMtor = ((BranchNode) parseTree.getRoot()).getFirstChild().getQueryToken();
        assertThat(qtMtor.getBegin()).isEqualTo(0);
        assertThat(qtMtor.getEnd()).isEqualTo(4);
        assertThat(qtMtor.getInputTokenType()).isEqualTo(ITokenInputService.TokenType.KEYWORD);

        final QueryToken qtMice = ((BranchNode) parseTree.getRoot()).getLastChild().getQueryToken();
        assertThat(qtMice.getBegin()).isEqualTo(9);
        assertThat(qtMice.getEnd()).isEqualTo(13);
        assertThat(qtMice.getInputTokenType()).isEqualTo(ITokenInputService.TokenType.KEYWORD);
        registry.shutdown();
    }

    @Test
    public void testAnalysisKeywordAndConcept() {
        final Registry registry = TestUtils.createTestRegistry(MockConceptServiceModule.class);
        final IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
        final ParseTree parseTree = queryAnalysisService.analyseQueryString("mtor and frap");
        assertThat(parseTree).extracting(ParseTree::toString).isEqualTo("(mtor AND frap)");
        assertThat(parseTree.getNumberNodes()).isEqualTo(3);
        assertThat(parseTree).extracting(ParseTree::getRoot).isInstanceOf(BranchNode.class);
        assertThat(parseTree).extracting(t -> ((BranchNode)t.getRoot()).getFirstChild()).isInstanceOf(TextNode.class);
        assertThat(parseTree).extracting(t -> ((BranchNode)t.getRoot()).getFirstChild()).isInstanceOf(TextNode.class);
        final QueryToken qtMtor = ((BranchNode) parseTree.getRoot()).getFirstChild().getQueryToken();
        assertThat(qtMtor.getBegin()).isEqualTo(0);
        assertThat(qtMtor.getEnd()).isEqualTo(4);
        assertThat(qtMtor.getInputTokenType()).isEqualTo(ITokenInputService.TokenType.KEYWORD);

        final QueryToken qtMice = ((BranchNode) parseTree.getRoot()).getLastChild().getQueryToken();
        assertThat(qtMice.getBegin()).isEqualTo(9);
        assertThat(qtMice.getEnd()).isEqualTo(13);
        assertThat(qtMice.getInputTokenType()).isEqualTo(ITokenInputService.TokenType.CONCEPT);
        // We set this synonym to the test concept in the test module.
        assertThat(qtMice.getMatchedSynonym()).isEqualTo("FRAP");
        registry.shutdown();
    }

    @ImportModule(SemedicoCoreTestModule.class)
    public static class MockConceptServiceModule {
        @Contribute(ServiceOverride.class)
        public void overrideConceptService(MappedConfiguration<Class, Object> configuration) {
            final DatabaseConcept dc = new DatabaseConcept("tid129");
            final Facet f = new Facet("fid1", "TestFacet");
            dc.setFacets(Arrays.asList(f));
            // We set some synonyms so we can check that the "find matched synonym" feature works correctly
            dc.setSynonyms(Arrays.asList("mTOR", "FRAP"));
            // Using the mock builder allows to create a "partial mock". It allows to mock the specified
            // method but falls back to the actually created object for unmocked methods.
            // We do this here because we need the "getCoreConcepts()" call which is done
            // in SemedicoCoreModule#contributeTermDictionaryChunker.
            final IMockBuilder<BaseConceptService> builder = EasyMock.createMockBuilder(BaseConceptService.class);
            builder.addMockedMethod("getTerm");
            final IConceptService conceptServiceMock = builder.createMock();
            EasyMock.expect(conceptServiceMock.getTerm("tid129")).andReturn(dc);
            EasyMock.replay(conceptServiceMock);
            configuration.add(IConceptService.class, conceptServiceMock);
        }
    }
}
