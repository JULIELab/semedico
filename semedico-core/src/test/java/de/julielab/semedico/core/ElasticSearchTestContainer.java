package de.julielab.semedico.core;

import de.julielab.java.utilities.FileUtilities;
import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertTrue;

@Test(groups = {"integration", "elasticsearch"})
public class ElasticSearchTestContainer {
    public static final String TEST_INDEX = "semedico_testindex";
    public static final String TEST_INDEX_PREANALYZED = "semedico_testindex_preanalyzed";
    public static final String TEST_CLUSTER = "semedico_testcluster";
    private final static Logger log = LoggerFactory.getLogger(ElasticSearchTestContainer.class);
    private final static Logger logContainer = LoggerFactory.getLogger("test.escontainer");
    // in case we need to disable X-shield: https://stackoverflow.com/a/51172136/1314955
    public static GenericContainer es;

    private static void setupES(String esHost, String esPort, String esCluster) throws Exception {
        createIndex(esHost, esPort, "src/test/resources/searchservice/esMappings/simpleMapping.json", TEST_INDEX);
        createIndex(esHost, esPort, "src/test/resources/searchservice/esMappings/preanalyzedMapping.json", TEST_INDEX_PREANALYZED);


        indexTestDocuments(esHost, esPort, TEST_INDEX, new File("src/test/resources/searchservice/simpleDocuments"));
        indexTestDocuments(esHost, esPort, TEST_INDEX_PREANALYZED, new File("src/test/resources/searchservice/preanalyzedDocuments"));
    }

    private static void indexTestDocuments(String esHost, String esPort, String testIndex, File documentsDirectory) throws IOException, InterruptedException {
        File[] testdocuments = documentsDirectory.listFiles((dir1, name) -> name.endsWith("json"));
        {
            // Index the test documents (created with gepi-indexing-pipeline and the JsonWriter).
            log.debug("Reading {} test documents for indexing", testdocuments.length);
            List<String> bulkCommandLines = new ArrayList<>(testdocuments.length);
            ObjectMapper om = new ObjectMapper();
            for (File doc : testdocuments) {
                String jsonContents = IOUtils.toString(FileUtilities.getInputStreamFromFile(doc), UTF_8).replaceAll(System.getProperty("line.separator"), "");
                Map<String, Object> indexMap = new HashMap<>();
                indexMap.put("_index", testIndex);
                indexMap.put("_id", doc.getName().replace(".json", ""));
                Map<String, Object> map = new HashMap<>();
                map.put("index", indexMap);
                bulkCommandLines.add(om.writeValueAsString(map));
                bulkCommandLines.add(jsonContents);
            }
            log.debug("Indexing test documents");
            URL url = new URL("http://" + esHost + ":" + esPort + "/_bulk");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            OutputStream outputStream = urlConnection.getOutputStream();
            IOUtils.writeLines(bulkCommandLines, System.getProperty("line.separator"), outputStream, "UTF-8");
            log.debug("Response for indexing: {}", urlConnection.getResponseMessage());
        }
        // Wait for ES to finish its indexing
        Thread.sleep(2000);
        {
            URL url = new URL("http://" + esHost + ":" + esPort + "/" + testIndex + "/_count");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            String countResponse = IOUtils.toString(urlConnection.getInputStream(), UTF_8);
            log.debug("Response for the count of documents: {}", countResponse);
            assertTrue(countResponse.contains("count\":" + testdocuments.length), "Unexpected count response: " + countResponse);
        }
    }

    private static void createIndex(String esHost, String esPort, String mappingPath, String indexName) throws IOException {
        {
            // Delete a potentially already existing test index
            URL url = new URL("http://" + esHost + ":" + esPort + "/" + indexName);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.connect();
            log.info("Response for index deletion: {}", urlConnection.getResponseMessage());

            if (urlConnection.getErrorStream() != null) {
                String error = IOUtils.toString(urlConnection.getErrorStream(), UTF_8);
                if (!error.contains("index_not_found_exception"))
                    log.error("Error when deleting index: {}", error);
            }
        }

        // Create the test index
        URL url = new URL("http://" + esHost + ":" + esPort + "/" + indexName);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("PUT");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setDoOutput(true);
        String mapping = IOUtils.toString(new File(mappingPath).toURI(), UTF_8);
        IOUtils.write(mapping, urlConnection.getOutputStream(), UTF_8);
        log.info("Response for index creation: {}", urlConnection.getResponseMessage());

        if (urlConnection.getErrorStream() != null) {
            String error = IOUtils.toString(urlConnection.getErrorStream(), UTF_8);
            log.error("Error when creating index: {}", error);
            throw new IllegalStateException(error);
        }
    }

    @BeforeSuite
    public void setup() throws Exception {
        String restPort = "9200";
        try {
            // Check if there is a local running ES instance found
            URL localEsUrl = new URL("http://" + SemedicoCoreTestModule.esHost + ":" + SemedicoCoreTestModule.esPort);
            localEsUrl.openConnection().connect();
            log.info("Found locally running ElasticSearch at {}. Using it for testing", localEsUrl);
        } catch (java.net.ConnectException e) {
            log.info("There is no locally running ElasticSearch available. Starting test container.");
            // No locally running ElasticSearch could be connected to. Starting the docker container.
            es = new GenericContainer(new ImageFromDockerfile("semedico_core_test", true)
                    .withFileFromClasspath("Dockerfile", "searchservice/dockercontext/Dockerfile")
                    .withFileFromClasspath("elasticsearch-mapper-preanalyzed-7.0.1-SNAPSHOT.zip", "searchservice/dockercontext/elasticsearch-mapper-preanalyzed-7.0.1-SNAPSHOT.zip"))
                    .withExposedPorts(9200)
                    .withStartupTimeout(Duration.ofMinutes(2))
                    .withEnv("cluster.name", TEST_CLUSTER)
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("discovery.type", "single-node");
            es.start();
            Slf4jLogConsumer toStringConsumer = new Slf4jLogConsumer(logContainer);
            es.followOutput(toStringConsumer, OutputFrame.OutputType.STDOUT);
            SemedicoCoreTestModule.esPort = String.valueOf(es.getMappedPort(9200));
            SemedicoCoreTestModule.esCluster = TEST_CLUSTER;
            restPort = String.valueOf(es.getMappedPort(9200));
        }
        setupES(SemedicoCoreTestModule.esHost, restPort, SemedicoCoreTestModule.esCluster);
        log.info("ElasticSearch test setup finished");
    }

    @AfterSuite
    public void shutdown() {
        if (es != null)
            es.stop();
    }
}
