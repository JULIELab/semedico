package de.julielab.semedico.core.services.query;

import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.DatabaseConcept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.parsing.BranchNode;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.services.ServiceOverride;
import org.easymock.EasyMock;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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
        assertThat(qtMtor.getBeginOffset()).isEqualTo(0);
        assertThat(qtMtor.getEndOffset()).isEqualTo(4);
        assertThat(qtMtor.getInputTokenType()).isEqualTo(ITokenInputService.TokenType.KEYWORD);

        final QueryToken qtMice = ((BranchNode) parseTree.getRoot()).getLastChild().getQueryToken();
        assertThat(qtMice.getBeginOffset()).isEqualTo(9);
        assertThat(qtMice.getEndOffset()).isEqualTo(13);
        assertThat(qtMice.getInputTokenType()).isEqualTo(ITokenInputService.TokenType.KEYWORD);
        registry.shutdown();
    }

    @Test
    public void testAnalysisKeywordAndConcept() {
        final Registry registry = TestUtils.createTestRegistry(MockConceptServiceModule.class);
        final IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
        final ParseTree parseTree = queryAnalysisService.analyseQueryString("mtor and frap");
        assertThat(parseTree).extracting(ParseTree::toString).isEqualTo("(mtor AND FRAP)");
        assertThat(parseTree.getNumberNodes()).isEqualTo(3);
        assertThat(parseTree).extracting(ParseTree::getRoot).isInstanceOf(BranchNode.class);
        assertThat(parseTree).extracting(t -> ((BranchNode)t.getRoot()).getFirstChild()).isInstanceOf(TextNode.class);
        assertThat(parseTree).extracting(t -> ((BranchNode)t.getRoot()).getFirstChild()).isInstanceOf(TextNode.class);
        final QueryToken qtMtor = ((BranchNode) parseTree.getRoot()).getFirstChild().getQueryToken();
        assertThat(qtMtor.getBeginOffset()).isEqualTo(0);
        assertThat(qtMtor.getEndOffset()).isEqualTo(4);
        assertThat(qtMtor.getInputTokenType()).isEqualTo(ITokenInputService.TokenType.KEYWORD);

        final QueryToken qtMice = ((BranchNode) parseTree.getRoot()).getLastChild().getQueryToken();
        assertThat(qtMice.getBeginOffset()).isEqualTo(9);
        assertThat(qtMice.getEndOffset()).isEqualTo(13);
        assertThat(qtMice.getInputTokenType()).isEqualTo(ITokenInputService.TokenType.CONCEPT);
        registry.shutdown();
    }

    @ImportModule(SemedicoCoreTestModule.class)
    public static class MockConceptServiceModule {
        @Contribute(ServiceOverride.class)
        public void overrideConceptService(MappedConfiguration<Class, Object> configuration) {
            final IConceptService termService = EasyMock.createStrictMock(IConceptService.class);
            final DatabaseConcept ft = new DatabaseConcept("tid1");
            final Facet f = new Facet("fid1", "TestFacet");
            ft.setFacets(Arrays.asList(f));
            EasyMock.expect(termService.getTerm("tid1")).andReturn(ft);
            EasyMock.replay(termService);
            configuration.add(IConceptService.class, termService);
        }
    }
}
