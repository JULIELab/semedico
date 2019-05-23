package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import org.slf4j.Logger;

import de.julielab.scicopia.core.elasticsearch.legacy.AbstractSearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchServerDocument;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchServerResponse;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchCarrier;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;
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
		if (null == solrResponse) {
			throw new IllegalArgumentException(
					"The solr response must not be null, but it is.");
		}
		String documentId = semCarrier.getSearchCommand().getDocumentId();
		if (documentId == null) {
			throw new IllegalArgumentException(
					"The document ID for the article to be searched is expected, but the ID has not been set.");
		}
		
		LegacySemedicoSearchResult searchResult = semCarrier.getResult();
		if (null == searchResult) {
			searchResult = new LegacySemedicoSearchResult(semCarrier.getSearchCommand().getSemedicoQuery());
			semCarrier.setResult(searchResult);
		}

		List<ISearchServerDocument> docList = solrResponse.getDocumentResults();

		if (null != docList && solrResponse.getNumFound() > 0) {
			if (docList.isEmpty()) {
				throw new IllegalArgumentException(
						"Results have been found but not returned. Assure the search has been configuration to return more than zero rows.");
			}
			ISearchServerDocument serverDoc = docList.get(0);
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
