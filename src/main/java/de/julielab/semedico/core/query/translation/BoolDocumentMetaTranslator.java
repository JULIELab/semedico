package de.julielab.semedico.core.query.translation;

import java.util.ArrayList;
import java.util.List;

import de.julielab.elastic.query.components.data.query.BoolClause;
import de.julielab.elastic.query.components.data.query.BoolQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.elastic.query.components.data.query.TermQuery;
import de.julielab.elastic.query.components.data.query.BoolClause.Occur;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public class BoolDocumentMetaTranslator implements IMetaQueryTranslator {

	@Override
	public SearchServerQuery combine(List<SearchServerQuery> queries, List<Concept> facetFilterConcepts) {
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
		
		
		if (null != facetFilterConcepts
				&& !facetFilterConcepts.isEmpty()) {
			BoolClause filterClause = new BoolClause();
			filterClause.occur = Occur.MUST;
			for (Concept selectedFacetConcept : facetFilterConcepts) {
				TermQuery termQuery = new TermQuery();
				termQuery.field = IIndexInformationService.GeneralIndexStructure.conceptlist;
				termQuery.term = selectedFacetConcept.getId();
				filterClause.addQuery(termQuery);
			}
			mainQuery.addClause(filterClause);
		}
		
		
		return mainQuery;
	}

}
