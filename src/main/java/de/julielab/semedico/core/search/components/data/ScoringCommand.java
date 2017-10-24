package de.julielab.semedico.core.search.components.data;

import de.julielab.elastic.query.components.data.SearchServerRequest;

/**
 * This class is meant as a part of {@link SearchServerRequest}. It determines
 * settings concerned with the scoring of search results.
 * 
 * @author faessler
 * @deprecated User FunctionScoreQuery; Explanation: This command has been
 *             created as an ElasticSearch-independent alternative to the
 *             FunctionScoreQuery of ElasticSearch. However, the
 *             FunctionScoreQuery can used in nested queries (e.g. BoolQuery)
 *             and not just as a scoring mechanism for the whole query (like this command). Thus, it
 *             is much more versatile.
 */
@Deprecated
public class ScoringCommand {
	/**
	 * Eligible values for the {@link ScoringCommand#weightFieldStrategy} field.
	 * 
	 * @see http
	 *      ://www.elasticsearch.org/guide/en/elasticsearch/reference/current
	 *      /query-dsl-function-score-query.html, section 'Parameter
	 *      Description'
	 * 
	 */
	public enum FieldValueFactorModifier {
		NONE, LOG, LOG1P, LOG2P, LN, LN1P, LN2P, SQUARE, SQRT, RECIPROCAL
	}

	/**
	 * A field holding a number to be used for scoring the respective document.
	 * This field must be present in all documents.
	 */
	public String weightField;
	/**
	 * The strategy <em>how</em> the value of {@link #weightField} should be
	 * used. Requires {@link #weightField} to be set. Eligible strategies are to
	 * be found in {@link FieldValueFactorModifier}.
	 */
	public FieldValueFactorModifier weightFieldStrategy;
}
