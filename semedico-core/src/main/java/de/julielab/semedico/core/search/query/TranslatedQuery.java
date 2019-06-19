package de.julielab.semedico.core.search.query;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.search.components.QueryTranslationComponent;
import de.julielab.semedico.core.search.components.SearchServerRequestCreationComponent;
import de.julielab.semedico.core.search.query.translation.AbstractQueryTranslator;

import java.util.Map;

/**
 * An intermediary query representation between the {@link ISemedicoQuery} and
 * the {@link SearchServerQuery}. Created by the
 * {@link QueryTranslationComponent} and consumed by the
 * {@link SearchServerRequestCreationComponent}.
 * 
 * @author faessler
 *
 */
public class TranslatedQuery {
	public TranslatedQuery(SearchServerQuery query, Map<String, SearchServerQuery> namedQueries) {
		this.query = query;
		this.namedQueries = namedQueries;
	}

	/**
	 * The main content query.
	 */
	public SearchServerQuery query;
	/**
	 * Named queries can be created by subclasses of
	 * {@link AbstractQueryTranslator}. They can be used to specify separate
	 * highlight queries.
	 */
	public Map<String, SearchServerQuery> namedQueries;
}
