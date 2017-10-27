package de.julielab.semedico.core.search.query;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest.OrderCommand;
import de.julielab.semedico.core.search.components.FieldTermsRetrievalPreparationComponent;

/**
 * To be used in search chains for the retrieval of terms in an index field. The
 * component {@link FieldTermsRetrievalPreparationComponent} works with this
 * query.
 * 
 * @author faessler
 *
 */
public class FieldTermsQuery extends WrappingQuery {
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

	private String field;
	private int size;
	public OrderCommand.SortOrder sortOrder;
	public OrderType orderType;

	public FieldTermsQuery(ISemedicoQuery wrappedQuery, String field, int size) {
		super(wrappedQuery);
		this.field = field;
		this.size = size;
	}

	public String getField() {
		return field;
	}

	/**
	 * The kind of order that is imposed on the returned field terms. The
	 * ElasticSearch default is {@link OrderType#COUNT}.
	 * 
	 * @return The order type for this field term query.
	 */
	public OrderCommand.SortOrder getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(OrderCommand.SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}

	public void setField(String field) {
		this.field = field;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
