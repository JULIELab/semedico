package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import de.julielab.semedico.core.search.query.AbstractSemedicoElasticQuery;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.ElasticSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerRequest;
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
public class SearchServerRequestCreationComponent extends AbstractSearchComponent<SemedicoESSearchCarrier> {

	public SearchServerRequestCreationComponent(Logger log) {
		super(log);
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface SearchServerRequestCreation {
		//
	}

	@Override
	protected boolean processSearch(SemedicoESSearchCarrier searchCarrier) {
		SemedicoESSearchCarrier carrier = searchCarrier;
		if (carrier.getQueries().size() != carrier.getTranslatedQueries().size())
			throw new IllegalStateException("There are " + carrier.getQueries().size() + " queries but "
					+ carrier.getTranslatedQueries().size() + " queries translated for ElasticSearch.");

		for (int i = 0; i < carrier.getQueries().size(); ++i) {
			AbstractSemedicoElasticQuery semedicoQuery = carrier.getQueries().get(i);
			TranslatedQuery translatedQuery = carrier.getTranslatedQueries().get(i);

			SearchServerRequest request = new SearchServerRequest();
			request.index = semedicoQuery.getIndex();
			request.query = translatedQuery.query;
			request.namedQueries = translatedQuery.namedQueries;
			
			request.aggregationRequests = semedicoQuery.getAggregationRequests();
			request.fieldsToReturn = semedicoQuery.getRequestedFields();
			request.hlCmds = semedicoQuery.getHlCmd() != null ? Arrays.asList(semedicoQuery.getHlCmd()) : null;

			carrier.addSearchServerRequest(request);
		}

		return false;
	}

}
