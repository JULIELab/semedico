package de.julielab.semedico.core.search.services;

import de.julielab.elastic.query.components.data.ISearchServerDocument;
import de.julielab.elastic.query.services.IElasticServerResponse;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.entities.documents.SemedicoIndexField;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import de.julielab.semedico.core.search.query.ParseTreeQueryBase;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;
import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class SearchServiceTest {
    public static final String TEST_INDEX = "semedico_testindex";
    public static final String TEST_CLUSTER = "semedico_testcluster";
    private final static Logger log = LoggerFactory.getLogger(SearchServiceTest.class);
    private final static Logger logContainer = LoggerFactory.getLogger("test.escontainer");
    // in case we need to disable X-shield: https://stackoverflow.com/a/51172136/1314955
    public static GenericContainer es = new GenericContainer("docker.elastic.co/elasticsearch/elasticsearch:5.4.0").withExposedPorts(9200, 9300)
            .withStartupTimeout(Duration.ofMinutes(2))
            .withEnv("cluster.name", TEST_CLUSTER)
            .withEnv("xpack.security.enabled", "false");
    private Registry registry;

    private static void setupES() throws Exception {
        es.start();
        Slf4jLogConsumer toStringConsumer = new Slf4jLogConsumer(logContainer);
        es.followOutput(toStringConsumer, OutputFrame.OutputType.STDOUT);

        {
            // Create the test index
            URL url = new URL("http://localhost:" + es.getMappedPort(9200) + "/" + TEST_INDEX);
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
            URL url = new URL("http://localhost:" + es.getMappedPort(9200) + "/_bulk");
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
            URL url = new URL("http://localhost:" + es.getMappedPort(9200) + "/" + TEST_INDEX + "/_count");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            String countResponse = IOUtils.toString(urlConnection.getInputStream(), StandardCharsets.UTF_8);
            log.debug("Response for the count of documents: {}", countResponse);
            assertTrue(countResponse.contains("count\":2"));
        }
    }

    @BeforeClass
    public void setup() throws Exception {
        setupES();
        SemedicoCoreTestModule.esPort = String.valueOf(es.getMappedPort(9300));
        SemedicoCoreTestModule.esCluster = TEST_CLUSTER;
        registry = TestUtils.createTestRegistry();
    }

    @AfterClass
    public void shutdown() {
        es.stop();
        registry.shutdown();
    }

    @Test
    public void testSimpleSearch() throws Exception {
        final ISearchService service = registry.getService(ISearchService.class);

        final ParseTreeQueryBase query = new ParseTreeQueryBase(ParseTree.ofPhrase("dogs"), TEST_INDEX);
        query.setSearchedFields(Arrays.asList(SemedicoIndexField.termsField("text")));
        final Future<TestDocumentResultList> future = service.search(query, EnumSet.of(SearchService.SearchOption.FULL), new TestDocumentCollector());
        final TestDocumentResultList results = future.get();
        assertThat(results).isNotNull();
        assertThat(results.getDocumentResults()).isNotNull();
        final List<TestDocumentResult> documentResults = results.getDocumentResults();
        assertThat(documentResults).hasSize(2);
        assertThat(documentResults).extracting(TestDocumentResult::getId).containsExactlyInAnyOrder("doc1", "doc2");
    }

    @Test
    public void testFollowUpSearches() throws Exception {
        final ISearchService service = registry.getService(ISearchService.class);

        final ParseTreeQueryBase query = new ParseTreeQueryBase(ParseTree.ofPhrase("first"), TEST_INDEX, SemedicoIndexField.termsField("title"));
        final TestDocumentResultList resultList = service.search(query, EnumSet.noneOf(SearchService.SearchOption.class), new TestDocumentCollector()).get();
        assertThat(resultList.getDocumentResults()).extracting(TestDocumentResult::getId).containsExactlyInAnyOrder("doc1");

        final ParseTreeQueryBase query2 = new ParseTreeQueryBase(ParseTree.ofText("title of the first Document", Node.NodeType.OR), TEST_INDEX, SemedicoIndexField.termsField("title"));
        final TestDocumentResultList resultList2 = service.search(query2, EnumSet.noneOf(SearchService.SearchOption.class), new TestDocumentCollector()).get();
        assertThat(resultList2.getDocumentResults()).extracting(TestDocumentResult::getId).containsExactlyInAnyOrder("doc1", "doc2");

        final ParseTreeQueryBase query3 = new ParseTreeQueryBase(ParseTree.ofText("title of the first Document", Node.NodeType.AND), TEST_INDEX, SemedicoIndexField.termsField("title"));
        final TestDocumentResultList resultList3 = service.search(query3, EnumSet.noneOf(SearchService.SearchOption.class), new TestDocumentCollector()).get();
        assertThat(resultList3.getDocumentResults()).extracting(TestDocumentResult::getId).containsExactlyInAnyOrder("doc1");
    }

    @Test
    public void testRetrieveField() throws Exception {
        final ISearchService service = registry.getService(ISearchService.class);

        final ParseTreeQueryBase query = new ParseTreeQueryBase(ParseTree.ofPhrase("first"), TEST_INDEX, Arrays.asList(SemedicoIndexField.termsField("title")), Arrays.asList("title" ,"text"));
        final TestDocumentResultList resultList = service.search(query, EnumSet.noneOf(SearchService.SearchOption.class), new TestDocumentCollector()).get();
        assertThat(resultList.getDocumentResults()).extracting(TestDocumentResult::getTitle).containsExactly("Title of the first test document.");
    }

    private class TestDocumentResultList extends SemedicoSearchResult {
        private List<TestDocumentResult> documentResults = new ArrayList<>();

        public List<TestDocumentResult> getDocumentResults() {
            return documentResults;
        }

        public void addResult(TestDocumentResult result) {
            documentResults.add(result);

        }
    }


    private class TestDocumentResult {

        private final String id;
        private String title;
        private String text;

        public TestDocumentResult(ISearchServerDocument serverDoc) {
            this.id = serverDoc.getId();
            final Optional<String> title = serverDoc.getFieldValue("title");
            if (title.isPresent()) this.title = title.get();
            final Optional<String> text = serverDoc.getFieldValue("text");
            if (text.isPresent()) this.text = text.get();
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getText() {
            return text;
        }
    }

    private class TestDocumentCollector extends SearchResultCollector<SemedicoESSearchCarrier, TestDocumentResultList> {

        public TestDocumentCollector() {
            super("Test Document Collector");
        }

        @Override
        public TestDocumentResultList collectResult(SemedicoESSearchCarrier carrier, int responseIndex) {
            final IElasticServerResponse response = carrier.getSearchResponse(responseIndex);
            final Iterator<ISearchServerDocument> it = response.getDocumentResults().iterator();
            final TestDocumentResultList collection = new TestDocumentResultList();
            while (it.hasNext()) {
                ISearchServerDocument serverDoc = it.next();
                final TestDocumentResult testDocumentResult = new TestDocumentResult(serverDoc);
                collection.addResult(testDocumentResult);
            }
            return collection;
        }
    }
}
