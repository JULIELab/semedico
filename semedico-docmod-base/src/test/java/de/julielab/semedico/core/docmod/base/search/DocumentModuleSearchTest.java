package de.julielab.semedico.core.docmod.base.search;

import de.julielab.java.utilities.FileUtilities;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.docmod.base.broadcasting.QueryBroadcastResult;
import de.julielab.semedico.core.docmod.base.broadcasting.SerpItemCollectorBroadcast;
import de.julielab.semedico.core.docmod.base.defaultmodule.entities.DefaultSerpItem;
import de.julielab.semedico.core.docmod.base.defaultmodule.services.DefaultDocumentModule;
import de.julielab.semedico.core.docmod.base.entities.QueryTarget;
import de.julielab.semedico.core.docmod.base.entities.SerpItemResult;
import de.julielab.semedico.core.docmod.base.services.IDocModInformationService;
import de.julielab.semedico.core.docmod.base.services.IQueryBroadcastingService;
import de.julielab.semedico.core.docmod.base.services.SemedicoDocModTestModule;
import de.julielab.semedico.core.entities.docmods.DocumentPart;
import de.julielab.semedico.core.parsing.SecopiaParse;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.search.query.SecopiaElasticQuery;
import de.julielab.semedico.core.search.results.highlighting.ISerpHighlight;
import de.julielab.semedico.core.search.services.ISearchService;
import de.julielab.semedico.core.search.services.SearchService;
import de.julielab.semedico.core.search.services.SearchServiceTest;
import de.julielab.semedico.core.services.SemedicoCoreModule;
import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.query.SecopiaParsingService;
import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.services.SymbolSource;
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
import java.time.Duration;
import java.util.*;

import static de.julielab.semedico.core.search.query.DefaultSearchStrategy.DEFAULT_STRATEGY;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class DocumentModuleSearchTest {
    public static final String TEST_INDEX = "semedico_testindex";
    public static final String TEST_CLUSTER = "semedico_testcluster";
    private final static Logger log = LoggerFactory.getLogger(SearchServiceTest.class);
    private final static Logger logContainer = LoggerFactory.getLogger("test.escontainer");
    // in case we need to disable X-shield: https://stackoverflow.com/a/51172136/1314955
    public static GenericContainer es = new GenericContainer("docker.elastic.co/elasticsearch/elasticsearch:"+ SemedicoCoreModule.ES_VERSION ).withExposedPorts(9200, 9300)
            .withStartupTimeout(Duration.ofMinutes(2))
            .withEnv("cluster.name", TEST_CLUSTER)
            .withEnv("xpack.security.enabled", "false")
            .withEnv("discovery.type", "single-node");;
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
            String mapping = IOUtils.toString(new File("src/main/resources/defaultdocmod-mapping.json").toURI(), UTF_8);
            IOUtils.write(mapping, urlConnection.getOutputStream(), UTF_8);
            log.info("Response for index creation: {}", urlConnection.getResponseMessage());

            if (urlConnection.getErrorStream() != null) {
                String error = IOUtils.toString(urlConnection.getErrorStream());
                log.error("Error when creating index: {}", error);
            }


        }


        {
            // Index the test documents (created with gepi-indexing-pipeline and the JsonWriter).
            File dir = new File("src/test/resources/search");
            File[] testdocuments = dir.listFiles((dir1, name) -> name.endsWith("json"));
            log.debug("Reading {} test documents for indexing", testdocuments.length);
            List<String> bulkCommandLines = new ArrayList<>(testdocuments.length);
            ObjectMapper om = new ObjectMapper();
            for (File doc : testdocuments) {
                String jsonContents = IOUtils.toString(FileUtilities.getInputStreamFromFile(doc), UTF_8).replaceAll(System.getProperty("line.separator"), "");
                Map<String, Object> indexMap = new HashMap<>();
                indexMap.put("_index", TEST_INDEX);
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
            String countResponse = IOUtils.toString(urlConnection.getInputStream(), UTF_8);
            log.debug("Response for the count of documents: {}", countResponse);
            assertTrue(countResponse.contains("count\":2"));
        }
    }

    @BeforeClass
    public void setup() throws Exception {
        setupES();
        SemedicoCoreTestModule.esPort = String.valueOf(es.getMappedPort(9200));
        SemedicoCoreTestModule.esCluster = TEST_CLUSTER;
        registry = TestUtils.createTestRegistry(SemedicoDocModTestModule.class);
    }

    @AfterClass
    public void shutdown() {
        es.stop();
        registry.shutdown();
    }

    @Test
    public void testSearch() throws Exception {
        final IQueryBroadcastingService broadcastingService = registry.getService(IQueryBroadcastingService.class);
        final ISearchService searchService = registry.getService(ISearchService.class);
        final IDocModInformationService docModInformationService = registry.getService(IDocModInformationService.class);
        final SymbolSource symbolSource = registry.getService(SymbolSource.class);

        final SecopiaParsingService parsingService = new SecopiaParsingService(LoggerFactory.getLogger(SecopiaParsingService.class));
        final QueryToken qt = new QueryToken(0, 6, "zebras");
        qt.setLexerType(QueryToken.Category.ALPHANUM);
        qt.setInputTokenType(ITokenInputService.TokenType.KEYWORD);
        final SecopiaParse parse = parsingService.parseQueryTokens(Collections.singletonList(qt));
        final SecopiaElasticQuery queryTemplate = new SecopiaElasticQuery(parse, null, DEFAULT_STRATEGY);


        String defaultDocModName = symbolSource.valueForSymbol(DefaultDocumentModule.DEFAULT_DOCMOD_NAME);
        final DocumentPart documentPart = docModInformationService.getDocumentPart(defaultDocModName, "Text");
        final QueryTarget queryTarget = new QueryTarget(defaultDocModName, documentPart, DEFAULT_STRATEGY);

        final QueryBroadcastResult queryBroadcastResult = broadcastingService.broadcastQuery(queryTemplate, Arrays.asList(queryTarget), null, Arrays.asList(new SerpItemCollectorBroadcast()));

        final ISemedicoQuery query = queryBroadcastResult.getQuery(0);
        final SerpItemResult<DefaultSerpItem> searchResult = (SerpItemResult<DefaultSerpItem>) searchService.search(query, EnumSet.of(SearchService.SearchOption.FULL), queryBroadcastResult.getResultCollectors(query).get(0)).get();
        assertThat(searchResult.getItems()).hasSize(1);
        final DefaultSerpItem defaultSerpItem = searchResult.getItems().get(0);
        final ISerpHighlight highlight = defaultSerpItem.getHighlight(DefaultDocumentModule.FIELD_TEXT);
        assertThat(highlight).isNotNull();
        final String singleHighlight = highlight.single().getHighlight();
        assertThat(singleHighlight).isNotNull().contains("<em>zebras</em>");
    }

    @Test
    public void testSearchCountOnly() throws Exception {
        final IQueryBroadcastingService broadcastingService = registry.getService(IQueryBroadcastingService.class);
        final ISearchService searchService = registry.getService(ISearchService.class);
        final IDocModInformationService docModInformationService = registry.getService(IDocModInformationService.class);
        final SymbolSource symbolSource = registry.getService(SymbolSource.class);

        final SecopiaParsingService parsingService = new SecopiaParsingService(LoggerFactory.getLogger(SecopiaParsingService.class));
        final QueryToken qt = new QueryToken(0, 4, "dogs");
        qt.setLexerType(QueryToken.Category.ALPHANUM);
        qt.setInputTokenType(ITokenInputService.TokenType.KEYWORD);
        final SecopiaParse parse = parsingService.parseQueryTokens(Collections.singletonList(qt));
        final SecopiaElasticQuery queryTemplate = new SecopiaElasticQuery(parse, null, DEFAULT_STRATEGY);


        String defaultDocModName = symbolSource.valueForSymbol(DefaultDocumentModule.DEFAULT_DOCMOD_NAME);
        final DocumentPart documentPart = docModInformationService.getDocumentPart(defaultDocModName, "Text");
        final QueryTarget queryTarget = new QueryTarget(defaultDocModName, documentPart, DEFAULT_STRATEGY);

        final QueryBroadcastResult queryBroadcastResult = broadcastingService.broadcastQuery(queryTemplate, Arrays.asList(queryTarget), null, Arrays.asList(new SerpItemCollectorBroadcast()));

        final ISemedicoQuery query = queryBroadcastResult.getQuery(0);
        final SerpItemResult<DefaultSerpItem> searchResult = (SerpItemResult<DefaultSerpItem>) searchService.search(query, EnumSet.of(SearchService.SearchOption.HIT_COUNT), queryBroadcastResult.getResultCollectors(query).get(0)).get();
        assertThat(searchResult.getNumDocumentsFound()).isEqualTo(2);
        assertThat(searchResult.getItems()).hasSize(0);

    }
}
