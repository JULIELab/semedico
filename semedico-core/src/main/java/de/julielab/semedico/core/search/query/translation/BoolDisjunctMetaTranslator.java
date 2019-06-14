package de.julielab.semedico.core.search.query.translation;

import de.julielab.elastic.query.components.data.query.BoolClause;
import de.julielab.elastic.query.components.data.query.BoolQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a simple disjunction from the given queries and adds a filter for the
 * given search scopes (sentences, relations, ...).
 * 
 * @author faessler
 *
 */
public class BoolDisjunctMetaTranslator implements IMetaQueryTranslator {

	/**
	 * Creates a simple disjunction from the given queries. Also adds the given
	 * search scopes as a filter clause, if any are given.
	 * @param queries
	 */
	@Override
	public SearchServerQuery combine(List<SearchServerQuery> queries) {
		if (queries.size() == 1)
			return queries.get(0);

		List<BoolClause> clauses = new ArrayList<>(queries.size());
		for (SearchServerQuery serverQuery : queries) {
			BoolClause clause = new BoolClause();
			clause.occur = BoolClause.Occur.MUST;
			clause.addQuery(serverQuery);
			clauses.add(clause);
		}

		BoolQuery boolQuery = new BoolQuery();
		boolQuery.clauses = clauses;

		return boolQuery;
	}

}
