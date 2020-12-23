package de.julielab.semedico.core.services;

import com.google.common.collect.Multimap;
import de.julielab.neo4j.plugins.ConceptManager;
import de.julielab.neo4j.plugins.datarepresentation.PushConceptsToSetCommand;
import de.julielab.neo4j.plugins.datarepresentation.constants.NodeIDPrefixConstants;
import de.julielab.semedico.core.concepts.ConceptDescription;
import de.julielab.semedico.core.concepts.interfaces.IConceptRelation;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.util.ConceptLoadingException;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.julielab.semedico.core.Neo4jTestContainer.neo4j;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Test(groups = {"integration", "neo4j"})
public class Neo4jServiceTest {
    private final static Logger log = LoggerFactory.getLogger(Neo4jServiceTest.class);

    public static Neo4jService neo4jService;
    private static Driver driver;

    @BeforeClass
    public static void start() {
        driver = GraphDatabase.driver("bolt://" + neo4j.getContainerIpAddress() + ":" + neo4j.getMappedPort(7687));
        neo4jService = new Neo4jService(LoggerFactory.getLogger(Neo4jService.class),
                new HttpClientService(LoggerFactory.getLogger(HttpClientService.class)),
                neo4j.getContainerIpAddress(),
                neo4j.getMappedPort(7474),
                driver);
    }

    @AfterClass
    public static void stop() {
        driver.close();
    }

    @Test
    public void testGetConcepts() {
        Stream<ConceptDescription> concepts = neo4jService.getConcepts(Arrays.asList(NodeIDPrefixConstants.TERM + 0, NodeIDPrefixConstants.TERM + 42));
        assertThat(concepts).isNotEmpty();
    }

    @Test
    public void testGetConceptPath() {
        String[] conceptPath = neo4jService.getConceptPath(NodeIDPrefixConstants.TERM + 0, NodeIDPrefixConstants.TERM + 127, IConceptRelation.Type.IS_BROADER_THAN);
        assertThat(conceptPath).hasSize(8);
        assertThat(conceptPath).containsExactly(NodeIDPrefixConstants.TERM + 0,
                NodeIDPrefixConstants.TERM + 1,
                NodeIDPrefixConstants.TERM + 3,
                NodeIDPrefixConstants.TERM + 7,
                NodeIDPrefixConstants.TERM + 15,
                NodeIDPrefixConstants.TERM + 31,
                NodeIDPrefixConstants.TERM + 63,
                NodeIDPrefixConstants.TERM + 127);
    }

    @Test
    public void testGetReflexiveConceptPath() {
        assertThatThrownBy(() -> neo4jService.getConceptPath(NodeIDPrefixConstants.TERM + 42, NodeIDPrefixConstants.TERM + 42, IConceptRelation.Type.IS_BROADER_THAN)).isOfAnyClassIn(IllegalArgumentException.class);
    }

    @Test
    public void testGetEmptyConceptPath() {
        String[] conceptPath = neo4jService.getConceptPath(NodeIDPrefixConstants.TERM + 42, NodeIDPrefixConstants.TERM + 300, IConceptRelation.Type.IS_BROADER_THAN);
        assertThat(conceptPath).isEmpty();
    }

    @Test
    public void testGetFacets() {
        Stream<FacetGroup<Facet>> facets = neo4jService.getFacetGroups(false);
        Optional<FacetGroup<Facet>> facetGroupO = facets.findAny();
        assertThat(facetGroupO.isPresent());
        FacetGroup<Facet> facetGroup = facetGroupO.get();
        assertThat((List<? extends Facet>) facetGroup).hasSize(1);
        Facet facet = facetGroup.get(0);
        assertThat(facet).isNotNull();
        assertThat(facet.getName()).isEqualTo("Facet 1");
        assertThat(facet).
                extracting(f -> f.getSource()).
                extracting("hierarchic").
                isEqualTo(true);
    }
    
    @Test
    public void testShortestRootPathInFacet() {
        String[] path = neo4jService.getShortestRootPathInFacet(NodeIDPrefixConstants.TERM + 127, NodeIDPrefixConstants.FACET + 0);
        assertThat(path).hasSize(8);
        assertThat(path).containsExactly(NodeIDPrefixConstants.TERM + 0,
                NodeIDPrefixConstants.TERM + 1,
                NodeIDPrefixConstants.TERM + 3,
                NodeIDPrefixConstants.TERM + 7,
                NodeIDPrefixConstants.TERM + 15,
                NodeIDPrefixConstants.TERM + 31,
                NodeIDPrefixConstants.TERM + 63,
                NodeIDPrefixConstants.TERM + 127);
    }

    @Test
    public void testGetFacetRootConcepts() throws ConceptLoadingException {
        Multimap<String, ConceptDescription> facetRootConcepts = neo4jService.getFacetRootConcepts(Arrays.asList(NodeIDPrefixConstants.FACET + 0), null, -1);
        assertThat(facetRootConcepts.size()).isEqualTo(2);
        Collection<ConceptDescription> roots = facetRootConcepts.get(NodeIDPrefixConstants.FACET + 0);
        assertThat(roots).extracting("preferredName").contains("RootConcept1", "RootConcept2");
    }

    @Test
    public void testPushToSet() {
        PushConceptsToSetCommand cmd = new PushConceptsToSetCommand();
        cmd.setName = "TESTSET";
        PushConceptsToSetCommand.ConceptSelectionDefinition selectionDefinition = cmd.new ConceptSelectionDefinition();
        selectionDefinition.conceptLabel = ConceptManager.ConceptLabel.CONCEPT.name();
        long pushed = neo4jService.pushTermsToSet(cmd, 10);
        assertThat(pushed).isEqualTo(10L);
    }

    @Test(dependsOnMethods = "testPushToSet", groups = {"neo4jtests"})
    public void testPopFromSet() throws ConceptLoadingException {
        List<ConceptDescription> poppedDescriptions = neo4jService.popTermsFromSet("TESTSET", 11);
        // There should only be 10 concepts, even though we requested a maximum of 11
        assertThat(poppedDescriptions).hasSize(10);
    }
}
