package de.julielab.semedico.core.search.searchresponse;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.elastic.query.components.data.aggregation.TermsAggregationResult;
import de.julielab.elastic.query.components.data.aggregation.TermsAggregationUnit;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.search.results.collectors.FieldTermCollector;
import de.julielab.topicmodeling.businessobjects.TMSearchResult;

/**
 * A very simple search response. There are no document bodies to retrieve, since topic models only return a list of
 * document IDs. This list is retrieved via {@link #getAggregationResult(AggregationRequest)} where the
 * <code>AggregationRequest</code> parameter is ignored. This allows to use the response together with a
 * {@link FieldTermCollector} to collect the result of a topic model search
 * when using the {@link de.julielab.semedico.core.search.services.ISearchService}.
 *
 */
public class TopicModelSearchResponse implements ISearchServerResponse, IAggregationSearchResponse {

    private TMSearchResult searchResult;
    public TopicModelSearchResponse(TMSearchResult searchResult) {
        this.searchResult = searchResult;
    }

    /**
     * Returns the IDs of the searched documents in score order.
     * @param aggCmd
     * @return
     */
    @Override
    public TermsAggregationResult getAggregationResult(AggregationRequest aggCmd) {
        TermsAggregationResult termsAggregationResult = new TermsAggregationResult();
        for (String docId : searchResult.pubmedID) {
            TermsAggregationUnit unit = new TermsAggregationUnit();
            unit.setTerm(docId);
            unit.setCount(1);
            termsAggregationResult.addAggregationUnit(unit);
        }
        return termsAggregationResult;
    }

    @Override
    public long getNumFound() {
        return 0;
    }

    @Override
    public String getQueryErrorType() {
        return null;
    }

    @Override
    public String getQueryErrorMessage() {
        return null;
    }

    @Override
    public boolean hasQueryError() {
        return false;
    }
}
