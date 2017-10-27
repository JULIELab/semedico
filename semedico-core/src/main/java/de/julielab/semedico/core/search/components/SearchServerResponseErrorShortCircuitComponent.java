package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.services.SemedicoCoreModule;

/**
 * This component terminates the command chain of search components in case a server response is null which points to an error.
 * @author faessler
 *
 */
public class SearchServerResponseErrorShortCircuitComponent extends AbstractSearchComponent{

	@Retention(RetentionPolicy.RUNTIME)
	public @interface SearchServerResponseErrorShortCircuit {
		//
	}
	
	public SearchServerResponseErrorShortCircuitComponent(Logger log) {
		super(log);
	}

	@Override
	protected boolean processSearch(SearchCarrier searchCarrier) {
		for (ISearchServerResponse response : searchCarrier.serverResponses) {
			if (response.hasQueryError()) {
				SemedicoCoreModule.searchTraceLog.error("Terminating current search chain because a search server response has an error: {}, {}", response.getQueryError(), response.getQueryErrorMessage());
				return true;
			}
		}
		return false;
	}

}
