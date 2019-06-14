package de.julielab.semedico.core.search.components;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.QueryError;
import de.julielab.elastic.query.services.IElasticServerResponse;
import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import de.julielab.semedico.core.search.searchresponse.ElasticServerResponse;
import org.slf4j.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Converts {@link de.julielab.elastic.query.components.data.ElasticServerResponse} to {@link ElasticServerResponse}.
 */
public class SearchResultPostprocessingComponent extends AbstractSearchComponent<SemedicoESSearchCarrier> {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SearchResultPostprocessing {
        //
    }
    public SearchResultPostprocessingComponent(Logger log) {
        super(log);
    }

    @Override
    protected boolean processSearch(SemedicoESSearchCarrier elasticSearchCarrier) {
        final List<IElasticServerResponse> searchResponses = elasticSearchCarrier.getSearchResponses();
        for (int i = 0; i < searchResponses.size(); i++) {
            IElasticServerResponse iElasticServerResponse = searchResponses.get(i);
            final ElasticServerResponse semedicoEsResponse = new ElasticServerResponse((de.julielab.elastic.query.components.data.ElasticServerResponse) iElasticServerResponse);
            searchResponses.set(i, semedicoEsResponse);
        }
        for (IElasticServerResponse searchResponse : searchResponses) {
            ElasticServerResponse response = (ElasticServerResponse) searchResponse;
            final QueryError queryError = response.getQueryError();
            if (queryError != null) {
                throw new IllegalStateException("Querying Elasticsearch failed due to " + response.getQueryErrorType() + ": " + response.getQueryErrorMessage());
            }
        }
        return false;
    }
}
