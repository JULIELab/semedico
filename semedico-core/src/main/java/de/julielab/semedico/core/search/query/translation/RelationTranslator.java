package de.julielab.semedico.core.search.query.translation;

import java.util.List;
import java.util.Map;

import de.julielab.semedico.core.search.query.AbstractSemedicoElasticQuery;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.BoolQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.SearchScope;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public class RelationTranslator extends DocumentQueryTranslator {

	public RelationTranslator(Logger log , @Symbol(SemedicoSymbolConstants.CONCEPT_TRANSLATION) ConceptTranslation conceptTranslation) {
		super(log, "Statement", conceptTranslation);
		addApplicableIndex(IIndexInformationService.Indices.Relations.name);
		addApplicableScope(SearchScope.RELATIONS);
		acceptsWildcards = true;
	}

	@Override
	public void translate(AbstractSemedicoElasticQuery query,
			List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(query.getScopes(), query.getIndex(), query.getSearchedFields()))
			return;

		BoolQuery eventFieldsQuery;
		ParseTree parseTree = query.getQuery();

		// TODO split terms to candidates for types and arguments and search in a more structured way
		SearchServerQuery argumentsTypesQuery = translateToBooleanQuery(parseTree,
				IIndexInformationService.Indices.Relations.arguments, "2");


		/* --------------- HIGHLIGHTING QUERY --------------- */

//		String hlField = IIndexInformationService.GeneralIndexStructure.EventFields.sentence;
//
//		BoolClause scoringClause = new BoolClause();
//		scoringClause.occur = Occur.MUST;
//		scoringClause.addQuery(argumentsTypesQuery);
//
//		TermQuery hlTerm = new TermQuery();
//		hlTerm.term = "event";
//		hlTerm.field = IIndexInformationService.GeneralIndexStructure.EventFields.sentence;
//
//		ConstantScoreQuery nullScoreQuery = new ConstantScoreQuery();
//		nullScoreQuery.query = hlTerm;
//		nullScoreQuery.boost = 0f;
//
//		BoolClause hlTermClause = new BoolClause();
//		hlTermClause.occur = Occur.SHOULD;
//		hlTermClause.addQuery(nullScoreQuery);
//
//		BoolQuery hlQuery = new BoolQuery();
//		hlQuery.addClause(scoringClause);
//		hlQuery.addClause(hlTermClause);
//
//		FunctionScoreQuery hlLikelihoodScoreQuery = new FunctionScoreQuery();
//		hlLikelihoodScoreQuery.fieldValueFactor = likelihoodScoreQuery.fieldValueFactor;
//		hlLikelihoodScoreQuery.query = hlQuery;
//
//		NestedQuery hlEventQuery = new NestedQuery();
//		hlEventQuery.path = eventQuery.path;
//		hlEventQuery.innerHits = new InnerHits();
//		hlEventQuery.innerHits.explain = false;
//		hlEventQuery.innerHits.highlight = new HighlightCommand();
//		hlEventQuery.innerHits.highlight.addField(hlField, 1, 1000);
//		hlEventQuery.innerHits.addField(IIndexInformationService.GeneralIndexStructure.EventFields.likelihood);
//		hlEventQuery.innerHits.addField(IIndexInformationService.GeneralIndexStructure.EventFields.sentence);
//		hlEventQuery.innerHits.addField(IIndexInformationService.GeneralIndexStructure.EventFields.allarguments);
//		hlEventQuery.innerHits.addField(IIndexInformationService.GeneralIndexStructure.EventFields.maineventtype);
//		hlEventQuery.query = hlLikelihoodScoreQuery;

		namedQueries.put("eventHl", argumentsTypesQuery);
	}
}
