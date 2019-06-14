package de.julielab.semedico.core.search.components;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.search.results.SentenceSearchResult;
import org.slf4j.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
		// TODO repair
		 ISearchServerResponse serverResponse = null;//searchCarrier.getSingleSearchServerResponse();
		SentenceSearchResult result = new SentenceSearchResult();
		// TODO continue
		return false;
	}

}
