package de.julielab.scicopia.core.elasticsearch;

import de.julielab.semedico.core.search.query.QueryToken;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.List;


public interface IElasticsearchQueryBuilder {

	public QueryBuilder analyseQueryString(List<QueryToken> tokens);
}
