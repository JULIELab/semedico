package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.search.results.StatementSearchResult;
import de.julielab.semedico.core.services.interfaces.IDocumentService;

public class StatementResultCreationComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface StatementResultCreation {
		//
	}

	private Logger log;
	private IDocumentService documentService;
	
	public StatementResultCreationComponent(Logger log, IDocumentService documentService) {
		super(log);
		this.documentService = documentService;
	}
	
	@Override
	protected boolean processSearch(SearchCarrier searchCarrier) {
		 ISearchServerResponse serverResponse = searchCarrier.getSingleSearchServerResponse();
		StatementSearchResult result = new StatementSearchResult();
		return false;
	}

}
