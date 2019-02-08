package de.julielab.semedico.core.search.services;

import de.julielab.elastic.query.components.data.FieldTermItem;
import de.julielab.elastic.query.components.data.HighlightCommand;
import de.julielab.elastic.query.components.data.ISearchServerDocument;
import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.elastic.query.services.IElasticServerResponse;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.CoreConcept;
import de.julielab.semedico.core.entities.documents.SemedicoIndexField;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import de.julielab.semedico.core.search.query.AggregationRequests;
import de.julielab.semedico.core.search.query.ParseTreeQueryBase;
import de.julielab.semedico.core.search.results.FieldTermsRetrievalResult;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;
import de.julielab.semedico.core.services.ConceptNeo4jService;
import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.Registry;
import org.assertj.core.api.Condition;
import org.assertj.core.api.HamcrestCondition;
import org.assertj.core.data.Index;
import org.junit.AfterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.google.common.collect.HashMultiset;
import org.testcontainers.shaded.com.google.common.collect.Multiset;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

/**
 * This is an integration test suite using ElasticSearch. There are two possibilities to use an ElasticSearch server
 * here:
 * 1. You can start up a local ElasticSearch server. If it is found at the address defined in SemedicoCoreTestModule,
 * it will be used. This will speed up testing considerable in comparison to the alternative.
 * 2. If there is no local ElasticSearch server found according to 1., a Docker container will be started. The
 * outcome of the tests should be the same (assuming the ES versions and configurations are the same as in 1.)
 * but the startup of the container takes quite a while. For repeated testing, 1. is recommended.
 */
public class SearchServiceTest {
    public static final String TEST_INDEX = "semedico_testindex";
    public static final String TEST_CLUSTER = "semedico_testcluster";
    private final static Logger log = LoggerFactory.getLogger(SearchServiceTest.class);
    private final static Logger logContainer = LoggerFactory.getLogger("test.escontainer");
    // in case we need to disable X-shield: https://stackoverflow.com/a/51172136/1314955
    public static GenericContainer es;
    private Registry registry;

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

    @BeforeSuite(groups = {"estests"})
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
        registry = TestUtils.createTestRegistry();
        setupES(SemedicoCoreTestModule.esHost, restPort, SemedicoCoreTestModule.esCluster);
    }

    @AfterSuite(groups = {"estests"})
    public void shutdown() {
        if (es != null)
            es.stop();
        registry.shutdown();
    }

    @Test(groups = {"estests"})
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

    @Test(groups = {"estests"})
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

    @Test(groups = {"estests"})
    public void testRetrieveField() throws Exception {
        final ISearchService service = registry.getService(ISearchService.class);

        final ParseTreeQueryBase query = new ParseTreeQueryBase(ParseTree.ofPhrase("first"), TEST_INDEX, Arrays.asList(SemedicoIndexField.termsField("title")), Arrays.asList("title", "text"));
        final TestDocumentResultList resultList = service.search(query, EnumSet.noneOf(SearchService.SearchOption.class), new TestDocumentCollector()).get();
        assertThat(resultList.getDocumentResults()).extracting(TestDocumentResult::getTitle).containsExactly("Title of the first test document.");
    }

    @Test(groups = {"estests"})
    public void testHighlighting() throws Exception {
        final ISearchService service = registry.getService(ISearchService.class);

        final ParseTreeQueryBase query = new ParseTreeQueryBase(ParseTree.ofPhrase("first"), TEST_INDEX, Arrays.asList(SemedicoIndexField.termsField("title")));
        final HighlightCommand hlCmd = new HighlightCommand();
        hlCmd.addField("title", 1, 100);
        query.setHlCmd(hlCmd);
        final TestDocumentResultList resultList = service.search(query, EnumSet.noneOf(SearchService.SearchOption.class), new TestDocumentCollector()).get();
        assertThat(resultList.getDocumentResults()).extracting(TestDocumentResult::getHighlights).flatExtracting(hl -> hl.get("title")).has(new Condition<>(s -> s.contains("<em>first</em>"), null), Index.atIndex(0));
    }

    @Test(groups = {"estests"})
    public void testRetrieveFieldValues() throws Exception {
        final ISearchService service = registry.getService(ISearchService.class);
        final ParseTree parseTree = ParseTree.ofText("*", Node.NodeType.AND);
        parseTree.getRoot().getQueryToken().addConceptToList(new ConceptNeo4jService(LoggerFactory.getLogger(ConceptNeo4jService.class), null, null, null, null).getCoreTerm(CoreConcept.CoreConceptType.ANY_TERM));
        parseTree.getRoot().asTextNode().setNodeType(Node.NodeType.CONCEPT);
        final ParseTreeQueryBase query = new ParseTreeQueryBase(parseTree, TEST_INDEX, Arrays.asList(SemedicoIndexField.termsField("title")));


        query.putAggregationRequest(AggregationRequests.getFieldTermsRequest("fieldterms", "concepts.keyword", 10, AggregationRequests.OrderType.COUNT, AggregationRequest.OrderCommand.SortOrder.DESCENDING));
        final Future<FieldTermsRetrievalResult> resultFuture = service.search(query, EnumSet.of(SearchService.SearchOption.NO_HITS), ResultCollectors.getFieldTermsCollector("fieldtermscollector", "fieldterms"));
        final FieldTermsRetrievalResult result = resultFuture.get();
        final Stream<FieldTermItem> fieldterms = result.getFieldTerms("fieldterms");
        Multiset<String> retrievedTerms = HashMultiset.create();
        fieldterms.forEach(t -> retrievedTerms.add((String) t.term, (int)(long)t.values.get(FieldTermItem.ValueType.COUNT)));
        assertEquals( retrievedTerms.count("dog"), 2);
        assertEquals(retrievedTerms.count("zebra"), 1);
        assertEquals(retrievedTerms.count("document"), 1);
        assertEquals(retrievedTerms.count("man"), 1);
        assertEquals(retrievedTerms.count("rat"), 1);
        assertEquals(retrievedTerms.count("text"), 1);
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
        private final Map<String, List<String>> highlights;
        private String title;
        private String text;

        public TestDocumentResult(ISearchServerDocument serverDoc) {
            this.id = serverDoc.getId();
            final Optional<String> title = serverDoc.getFieldValue("title");
            if (title.isPresent()) this.title = title.get();
            final Optional<String> text = serverDoc.getFieldValue("text");
            if (text.isPresent()) this.text = text.get();
            this.highlights = serverDoc.getHighlights();
        }

        public Map<String, List<String>> getHighlights() {
            return highlights;
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
