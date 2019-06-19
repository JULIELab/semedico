package de.julielab.semedico.core;

import de.julielab.java.utilities.FileUtilities;
import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertTrue;

@Test(groups={"integration", "elasticsearch"})
public class ElasticSearchTestHelper {
    public static final String TEST_INDEX = "semedico_testindex";
    public static final String TEST_CLUSTER = "semedico_testcluster";
    private final static Logger log = LoggerFactory.getLogger(ElasticSearchTestHelper.class);
    private final static Logger logContainer = LoggerFactory.getLogger("test.escontainer");
    // in case we need to disable X-shield: https://stackoverflow.com/a/51172136/1314955
    public static GenericContainer es;

    @BeforeTest
    public void setup() throws Exception {
        String restPort = "9200";
        try {
            // Check if there is a local running ES instance found
            URL localEsUrl = new URL("http://" + SemedicoCoreTestModule.esHost + ":" + SemedicoCoreTestModule.esPort);
            localEsUrl.openConnection().connect();
        } catch (java.net.ConnectException e) {
            // No locally running ElasticSearch could be connected to. Starting the docker container.
            es = new GenericContainer("docker.elastic.co/elasticsearch/elasticsearch:5.4.0").withExposedPorts(9200, 9300)
                    .withStartupTimeout(Duration.ofMinutes(2))
                    .withEnv("cluster.name", TEST_CLUSTER)
                    .withEnv("xpack.security.enabled", "false");
            es.start();
            Slf4jLogConsumer toStringConsumer = new Slf4jLogConsumer(logContainer);
            es.followOutput(toStringConsumer, OutputFrame.OutputType.STDOUT);
            SemedicoCoreTestModule.esPort = String.valueOf(es.getMappedPort(9300));
            SemedicoCoreTestModule.esCluster = TEST_CLUSTER;
            restPort = String.valueOf(es.getMappedPort(9200));
        }
        setupES(SemedicoCoreTestModule.esHost, restPort, SemedicoCoreTestModule.esCluster);
    }

    @AfterTest
    public void shutdown() {
        if (es != null)
            es.stop();
    }

    private static void setupES(String esHost, String esPort, String esCluster) throws Exception {
        {
            {
                // Delete a potentially already existing test index
                URL url = new URL("http://" + esHost + ":" + esPort + "/" + TEST_INDEX);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("DELETE");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.connect();
                log.info("Response for index deletion: {}", urlConnection.getResponseMessage());

                if (urlConnection.getErrorStream() != null) {
                    String error = IOUtils.toString(urlConnection.getErrorStream());
                    log.error("Error when deleting index: {}", error);
                }
            }

            // Create the test index
            URL url = new URL("http://" + esHost + ":" + esPort + "/" + TEST_INDEX);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("PUT");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoOutput(true);
            String mapping = IOUtils.toString(new File("src/test/resources/searchservice/esMappings/simpleMapping.json").toURI());
            IOUtils.write(mapping, urlConnection.getOutputStream(), StandardCharsets.UTF_8);
            log.info("Response for index creation: {}", urlConnection.getResponseMessage());

            if (urlConnection.getErrorStream() != null) {
                String error = IOUtils.toString(urlConnection.getErrorStream());
                log.error("Error when creating index: {}", error);
            }


        }


        {
            // Index the test documents (created with gepi-indexing-pipeline and the JsonWriter).
            File dir = new File("src/test/resources/searchservice");
            File[] testdocuments = dir.listFiles((dir1, name) -> name.endsWith("json"));
            log.debug("Reading {} test documents for indexing", testdocuments.length);
            List<String> bulkCommandLines = new ArrayList<>(testdocuments.length);
            ObjectMapper om = new ObjectMapper();
            for (File doc : testdocuments) {
                String jsonContents = IOUtils.toString(FileUtilities.getInputStreamFromFile(doc), StandardCharsets.UTF_8).replaceAll(System.getProperty("line.separator"), "");
                Map<String, Object> indexMap = new HashMap<>();
                indexMap.put("_index", TEST_INDEX);
                indexMap.put("_type", "documents");
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
            URL url = new URL("http://" + esHost + ":" + esPort + "/" + TEST_INDEX + "/_count");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            String countResponse = IOUtils.toString(urlConnection.getInputStream(), StandardCharsets.UTF_8);
            log.debug("Response for the count of documents: {}", countResponse);
            assertTrue(countResponse.contains("count\":2"));
        }
    }
}
