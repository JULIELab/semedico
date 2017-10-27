package de.julielab.semedico.core.search.query.translation;

import java.util.List;
import java.util.Map;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.search.query.ISemedicoQuery;

public interface IQueryTranslator {
	void translate(ISemedicoQuery query, List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries);
}
