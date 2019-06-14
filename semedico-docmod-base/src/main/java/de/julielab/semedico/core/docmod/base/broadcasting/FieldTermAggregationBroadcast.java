package de.julielab.semedico.core.docmod.base.broadcasting;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.search.query.AggregationRequests;

/**
 * <p>This broadcast is for faceting terms of a single field. Which field that is, the document module has to
 * specify. The broadcast holds all other parameters to be used by {@link AggregationRequests#getFieldTermsRequest(String, String, int, AggregationRequests.OrderType, AggregationRequest.OrderCommand.SortOrder)}.</p>
 */
public class FieldTermAggregationBroadcast implements IAggregationBroadcast {
    public static final String FIELDTERMS_NAME = "fieldterms";
    private int termNumber;
    private AggregationRequests.OrderType orderType;
    private AggregationRequest.OrderCommand.SortOrder sortOrder;
    private String basename;

    /**
     * Creates an aggregation broadcast with the basename "fieldterms".
     */
    public FieldTermAggregationBroadcast() {
        basename = FIELDTERMS_NAME;
    }

    /**
     * Creates an aggregation broadcast with the basename "fieldterms".
     *
     * @param termNumber
     * @param orderType
     * @param sortOrder
     */
    public FieldTermAggregationBroadcast(int termNumber, AggregationRequests.OrderType orderType, AggregationRequest.OrderCommand.SortOrder sortOrder) {
        this();
        this.termNumber = termNumber;
        this.orderType = orderType;
        this.sortOrder = sortOrder;
    }

    /**
     * Creates an aggregation broadcast with the given basename.
     *
     * @param basename
     * @param termNumber
     * @param orderType
     * @param sortOrder
     */
    public FieldTermAggregationBroadcast(String basename, int termNumber, AggregationRequests.OrderType orderType, AggregationRequest.OrderCommand.SortOrder sortOrder) {
        this(termNumber, orderType, sortOrder);

        this.basename = basename;
    }

    public String getBasename() {
        return basename;
    }

    public void setBasename(String basename) {
        this.basename = basename;
    }

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
        return basename;
    }
}
