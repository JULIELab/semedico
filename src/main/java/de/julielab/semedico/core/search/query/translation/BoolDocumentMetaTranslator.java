package de.julielab.semedico.core.search.query.translation;

import java.util.ArrayList;
import java.util.List;

import de.julielab.elastic.query.components.data.query.BoolClause;
import de.julielab.elastic.query.components.data.query.BoolClause.Occur;
import de.julielab.elastic.query.components.data.query.BoolQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;

public class BoolDocumentMetaTranslator implements IMetaQueryTranslator {

	@Override
	public SearchServerQuery combine(List<SearchServerQuery> queries) {
		if (queries.size() == 1)
			return queries.get(0);
		
		List<BoolClause> clauses = new ArrayList<>(queries.size());
		for (SearchServerQuery serverQuery : queries) {
			BoolClause clause = new BoolClause();
			clause.occur = BoolClause.Occur.SHOULD;
			clause.addQuery(serverQuery);
			clauses.add(clause);
		}
		BoolQuery boolQuery = new BoolQuery();
		boolQuery.clauses = clauses;
		BoolClause queryClause = new BoolClause();
		queryClause.occur = Occur.MUST;
		queryClause.addQuery(boolQuery);
		BoolQuery mainQuery = new BoolQuery();
		mainQuery.addClause(queryClause);
		
		return mainQuery;
	}

}
