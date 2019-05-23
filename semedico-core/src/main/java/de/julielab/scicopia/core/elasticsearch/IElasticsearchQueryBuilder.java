package de.julielab.scicopia.core.elasticsearch;

import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;

import de.julielab.semedico.core.query.QueryToken;

public interface IElasticsearchQueryBuilder {

	public QueryBuilder analyseQueryString(List<QueryToken> tokens);
}
