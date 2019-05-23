package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import org.slf4j.Logger;

import com.google.common.collect.Lists;

import de.julielab.scicopia.core.elasticsearch.legacy.AbstractSearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchServerDocument;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchServerResponse;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchCarrier;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
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
	this.log = log;
	this.documentService = documentService;
}
	
	@Override
	protected boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		ISearchServerResponse serverResponse = semCarrier.getSingleSearchServerResponse();
		if (null == serverResponse)
			throw new IllegalArgumentException("The search server response must not be null, but it is.");
		LegacySemedicoSearchResult searchResult = semCarrier.getResult();
		if (null == searchResult) {
			searchResult = new LegacySemedicoSearchResult(semCarrier.getSearchCommand().getSemedicoQuery());
			semCarrier.setResult(searchResult);
		}

		List<HighlightedSemedicoDocument> documentHits = Lists.newArrayList();

		List<ISearchServerDocument> solrDocs = serverResponse.getDocumentResults();
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

		LazyDisplayGroup<HighlightedSemedicoDocument> displayGroup = new LazyDisplayGroup<>(
				(int) serverResponse.getNumFound(), SemedicoSearchConstants.MAX_DOCS_PER_PAGE,
				SemedicoSearchConstants.MAX_BATCHES, documentHits);
		searchResult.documentHits = displayGroup;

		return false;
	}

}
