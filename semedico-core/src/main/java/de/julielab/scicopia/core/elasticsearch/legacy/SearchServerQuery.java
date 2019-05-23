package de.julielab.scicopia.core.elasticsearch.legacy;

import de.julielab.scicopia.core.elasticsearch.legacy.SearchServerCommand;
import org.elasticsearch.index.query.BoolQueryBuilder;


/**
 * <p>
 * An abstract super class for query objects. For each {@link SearchServerCommand}, one such object is given to form the
 * central element of the request, the query itself. This does not include the number of documents to return, faceting
 * and such.
 * <p>
 * The classes extending this class will be inspired by the ElasticSearch query DSL
 * (http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl.html) since, at the time of writing,
 * <tt>ElasticSearch</tt> is the employed search server. Also, the <tt>ElasticSearch</tt> query DSL is well structured
 * following an object property approach, which is easily represented as Java objects, where the corresponding
 * <tt>Solr</tt> queries consist of different, not always easy to understand, elements as the query itself and
 * <tt>local parameters</tt> (https://wiki.apache.org/solr/LocalParams). Thus, when using another search server, e.g.
 * <tt>Solr</tt>, those queries must be translated in the Solr search component.
 * </p>
 * 
 * @see SearchServerCommand#query
 * 
 * @author faessler
 * 
 */
public abstract class SearchServerQuery {
	/**
	 * A boost to make this query more (&gt;1) or less (&lt;1) important than other query clauses, if used in a compound query, e.g. {@link BoolQueryBuilder}.
	 */
	public float boost = 1f;
}
