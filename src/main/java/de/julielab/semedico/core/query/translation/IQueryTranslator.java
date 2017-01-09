package de.julielab.semedico.core.query.translation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.query.ISemedicoQuery;

public interface IQueryTranslator {
	void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes, List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries);
}
