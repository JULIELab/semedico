package de.julielab.scicopia.core.search.components;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.QueryBuilder;
import org.slf4j.Logger;

import de.julielab.scicopia.core.elasticsearch.IElasticsearchQueryBuilder;
import de.julielab.scicopia.core.elasticsearch.legacy.AbstractSearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchCarrier;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchServerCommand;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;

public class ElasticsearchQueryComponent extends AbstractSearchComponent {
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ElasticsearchQuery {
		//
	}
	
	private IElasticsearchQueryBuilder elasticsearchQueryService;
	private Logger log;

	public ElasticsearchQueryComponent(Logger log, IElasticsearchQueryBuilder elasticsearchQueryService) {
		this.log = log;
		this.elasticsearchQueryService = elasticsearchQueryService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.search.components.ISearchComponent#process(de.julielab
	 * .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		List<QueryToken> tokens = semCarrier.getUserQuery();

		if (null == tokens) {
			throw new IllegalArgumentException("A list of " + QueryToken.class.getName()
					+ " is expected, but it was null.");
		}
		
		if (tokens.isEmpty()) {
			throw new IllegalArgumentException("The passed list of " + QueryToken.class.getName()
					+ " is invalid. The user query is empty.");
		}

		SearchServerCommand serverCmds = semCarrier.getSingleSearchServerCommandOrCreate();
		serverCmds.query = elasticsearchQueryService.analyseQueryString(tokens);
		Map<String, QueryBuilder> namedQueries = new HashMap<>();
		serverCmds.namedQueries = namedQueries;
		return false;
	}
}
