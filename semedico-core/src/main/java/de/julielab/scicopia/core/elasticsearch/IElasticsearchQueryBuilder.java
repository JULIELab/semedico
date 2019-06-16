package de.julielab.scicopia.core.elasticsearch;

import java.util.List;

import de.julielab.semedico.core.search.query.QueryToken;
import org.elasticsearch.index.query.QueryBuilder;


public interface IElasticsearchQueryBuilder {

	public QueryBuilder analyseQueryString(List<QueryToken> tokens);
}
