package de.julielab.scicopia.core.parsing;

import org.elasticsearch.index.query.QueryBuilder;

public final class QueryFragment {

	protected QueryBuilder query;
	protected QueryPriority priority;
	
	protected QueryFragment(QueryBuilder query, QueryPriority priority) {
		this.query = query;
		this.priority = priority;
	}
	
	protected QueryFragment(QueryBuilder query) {
		this.query = query;
		this.priority = QueryPriority.SHOULD;
	}
}
