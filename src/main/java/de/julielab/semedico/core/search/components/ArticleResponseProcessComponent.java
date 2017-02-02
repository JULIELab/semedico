package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.ISearchServerDocument;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.HighlightedSemedicoDocument;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.services.interfaces.IDocumentService;

public class ArticleResponseProcessComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface ArticleResponseProcess {
	}
	private final IDocumentService documentService;
	private final Logger log;

	public ArticleResponseProcessComponent(Logger log,
			IDocumentService documentService) {
		this.log = log;
		this.documentService = documentService;

	}


	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		ISearchServerResponse solrResponse = semCarrier.getSingleSearchServerResponse();
		if (null == solrResponse)
			throw new IllegalArgumentException(
					"The solr response must not be null, but it is.");
		String documentId = semCarrier.searchCmd.documentId;
		if (documentId == null)
			throw new IllegalArgumentException(
					"The document ID for the article to be searched is expected, but the ID has not been set.");

		LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) semCarrier.result;
		if (null == searchResult) {
			searchResult = new LegacySemedicoSearchResult(semCarrier.searchCmd.semedicoQuery);
			semCarrier.result = searchResult;
		}

		List<ISearchServerDocument> docList = solrResponse.getDocumentResults();

		if (null != docList && solrResponse.getNumFound() > 0) {
			if (docList.size() == 0)
				throw new IllegalArgumentException(
						"Results have been found but not returned. Assure the search has been configuration to return more than zero rows.");
			ISearchServerDocument serverDoc = docList.get(0);
			Map<String, Map<String, List<String>>> highlighting = solrResponse
					.getHighlighting();
			Map<String, List<String>> docHighlights = null;
			if (null != highlighting)
				docHighlights = highlighting.get(String.valueOf(documentId));
			HighlightedSemedicoDocument semedicoDoc = documentService
					.getHighlightedSemedicoDocument(serverDoc);

			searchResult.semedicoDoc = semedicoDoc;
		} else {
			log.warn(
					"Document with ID \"{}\" was queried from Solr but no result has been returned.",
					documentId);
		}

		return false;
	}
}
