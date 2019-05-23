package de.julielab.semedico.resources;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.slf4j.Logger;

import de.julielab.scicopia.core.elasticsearch.legacy.ISearchClient;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchClientProvider;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public class IndexDocumentIdsWriter implements IIndexDocumentIdsWriter {

	private Logger log;
	private Client client;
	private String documentsIndex;

	public IndexDocumentIdsWriter(Logger log, ISearchClientProvider searchClientProvider,
			@Symbol(SemedicoSymbolConstants.DOCUMENTS_INDEX_NAME) String documentsIndex) {
		this.log = log;
		this.documentsIndex = documentsIndex;
		ISearchClient semedicoSearchClient = searchClientProvider.getSearchClient();
		client = semedicoSearchClient.getClient();
	}

	@Override
	public void writeDocumentIdsInIndex() throws FileNotFoundException, IOException {
		log.info("Retrieving document IDs for index \"{}\".", documentsIndex);
		SearchRequestBuilder srb = client.prepareSearch(documentsIndex)
				.addStoredField(IIndexInformationService.MedlineIndexStructure.pmid)
				.addStoredField(IIndexInformationService.PmcIndexStructure.pmcid);
		log.info("Scroll size for ID retrieval is 500.");
		srb.setScroll(TimeValue.timeValueMinutes(1)).setSize(500).setQuery(new MatchAllQueryBuilder())
				.addSort(SortBuilders.fieldSort("_doc"));

		SearchResponse scrollResp = srb.execute().actionGet();
		String scrollId = scrollResp.getScrollId();
		try (FileOutputStream pmids = new FileOutputStream(IIndexInformationService.Indexes.DocumentTypes.MEDLINE + ".ids");
				FileOutputStream pmcids = new FileOutputStream(IIndexInformationService.Indexes.DocumentTypes.PMC + ".ids")) {
			do {
				for (SearchHit hit : scrollResp.getHits()) {
					switch (hit.getType()) {
					case IIndexInformationService.Indexes.DocumentTypes.MEDLINE:
						IOUtils.write(hit.field(IIndexInformationService.MedlineIndexStructure.pmid).getValue() + "\n", pmids,
								"UTF-8");
						break;
					case IIndexInformationService.Indexes.DocumentTypes.PMC:
						IOUtils.write(hit.field(IIndexInformationService.MedlineIndexStructure.pmcid).getValue() + "\n", pmcids,
								"UTF-8");
						break;
					default:
						throw new IllegalStateException("Unhandeled document type: " + hit.getType());
					}
				}

				scrollResp = client.prepareSearchScroll(scrollId).setScroll(TimeValue.timeValueMinutes(5)).execute()
						.actionGet();
			} while (null != scrollResp && scrollResp.getHits().getHits().length > 0);
		}
	}

}
