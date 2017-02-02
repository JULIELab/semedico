package de.julielab.semedico.core.query.translation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.query.ISemedicoQuery;

public class FactTranslator extends DocumentQueryTranslator {

	public FactTranslator(Logger log, String name) {
		super(log, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes,
			List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries) {
		// TODO Auto-generated method stub

	}

}
