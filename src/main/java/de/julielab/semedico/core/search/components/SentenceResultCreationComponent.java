package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.search.results.SentenceSearchResult;
import de.julielab.semedico.core.search.results.StatementSearchResult;

public class SentenceResultCreationComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface StatementResultCreation {
		//
	}

	private Logger log;
	
	public SentenceResultCreationComponent(Logger log) {
		super(log);
	}
	
	@Override
	protected boolean processSearch(SearchCarrier searchCarrier) {
		 ISearchServerResponse serverResponse = searchCarrier.getSingleSearchServerResponse();
		SentenceSearchResult result = new SentenceSearchResult();
		// TODO continue
		return false;
	}

}
