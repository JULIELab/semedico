package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.ISearchServerDocument;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.query.ArticleQuery;
import de.julielab.semedico.core.search.results.ArticleSearchResult;
import de.julielab.semedico.core.services.interfaces.IDocumentService;

public class ArticleResponseProcessComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface ArticleResponseProcess {
	}

	private final IDocumentService documentService;

	public ArticleResponseProcessComponent(Logger log, IDocumentService documentService) {
		super(log);
		this.documentService = documentService;

	}

	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		@SuppressWarnings("unchecked")
		SemedicoSearchCarrier<ArticleQuery, ArticleSearchResult> semCarrier = (SemedicoSearchCarrier<ArticleQuery, ArticleSearchResult>) searchCarrier;
		ISearchServerResponse solrResponse = semCarrier.getSingleSearchServerResponse();
		if (null == solrResponse)
			throw new IllegalArgumentException("The search server response must not be null, but it is.");
		String documentId = semCarrier.query.getArticleId();
		if (documentId == null)
			throw new IllegalArgumentException(
					"The document ID for the article to be searched is expected, but the ID has not been set.");

		semCarrier.result = new ArticleSearchResult();

		List<ISearchServerDocument> docList = solrResponse.getDocumentResults().collect(Collectors.toList());

		if (null != docList && solrResponse.getNumFound() > 0) {
			if (docList.size() == 0)
				throw new IllegalArgumentException(
						"Results have been found but not returned. Assure the search has been configuration to return more than zero rows.");
			ISearchServerDocument serverDoc = docList.get(0);
			HighlightedSemedicoDocument semedicoDoc = documentService.getHighlightedSemedicoDocument(serverDoc);

			semCarrier.result.article = semedicoDoc;
		} else {
			log.warn("Document with ID \"{}\" was queried from Solr but no result has been returned.", documentId);
		}

		return false;
	}
}
