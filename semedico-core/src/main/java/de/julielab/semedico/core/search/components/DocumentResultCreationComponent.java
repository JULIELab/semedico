package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.google.common.collect.Lists;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.ISearchServerDocument;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import de.julielab.semedico.core.services.SemedicoSearchConstants;
import de.julielab.semedico.core.services.interfaces.IDocumentService;
import de.julielab.semedico.core.util.LazyDisplayGroup;

public class DocumentResultCreationComponent extends AbstractSearchComponent {

	
	private Logger log;
	private IDocumentService documentService;

	@Retention(RetentionPolicy.RUNTIME)
	public @interface DocumentResultCreation {
		//
	}
	
public DocumentResultCreationComponent(Logger log, IDocumentService documentService) {
	super(log);
	this.documentService = documentService;
}
	
	@Override
	protected boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoESSearchCarrier semCarrier = (SemedicoESSearchCarrier) searchCarrier;
		ISearchServerResponse serverResponse = semCarrier.getSingleSearchServerResponse();
		if (null == serverResponse)
			throw new IllegalArgumentException("The search server response must not be null, but it is.");
		// TODO not this way any more
		LegacySemedicoSearchResult searchResult = null;//(LegacySemedicoSearchResult) semCarrier.result;
		if (null == searchResult) {
			searchResult = new LegacySemedicoSearchResult(semCarrier.searchCmd.semedicoQuery);
			// TODO not this way any more
//			semCarrier.result = searchResult;
		}

		List<HighlightedSemedicoDocument> documentHits = Lists.newArrayList();

		// TODO repair
		List<ISearchServerDocument> solrDocs = null;//serverResponse.getDocumentResults().collect(Collectors.toList());
		log.debug("Retrieved {} documents for display, {} documents hits overall.", solrDocs.size(),
				serverResponse.getNumFound());
		searchResult.totalNumDocs = serverResponse.getNumFound();
		for (ISearchServerDocument solrDoc : solrDocs) {
			// Is it possible to highlight corresponding to the user input and
			// return fragments for each term hit instead of returning multiple
			// snippets for the same term?
			HighlightedSemedicoDocument documentHit = documentService.getHitListDocument(solrDoc);
			documentHits.add(documentHit);
		}

		LazyDisplayGroup<HighlightedSemedicoDocument> displayGroup = new LazyDisplayGroup<HighlightedSemedicoDocument>(
				(int) serverResponse.getNumFound(), SemedicoSearchConstants.MAX_DOCS_PER_PAGE,
				SemedicoSearchConstants.MAX_BATCHES, documentHits);
		searchResult.documentHits = displayGroup;

		return false;
	}

}
