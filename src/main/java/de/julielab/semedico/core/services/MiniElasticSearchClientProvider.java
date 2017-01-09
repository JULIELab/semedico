package de.julielab.semedico.core.services;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;

import de.julielab.elastic.query.services.ISearchClientProvider;
import de.julielab.elastic.query.services.ISearchClient;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

/**
 * Returns a client to a local, embedded search index with a few small
 * documents. Does not require an external ElasticSearch server. This is meant
 * for easy development.
 * 
 * @author faessler
 * 
 */
public class MiniElasticSearchClientProvider implements ISearchClientProvider {

	private Client esClient;
	private Logger log;
	private Node node;

	public MiniElasticSearchClientProvider(Logger log) {
		this.log = log;
		createLocalNodeClient();
		setupIndex();
		 indexDocuments();
	}

	private void setupIndex() {
		try {
			String indexName = IIndexInformationService.Indexes.documents;
			String documentType = "doc";

			// delete old index
			final IndicesExistsResponse res = esClient.admin().indices().prepareExists(indexName)
					.execute().actionGet();
			log.info("Index {} already exists: {}", indexName, res.isExists());
			if (res.isExists()) {
				final DeleteIndexRequestBuilder delIdx = esClient.admin().indices()
						.prepareDelete(indexName);
				delIdx.execute().actionGet();
			}

			final CreateIndexRequestBuilder createIndexRequestBuilder = esClient.admin().indices()
					.prepareCreate(indexName);
			// MAPPING GOES HERE

			final XContentBuilder mappingBuilder = jsonBuilder().startObject()
					.startObject(documentType).startObject("properties").startObject("title")
					.field("type", "string").field("term_vector", "with_positions_offsets")
					.field("store", true).endObject().startObject("pubmedID")
					.field("type", "string").field("store", true).endObject()
					.startObject("abstract").field("type", "string")
					.field("term_vector", "with_positions_offsets").field("store", true)
					.endObject().endObject().endObject().endObject();
			createIndexRequestBuilder.addMapping(documentType, mappingBuilder);

			// MAPPING DONE
			createIndexRequestBuilder.execute().actionGet();

		} catch (ElasticsearchException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void indexDocuments() {
		log.info("Indexing a few mini documents");
		List<Map<String, Object>> documents = new ArrayList<>();
		Map<String, Object> indexDoc;

		indexDoc = new HashMap<>();
		indexDoc.put("_id", "0");
		indexDoc.put("pubmedID", "0");
		indexDoc.put("title", "Newest achievements against Alzeimer");
		indexDoc.put("abstract",
				"We report a new method to mitigate the symptoms shown by Alzheimer patients.");
		indexDoc.put("date", "2000");
		documents.add(indexDoc);

		indexDoc = new HashMap<>();
		indexDoc.put("_id", "1");
		indexDoc.put("pubmedID", "1");
		indexDoc.put("title", "Mice behaviour in mazes under the influence of beta-blockers.");
		indexDoc.put(
				"abstract",
				"We apply 1mg Atenolol to adult mice in an effort to investigate the effects on the orientation of the animals.");
		indexDoc.put("date", "2001");
		documents.add(indexDoc);

		Iterator<Map<String, Object>> documentIterator = documents.iterator();
		BulkRequest br = new BulkRequest();
		String indexName = IIndexInformationService.Indexes.documents;
		while (documentIterator.hasNext()) {
			Map<String, Object> doc = documentIterator.next();

			IndexRequest ir = new IndexRequest(indexName).refresh(true);
			ir.source(doc);
			ir.type("doc");
			if (doc.get("_id") != null)
				ir.id((String) doc.get("_id"));
			br.add(ir);
		}
		ActionFuture<BulkResponse> future = esClient.bulk(br);
		try {
			BulkResponse response = future.get();
			if (response.hasFailures()) {
				log.error("Error while indexing: {}", response.buildFailureMessage());
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		// for indexing to be visible for searchers
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
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
			node = nodeBuilder()
					.clusterName("miniDevelopmentEsCluster")
					.data(true)
					.local(true)
					.settings(
							Settings.settingsBuilder().put("http.enabled", false)
									.put("path.data", "src/test/resources/estestindex")).node();
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
