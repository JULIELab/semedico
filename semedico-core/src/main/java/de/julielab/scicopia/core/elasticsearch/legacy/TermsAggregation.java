package de.julielab.scicopia.core.elasticsearch.legacy;

import java.util.ArrayList;
import java.util.List;

/**
 * The terms aggregation is basically faceting on term level.
 * 
 * see http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations-bucket-terms-aggregation.html
 * @author faessler
 * 
 */
public class TermsAggregation extends AggregationCommand {
	/**
	 * The field from which to retrieve terms to aggregate over.
	 */
	public String field;
	/**
	 * Specifies the ordering of the aggregation buckets. Defaults to bucket size.
	 */
	public List<OrderCommand> order;
	
	/**
	 * The number of top-terms to be used for aggregation.
	 */
	public Integer size;

	public void addOrder(OrderCommand orderItem) {
		if (null == order)
			order = new ArrayList<>();
			order.add(orderItem);
	}
}
