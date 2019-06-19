package de.julielab.scicopia.core.search;


import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.scicopia.core.elasticsearch.IElasticsearchQueryBuilder;
import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import org.slf4j.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ElasticsearchQueryComponent extends AbstractSearchComponent<SemedicoESSearchCarrier> {
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ElasticsearchQuery {
		//
	}
	
	private IElasticsearchQueryBuilder elasticsearchQueryService;

	public ElasticsearchQueryComponent(Logger log, IElasticsearchQueryBuilder elasticsearchQueryService) {
		super(log);
		this.elasticsearchQueryService = elasticsearchQueryService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.search.components.ISearchComponent#process(de.julielab
	 * .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean processSearch(SemedicoESSearchCarrier semCarrier) {
		// This code has to be changed to:
		// * Not be a search component but a part of the QueryAnalysisComponent
		// * Create an IElasticQuery

//		List<QueryToken> tokens = semCarrier.getUserQuery();
//
//		if (null == tokens) {
//			throw new IllegalArgumentException("A list of " + QueryToken.class.getName()
//					+ " is expected, but it was null.");
//		}
//
//		if (tokens.isEmpty()) {
//			throw new IllegalArgumentException("The passed list of " + QueryToken.class.getName()
//					+ " is invalid. The user query is empty.");
//		}
//
//		SearchServerCommand serverCmds = semCarrier.getSingleSearchServerCommandOrCreate();
//		serverCmds.query = elasticsearchQueryService.analyseQueryString(tokens);
//		Map<String, QueryBuilder> namedQueries = new HashMap<>();
//		serverCmds.namedQueries = namedQueries;
		return false;
	}
}
