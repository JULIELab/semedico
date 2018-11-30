package de.julielab.semedico.core.search.query;

import com.google.common.collect.Multimap;
import de.julielab.elastic.query.components.data.FieldTermItem.ValueType;
import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.elastic.query.components.data.aggregation.AggregationRequest.OrderCommand;
import de.julielab.elastic.query.components.data.aggregation.AggregationRequest.OrderCommand.SortOrder;
import de.julielab.elastic.query.components.data.aggregation.MaxAggregation;
import de.julielab.elastic.query.components.data.aggregation.TermsAggregation;
import de.julielab.semedico.core.entities.state.UserInterfaceState;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.search.results.collectors.FieldTermCollector;
import de.julielab.semedico.core.services.UIService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A convenience method to access commonly required aggregation requests easily.
 *
 * @author faessler
 */
public class AggregationRequests {

    /**
     * Creates an request for field terms of the documents matching a query when
     * searching. This is the most general version of the method that takes the
     * most parameters.
     *
     * @param aggregationName The name of the aggregation. Used, amongst others, by the
     *                        {@link FieldTermCollector} to get the right field.
     * @param fieldname       The field for which to extract terms.
     * @param termNumber      The maximum number of terms to be returned.
     * @param orderType       One of {@link OrderType#COUNT}, {@link OrderType#TERM} or
     *                        {@link OrderType#DOC_SCORE}.
     * @param sortOrder       {@link SortOrder#ASCENDING} or {@link SortOrder#DESCENDING}
     * @return An aggregation request that will request field terms from the
     * specified field in the specified order. Should be collected by a
     * {@link FieldTermCollector}.
     */
    public static AggregationRequest getFieldTermsRequest(String aggregationName, String fieldname, int termNumber,
                                                          OrderType orderType, SortOrder sortOrder) {
        // We will get the field terms by a TermsAggregation.
        TermsAggregation terms = new TermsAggregation();
        terms.field = fieldname;
        terms.name = aggregationName;
        terms.size = termNumber;

        OrderCommand orderCmd = new OrderCommand();
        orderCmd.sortOrder = sortOrder;
        switch (orderType) {
            case COUNT:
                orderCmd.referenceType = OrderCommand.ReferenceType.COUNT;
                break;
            case DOC_SCORE:
                orderCmd.referenceType = OrderCommand.ReferenceType.AGGREGATION_SINGLE_VALUE;
                // The name of the aggregation that will hold the (maximum) document
                // score for the terms to sort them by
                orderCmd.referenceName = ValueType.MAX_DOC_SCORE.name();
                // now we also have to create the respective aggregation to get the
                // document score in the first place
                MaxAggregation maxAgg = new MaxAggregation();
                maxAgg.name = ValueType.MAX_DOC_SCORE.name();
                maxAgg.script = "_score";
                terms.addSubaggregation(maxAgg);
                break;
            case TERM:
                orderCmd.referenceType = OrderCommand.ReferenceType.TERM;
                break;
        }
        terms.addOrder(orderCmd);
        return terms;
    }

    /**
     * Creates an request for field terms of the documents matching a query when
     * searching. This method returns the field terms in descending order by the
     * document score a term was found in.
     *
     * @param aggregationName The name of the aggregation. Used, amongst others, by the
     *                        {@link FieldTermCollector} to get the right field.
     * @param fieldname       The field for which to extract terms.
     * @param termNumber      The maximum number of terms to be returned.
     * @return A request for an aggregation of doc score sorted field terms.
     */
    public static AggregationRequest getFieldTermsByDocScoreRequest(String aggregationName, String fieldname,
                                                                    int termNumber) {
        return getFieldTermsRequest(aggregationName, fieldname, termNumber, OrderType.DOC_SCORE, SortOrder.DESCENDING);
    }

    /**
     * Creates an request for field terms of the documents matching a query when
     * searching. This method returns ALL the field terms in descending order by
     * the document score a term was found in.
     *
     * @param aggregationName The name of the aggregation. Used, amongst others, by the
     *                        {@link FieldTermCollector} to get the right field.
     * @param fieldname       The field for which to extract terms.
     * @return A request for an aggregation of doc score sorted field terms.
     */
    public static AggregationRequest getFieldTermsByDocScoreRequest(String aggregationName, String fieldname) {
        return getFieldTermsByDocScoreRequest(aggregationName, fieldname, Integer.MAX_VALUE);
    }

    public static List<AggregationRequest> getFacetGroupRequest(List<UIFacet> facets, UIService uiService, IElasticQuery query, UserInterfaceState uiState) {
        List<AggregationRequest> requests = new ArrayList<>(facets.size());
        Multimap<UIFacet, String> uncountedConceptIdsForFacets = uiState.getLabelStore().getUncountedConceptIdsForFacets(facets, uiService, query, true);
        for (UIFacet facet : facets) {
            TermsAggregation conceptIdCountAggregation = new TermsAggregation();
            conceptIdCountAggregation.include = uncountedConceptIdsForFacets.get(facet);
            conceptIdCountAggregation.field = facet.getSource().getName();
            conceptIdCountAggregation.name = "conceptcounts:" + facet.getId();
            requests.add(conceptIdCountAggregation);
        }
        return requests;
    }

    public static AggregationRequest getFacetRequest(UIFacet facet, UIService uiService, IElasticQuery query, UserInterfaceState uiState) {
        List<AggregationRequest> facetGroupRequest = getFacetGroupRequest(Collections.singletonList(facet), uiService, query, uiState);
        return facetGroupRequest.isEmpty() ? null : facetGroupRequest.get(0);
    }

    public enum OrderType {
        /**
         * Sort the term buckets (each field term will constitute its own
         * "aggregation bucket") alphabetically.
         */
        TERM,
        /**
         * Sort the term buckets by the frequency of the terms in each bucket.
         */
        COUNT,
        /**
         * Sort the terms by the document score. This does not work
         * out-of-the-box but requires a subaggregation the exhibits the
         * document score that is then used for sorting. The
         * {@link AggregationRequests#getFieldTermsByDocScoreRequest(String, String, int)}
         * method handles this.
         */
        DOC_SCORE
    }
}
