package de.julielab.semedico.core.suggestions;

import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetSource;
import de.julielab.semedico.core.search.services.ISearchService;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import org.apache.tapestry5.ioc.Registry;
import org.easymock.EasyMock;
import org.junit.AfterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Set;

public class ConceptSuggestionServiceTest {
private final static Logger log = LoggerFactory.getLogger(ConceptSuggestionServiceTest.class);

    private Registry registry;

    @BeforeClass
    public void setup() throws Exception {
        registry = TestUtils.createTestRegistry();
    }

    @AfterClass
    public void shutdown() {
        registry.shutdown();
    }

    @Test
    public void testGetConceptIdsInSuggestionFacets() throws Exception {
        // Set up a facet service mock the provides the facet(s) for which suggestions should be created
        final Facet suggestionFacet = new Facet("fid1", "SuggestionFacet");
        suggestionFacet.setSource(new FacetSource(FacetSource.SourceType.FIELD_TAXONOMIC_TERMS, "concepts"));
        final IFacetService facetServiceMock = EasyMock.createMock(IFacetService.class);
        EasyMock.expect(facetServiceMock.getSuggestionFacets()).andReturn(Arrays.asList(suggestionFacet));
        EasyMock.replay(facetServiceMock);

        final ISearchService searchService = registry.getService(ISearchService.class);

        final ConceptSuggestionService suggestionService = new ConceptSuggestionService(LoggerFactory.getLogger(ConceptSuggestionService.class), null, null, facetServiceMock, searchService, null, null, true, null, "concepts", true);
        final Method m = suggestionService.getClass().getDeclaredMethod("getConceptIdsInSuggestionFacets");
        m.setAccessible(true);
        final Set<String> conceptIds = (Set<String>) m.invoke(suggestionService);
        System.out.println(conceptIds);
    }
}
