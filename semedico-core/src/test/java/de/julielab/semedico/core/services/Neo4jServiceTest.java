package de.julielab.semedico.core.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.Multimap;
import de.julielab.neo4j.plugins.ConceptManager;
import de.julielab.neo4j.plugins.datarepresentation.*;
import de.julielab.neo4j.plugins.datarepresentation.constants.FacetConstants;
import de.julielab.neo4j.plugins.datarepresentation.constants.NodeIDPrefixConstants;
import de.julielab.semedico.core.concepts.ConceptDescription;
import de.julielab.semedico.core.concepts.interfaces.IConceptRelation;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.util.ConceptLoadingException;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testng.annotations.*;

import java.io.IOException;
import java.util.*;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class Neo4jServiceTest {
    private final static Logger log = LoggerFactory.getLogger(Neo4jServiceTest.class);

    public static GenericContainer neo4j;
    public static Driver driver;
    public static Neo4jService neo4jService;

    @BeforeSuite(groups = {"neo4jtests"})
    public static void startNeo4j() {
        neo4j = new GenericContainer("neo4j:" + SemedicoCoreModule.NEO4J_VERSION).
                withEnv("NEO4J_AUTH", "none").withExposedPorts(7474, 7687).
                withClasspathResourceMapping("julielab-neo4j-plugins-concepts-1.8.0-assembly.jar",
                        "/var/lib/neo4j/plugins/julielab-neo4j-plugins-concepts-1.8.0-assembly.jar",
                        BindMode.READ_WRITE);
        neo4j.start();
        Slf4jLogConsumer toStringConsumer = new Slf4jLogConsumer(log);
        neo4j.followOutput(toStringConsumer, OutputFrame.OutputType.STDOUT);
        ImportFacetGroup fg = new ImportFacetGroup("testfg", 0, Arrays.asList("TOP_GROUP"));
        ImportFacet facet = new ImportFacet(fg, "facet1", "Facet 1", "f1", FacetConstants.SRC_TYPE_HIERARCHICAL);
        ImportConcept root1 = new ImportConcept("RootConcept1", Arrays.asList("synonym"), new ConceptCoordinates("r1", "facetSource", CoordinateType.SRC));
        List<ImportConcept> concepts = new ArrayList<>();
        concepts.add(root1);
        for (int i = 1; i < 128; i++) {
            ImportConcept concept = new ImportConcept("Concept " + i, Arrays.asList("synonym " + 1), new ConceptCoordinates("c" + i, "facetSource", CoordinateType.SRC));
            ImportConcept parent = concepts.get((i - 1) / 2);
            concept.parentCoordinates = Arrays.asList(parent.coordinates);
            concepts.add(concept);
        }
        // Root 2 won't have any concepts, we just use it for root request related tests
        ImportConcept root2 = new ImportConcept("RootConcept2", Arrays.asList("synonym"), new ConceptCoordinates("r2", "facetSource", CoordinateType.SRC));
        concepts.add(root2);

        String uriString = "http://" + neo4j.getContainerIpAddress() + ":" + neo4j.getMappedPort(7474) + "/db/data/ext/"+ConceptManager.class.getSimpleName()+"/graphdb/"+ConceptManager.INSERT_CONCEPTS;

        try {
            ObjectMapper jsonMapper = new ObjectMapper().registerModule(new Jdk8Module());
            jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            Map<String, Object> importMap = new HashMap<>();
            importMap.put(ConceptManager.KEY_CONCEPTS, jsonMapper.writeValueAsString(concepts));
            importMap.put(ConceptManager.KEY_FACET, jsonMapper.writeValueAsString(facet));
            String json = jsonMapper.writeValueAsString(importMap);
            HttpClientService httpService = new HttpClientService(LoggerFactory.getLogger(HttpClientService.class));
            HttpEntity response = httpService.sendPostRequest(uriString, json);
            if (response != null)
                log.info(EntityUtils.toString(response));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        driver = GraphDatabase.driver("bolt://" + neo4j.getContainerIpAddress() + ":" + neo4j.getMappedPort(7687));
        Value value = driver.session().readTransaction(tx -> {
            StatementResult run = tx.run("MATCH (c:CONCEPT) RETURN COUNT(c)");
            return run.next().get(0);
        });
        assertThat(value.asNumber()).isEqualTo(129L);
        neo4jService = new Neo4jService(LoggerFactory.getLogger(Neo4jService.class),
                new HttpClientService(LoggerFactory.getLogger(HttpClientService.class)),
                neo4j.getContainerIpAddress(),
                neo4j.getMappedPort(7474),
                driver);
    }

    @AfterSuite(groups = {"neo4jtests"})
    public static void stopNeo4j() {
        System.out.println("AFTER STUITE NEI4J");
        driver.close();neo4j.stop();
    }

    @Test(groups = {"neo4jtests"})
    public void testGetConcepts() {
        Stream<ConceptDescription> concepts = neo4jService.getConcepts(Arrays.asList(NodeIDPrefixConstants.TERM + 0, NodeIDPrefixConstants.TERM + 42));
        assertThat(concepts).isNotEmpty();
    }

    @Test(groups = {"neo4jtests"})
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

    @Test(groups = {"neo4jtests"})
    public void testGetReflexiveConceptPath() {
        assertThatThrownBy(() -> neo4jService.getConceptPath(NodeIDPrefixConstants.TERM + 42, NodeIDPrefixConstants.TERM + 42, IConceptRelation.Type.IS_BROADER_THAN)).isOfAnyClassIn(IllegalArgumentException.class);
    }

    @Test(groups = {"neo4jtests"})
    public void testGetEmptyConceptPath() {
        String[] conceptPath = neo4jService.getConceptPath(NodeIDPrefixConstants.TERM + 42, NodeIDPrefixConstants.TERM + 300, IConceptRelation.Type.IS_BROADER_THAN);
        assertThat(conceptPath).isEmpty();
    }

    @Test(groups = {"neo4jtests"})
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
                contains(true);
    }
    
    @Test(groups = {"neo4jtests"})
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

    @Test(groups = {"neo4jtests"})
    public void testGetFacetRootConcepts() throws ConceptLoadingException {
        Multimap<String, ConceptDescription> facetRootConcepts = neo4jService.getFacetRootConcepts(Arrays.asList(NodeIDPrefixConstants.FACET + 0), null, -1);
        assertThat(facetRootConcepts.size()).isEqualTo(2);
        Collection<ConceptDescription> roots = facetRootConcepts.get(NodeIDPrefixConstants.FACET + 0);
        assertThat(roots).extracting("preferredName").contains("RootConcept1", "RootConcept2");
    }

    @Test(groups = {"neo4jtests"})
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
