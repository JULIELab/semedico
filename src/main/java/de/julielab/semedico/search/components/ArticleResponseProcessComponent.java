package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;

import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.core.services.interfaces.IDocumentService;

public class ArticleResponseProcessComponent implements ISearchComponent {

	private final IDocumentService documentService;
	private final Logger log;

	public ArticleResponseProcessComponent(Logger log,
			IDocumentService documentService) {
		this.log = log;
		this.documentService = documentService;

	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface ArticleResponseProcess {
	}

	@Override
	public boolean process(SearchCarrier searchCarrier) {
		QueryResponse solrResponse = searchCarrier.solrResponse;
		if (null == solrResponse)
			throw new IllegalArgumentException(
					"The solr response must not be null, but it is.");
		int documentId = searchCarrier.searchCmd.documentId;
		if (documentId == Integer.MIN_VALUE)
			throw new IllegalArgumentException(
					"The document ID for the article to be searched is expected, but the ID has not been set.");

		SemedicoSearchResult searchResult = searchCarrier.searchResult;
		if (null == searchResult) {
			searchResult = new SemedicoSearchResult();
			searchCarrier.searchResult = searchResult;
		}

		SolrDocumentList docList = solrResponse.getResults();

		if (null != docList && docList.getNumFound() > 0) {
			if (docList.size() == 0)
				throw new IllegalArgumentException(
						"Results have been found but not returned. Solr's 'row' parameter must be set to greater then zero.");
			SolrDocument solrDoc = docList.get(0);
			Map<String, Map<String, List<String>>> highlighting = solrResponse
					.getHighlighting();
			Map<String, List<String>> docHighlights = null;
			if (null != highlighting)
				docHighlights = highlighting.get(String.valueOf(documentId));
			SemedicoDocument semedicoDoc = documentService
					.getHighlightedSemedicoDocument(solrDoc, docHighlights);
			//
			// String highlightedAbstract = kwicService
			// .getHighlightedAbstract(docHighlights);
			// String highlightedTitle = kwicService
			// .getHighlightedTitle(docHighlights);
			//
			// HighlightedSemedicoDocument highlightedSemedicoDoc = new
			// HighlightedSemedicoDocument(
			// semedicoDocument,
			// loggerSource.getLogger(HighlightedSemedicoDocument.class));
			// highlightedSemedicoDoc.setHighlightedTitle(highlightedTitle);
			// highlightedSemedicoDoc.setHighlightedAbstract(highlightedAbstract);

			searchResult.semedicoDoc = semedicoDoc;
		} else {
			log.warn(
					"Document with ID \"{}\" was queried from Solr but no result has been returned.",
					documentId);
		}

		return false;
	}
}
