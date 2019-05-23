package de.julielab.scicopia.core.elasticsearch.legacy;

/**
 * This aggregation returns the maximum value of the specified field across documents matched by the main query.
 * 
 * see http
 *      ://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations-metrics-max-aggregation
 *      .html
 * @author faessler
 * 
 */
public class MaxAggregation extends AggregationCommand {
	/**
	 * The field to compute and return the maximum value for.
	 */
	public String field;
	/**
	 * A script to computer and return the maximum value.
	 * 
	 * see http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/modules-scripting.html
	 */
	public String script;
}
