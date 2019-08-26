package de.julielab.semedico.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import de.julielab.neo4j.plugins.ConceptManager;
import de.julielab.neo4j.plugins.datarepresentation.*;
import de.julielab.neo4j.plugins.datarepresentation.constants.FacetConstants;
import de.julielab.semedico.core.services.HttpClientService;
import de.julielab.semedico.core.services.Neo4jService;
import de.julielab.semedico.core.services.SemedicoCoreModule;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
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
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Test(groups = {"integration", "neo4j"})
public class Neo4jTestContainer {
    private final static Logger log = LoggerFactory.getLogger(Neo4jTestContainer.class);
    public static GenericContainer neo4j;
    public static Neo4jService neo4jService;
    private static Driver driver;

    @BeforeSuite
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
        // We will use root2 for custom concepts that we need for other tests, e.g. the ConceptRecognitionServiceTest.
        ImportConcept root2 = new ImportConcept("RootConcept2", Arrays.asList("root2ConceptSynonym"), new ConceptCoordinates("r2", "facetSource", CoordinateType.SRC));
        concepts.add(root2);
        final ImportConcept mtor = new ImportConcept("mTOR", Arrays.asList("FRAP"), "An mTOR test concept", new ConceptCoordinates("c200", "facetSource", CoordinateType.SRC), root2.coordinates);
        concepts.add(mtor);

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
        assertThat(value.asNumber()).isEqualTo(130L);
        neo4jService = new Neo4jService(LoggerFactory.getLogger(Neo4jService.class),
                new HttpClientService(LoggerFactory.getLogger(HttpClientService.class)),
                neo4j.getContainerIpAddress(),
                neo4j.getMappedPort(7474),
                driver);
        System.setProperty(SemedicoSymbolConstants.NEO4J_HOST, neo4j.getContainerIpAddress());
        System.setProperty(SemedicoSymbolConstants.NEO4J_HTTP_PORT, String.valueOf(neo4j.getMappedPort(7474)));
        System.setProperty(SemedicoSymbolConstants.NEO4J_BOLT_PORT, String.valueOf(neo4j.getMappedPort(7687)));
        System.setProperty(SemedicoSymbolConstants.NEO4J_BOLT_URI, "bolt://" + neo4j.getContainerIpAddress() + ":" + neo4j.getMappedPort(7687));
    }

    @AfterSuite
    public static void stopNeo4j() {
        driver.close();neo4j.stop();
    }
}
