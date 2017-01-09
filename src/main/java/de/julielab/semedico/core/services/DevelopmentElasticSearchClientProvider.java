package de.julielab.semedico.core.services;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;

import de.julielab.elastic.query.services.ISearchClientProvider;
import de.julielab.elastic.query.services.ISearchClient;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.suggestions.ITermSuggestionService;

/**
 * Returns a client to a local, embedded search index with a few small
 * documents. Does not require an external ElasticSearch server. This is meant
 * for easy development.
 * 
 * @author faessler
 * 
 */
public class DevelopmentElasticSearchClientProvider implements ISearchClientProvider {

	private Client esClient;
	private Logger log;
	private Node node;
	private Gson gson = new Gson();

	public DevelopmentElasticSearchClientProvider(Logger log, ITermSuggestionService suggestionService) throws IOException {
		this.log = log;
		createLocalNodeClient();
		setupIndex();
		indexDocuments("src/test/resources/json-st09", IIndexInformationService.Indexes.documents,
				IIndexInformationService.Indexes.DocumentTypes.pmc,
				IIndexInformationService.GeneralIndexStructure.pmcid);
		suggestionService.createSuggestionIndex();
	}

	@SuppressWarnings("unchecked")
	private void setupIndex() {
		try {
			Gson gson = new Gson();
			Yaml yaml = new Yaml();
			String documentsIndexName = IIndexInformationService.Indexes.documents;
			String suggestionsIndexName = IIndexInformationService.Indexes.suggestions;
			InputStream medlineIndexSettingsStream = getClass()
					.getResourceAsStream("/semedicoElasticSearchSetup/medlineIndexSettings.json");
			InputStream suggestionIndexSettingsStream = getClass()
					.getResourceAsStream("/semedicoElasticSearchSetup/suggestionSearchIndexSettings.json");
			InputStream analyzerSettingsStream = getClass()
					.getResourceAsStream("/semedicoElasticSearchSetup/indexAnalyzerSettings.yml");
			String medlineIndexSettingString = IOUtils.toString(medlineIndexSettingsStream);
			String suggestionsIndexSettingString = IOUtils.toString(suggestionIndexSettingsStream);
			String analyzerSettingsString = IOUtils.toString(analyzerSettingsStream);

			log.debug("Applying analyzer settings:\n{}", analyzerSettingsString);
			Map<String, Object> analyzerSettings = (Map<String, Object>) yaml
					.load(new StringReader(analyzerSettingsString));

			Map<String, Object> settings = gson.fromJson(medlineIndexSettingString, Map.class);
			// adapt shard numbers so we get less directories; this is really a
			// small test index
			((Map<String, Object>) settings.get("settings")).put("number_of_shards", 1);

			// delete old index
			final IndicesExistsResponse res = esClient.admin().indices().prepareExists(documentsIndexName).execute().actionGet();
			log.info("Index {} already exists: {}", documentsIndexName, res.isExists());
			if (res.isExists()) {
				final DeleteIndexRequestBuilder delIdx = esClient.admin().indices().prepareDelete(documentsIndexName);
				delIdx.execute().actionGet();
			}

			final CreateIndexRequestBuilder createIndexRequestBuilder = esClient.admin().indices()
					.prepareCreate(documentsIndexName);
			createIndexRequestBuilder.setSettings(analyzerSettings);
			// createIndexRequestBuilder.setSettings(settings.get("settings"));

			// MAPPING DONE
			createIndexRequestBuilder.execute().actionGet();

			{
			PutMappingRequestBuilder putMappingRequestBuilder = esClient.admin().indices().preparePutMapping(documentsIndexName);
			Map<String, Object> mappingsMap = (Map<String, Object>) settings.get("mappings");
			Map<String, Object> medlineMappingMap = (Map<String, Object>) mappingsMap
					.get(IIndexInformationService.Indexes.DocumentTypes.medline);
			String medlineMappingJson = gson.toJson(medlineMappingMap);
			putMappingRequestBuilder.setType(IIndexInformationService.Indexes.DocumentTypes.medline);
			putMappingRequestBuilder.setSource(StringUtils.normalizeSpace(medlineMappingJson));
			putMappingRequestBuilder.execute().actionGet();
			
			
			Map<String, Object> pmcMappingMap = (Map<String, Object>) mappingsMap
					.get(IIndexInformationService.Indexes.DocumentTypes.pmc);
			String pmcMappingJson = gson.toJson(pmcMappingMap);
			putMappingRequestBuilder.setType(IIndexInformationService.Indexes.DocumentTypes.pmc);
			putMappingRequestBuilder.setSource(StringUtils.normalizeSpace(pmcMappingJson));
			putMappingRequestBuilder.execute().actionGet();
			}

			PutMappingRequestBuilder putMappingRequestBuilder = esClient.admin().indices().preparePutMapping(suggestionsIndexName);
			Map<String, Object> suggestionSettings = gson.fromJson(suggestionsIndexSettingString, Map.class);
			Map<String, Object> mappingsMap = (Map<String, Object>) suggestionSettings.get("mappings");
			Map<String, Object> suggItemMappingMap = (Map<String, Object>) mappingsMap
					.get(IIndexInformationService.Indexes.SuggestionTypes.item);
			String suggItemMappingJson = gson.toJson(suggItemMappingMap);
			putMappingRequestBuilder.setType(IIndexInformationService.Indexes.SuggestionTypes.item);
			putMappingRequestBuilder.setSource(StringUtils.normalizeSpace(suggItemMappingJson));
			putMappingRequestBuilder.execute().actionGet();
			
		} catch (ElasticsearchException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void indexDocuments(String directory, String indexName, String type, String idField) throws IOException {
		log.info("Indexing some development documents");
		File dir = new File(directory);
		File[] files = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".json") || name.endsWith(".json.gz");
			}
		});
		if (!dir.exists() || null == files) {
			log.warn(
					"The directory {} could not be found or it does not contain any files. No documents from this location will be added to the index.",
					dir.getAbsolutePath());
			return;
		}
		log.info("Indexing {} test documents from directory {}", files.length, dir.getAbsolutePath());
		BulkRequest br = new BulkRequest();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			String json;
			if (file.getName().endsWith(".json")) {
				json = FileUtils.readFileToString(file, "UTF-8");
			} else if (file.getName().endsWith(".json.gz")) {
				GZIPInputStream is = new GZIPInputStream(new FileInputStream(file));
				json = IOUtils.toString(is, "UTF-8");
			} else {
				log.debug("File {} was skipped because its extensions do not seem to hin a JSON file", file);
				continue;
			}
			Map<String, Object> document = gson.fromJson(json, Map.class);
			String docId = (String) document.get(idField);
			document.put("_id", docId);

			IndexRequest ir = new IndexRequest(indexName).refresh(true);
			ir.source(document);
			ir.type(type);
			if (document.get("_id") != null) {
				ir.id((String) document.get("_id"));
			}
			br.add(ir);
		}
		ActionFuture<BulkResponse> future = getSearchClient().getClient().bulk(br);
		try {
			BulkResponse response = future.get();
			if (response.hasFailures()) {
				log.error("Error while indexing: {}", response.buildFailureMessage());
			}
			log.info("Refreshing index to make documents immediately accessible.");
			getSearchClient().getClient().admin().indices().prepareRefresh(indexName).execute().actionGet();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		log.info("Done indexing");
	}

	@Override
	public ISearchClient getSearchClient() {
		return new ISearchClient() {

			@Override
			public void shutdown() {
				shutdown();
			}

			@Override
			public Client getClient() {
				return esClient;
			}
		};
	}

	private void createLocalNodeClient() {
		if (esClient == null) {
			node = nodeBuilder().clusterName("miniDevelopmentEsCluster").data(true).local(true)
					.settings(Settings.settingsBuilder().put("http.enabled", false).put("path.data",
							"src/test/resources/estestindex"))
					.node();
			esClient = node.client();

			try {
				// wait a moment to let the node recover existing indices
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@PostInjection
	public void startupService(RegistryShutdownHub shutdownHub) {
		shutdownHub.addRegistryShutdownListener(new Runnable() {
			public void run() {
				log.info("Shutting down elastic search clients.");
				shutdown();
			}
		});
	}

	public void shutdown() {
		node.close();

	}

}
