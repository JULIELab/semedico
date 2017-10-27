package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.ElasticSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerRequest;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.query.TranslatedQuery;

/**
 * This component is the bridge between the {@link QueryTranslationComponent}
 * and the {@link ElasticSearchComponent}. It reads the {@link TranslatedQuery}
 * instances from the first and created {@link SearchServerRequest} objects for
 * the latter.
 * 
 * @author faessler
 *
 */
public class SearchServerRequestCreationComponent extends AbstractSearchComponent {

	public SearchServerRequestCreationComponent(Logger log) {
		super(log);
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface SearchServerRequestCreation {
		//
	}

	@Override
	protected boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier carrier = (SemedicoSearchCarrier) searchCarrier;
		if (carrier.queries.size() != carrier.translatedQueries.size())
			throw new IllegalStateException("There are " + carrier.queries.size() + " queries but "
					+ carrier.translatedQueries.size() + " queries translated for ElasticSearch.");

		for (int i = 0; i < carrier.queries.size(); ++i) {
			ISemedicoQuery semedicoQuery = carrier.queries.get(i);
			TranslatedQuery translatedQuery = carrier.translatedQueries.get(i);

			SearchServerRequest request = new SearchServerRequest();
			request.index = semedicoQuery.getIndex();
			request.query = translatedQuery.query;
			request.namedQueries = translatedQuery.namedQueries;
			
			request.aggregationRequests = semedicoQuery.getAggregationRequests();
			request.fieldsToReturn = semedicoQuery.getRequestedFields();
//			request.hlCmds = // TODO

			carrier.addSearchServerRequest(request);
		}

		return false;
	}

}
