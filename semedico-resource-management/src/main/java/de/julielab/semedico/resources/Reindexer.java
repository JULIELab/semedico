package de.julielab.semedico.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.slf4j.Logger;

import de.julielab.scicopia.core.elasticsearch.legacy.IIndexingService;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchClient;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchClientProvider;

/**
 * Uses the ElasticSearch reindexing feature to create a new index from the
 * contents of an old index. This can be used when the _source field of the old
 * index was enabled at indexing and contains all information needed to create
 * the new index. It can be useful when just the analysis has changed. Please
 * note that the _source field will mostly be deactivated to save disc space.
 * Then, a complete fresh indexing will be necessary and this class is of no
 * use.
 * 
 * @author faessler
 *
 */
public class Reindexer implements IReindexer {

	private Logger log;
	private Client client;
	private IIndexingService indexingService;

	public Reindexer(Logger log, ISearchClientProvider searchClientProvider, IIndexingService indexingService) {
		this.log = log;
		this.indexingService = indexingService;
		ISearchClient semedicoSearchClient = searchClientProvider.getSearchClient();
		client = semedicoSearchClient.getClient();
	}

	@Override
	public void reindex(String sourceIndex, String targetIndex) throws Exception {
		SearchRequestBuilder srb = client.prepareSearch(sourceIndex);
		log.info("Reindexing batch size is 500.");
		srb.setScroll(TimeValue.timeValueMinutes(1)).setSize(500).setQuery(new MatchAllQueryBuilder())
				.addSort(SortBuilders.fieldSort("_doc"));

		// SearchResponse response = srb.execute().actionGet();
		// String scrollId = response.getScrollId();
		//
		// SearchResponse scrollResp = null;
		SearchResponse scrollResp = srb.execute().actionGet();
		String scrollId = scrollResp.getScrollId();
		do {
			Map<String, List<Map<String, Object>>> typeDocMap = new HashMap<>();
			for (SearchHit hit : scrollResp.getHits()) {
				Map<String, Object> source = hit.getSourceAsMap();
				List<Map<String, Object>> sourceList = typeDocMap.get(hit.getType());
				if (null == sourceList) {
					sourceList = new ArrayList<>();
					typeDocMap.put(hit.getType(), sourceList);
				}
				source.put("_id", hit.getId());
				sourceList.add(source);
			}

			for (String type : typeDocMap.keySet()) {
				List<Map<String, Object>> sourceList = typeDocMap.get(type);
				indexingService.indexDocuments(targetIndex, type, sourceList.iterator());
				sourceList.clear();
			}
			scrollResp = client.prepareSearchScroll(scrollId).setScroll(TimeValue.timeValueMinutes(1)).execute()
					.actionGet();
		} while (null != scrollResp && scrollResp.getHits().getHits().length > 0);

	}

}
