package de.julielab.semedico.docmods.base.broadcasting;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.search.query.AggregationRequests;

/**
 * <p>This broadcast is for faceting terms of a single field. Which field that is, the document module has to
 * specify. The broadcast holds all other parameters to be used by {@link AggregationRequests#getFieldTermsRequest(String, String, int, AggregationRequests.OrderType, AggregationRequest.OrderCommand.SortOrder)}.</p>
 */
public class FieldTermAggregationBroadcast implements IAggregationBroadcast {
    private int termNumber;
    private AggregationRequests.OrderType orderType;
    private AggregationRequest.OrderCommand.SortOrder sortOrder;

    public int getTermNumber() {
        return termNumber;
    }

    public void setTermNumber(int termNumber) {
        this.termNumber = termNumber;
    }

    public AggregationRequests.OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(AggregationRequests.OrderType orderType) {
        this.orderType = orderType;
    }

    public AggregationRequest.OrderCommand.SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(AggregationRequest.OrderCommand.SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public String getAggregationBaseName() {
        return "fieldterms";
    }
}
