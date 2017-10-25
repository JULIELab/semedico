package de.julielab.semedico.core.search.query.translation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.HighlightCommand;
import de.julielab.elastic.query.components.data.query.BoolClause;
import de.julielab.elastic.query.components.data.query.BoolClause.Occur;
import de.julielab.elastic.query.components.data.query.BoolQuery;
import de.julielab.elastic.query.components.data.query.ConstantScoreQuery;
import de.julielab.elastic.query.components.data.query.FunctionScoreQuery;
import de.julielab.elastic.query.components.data.query.FunctionScoreQuery.FieldValueFactor;
import de.julielab.elastic.query.components.data.query.FunctionScoreQuery.FieldValueFactor.Modifier;
import de.julielab.elastic.query.components.data.query.InnerHits;
import de.julielab.elastic.query.components.data.query.NestedQuery;
import de.julielab.elastic.query.components.data.query.NestedQuery.ScoreMode;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.elastic.query.components.data.query.TermQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public class RelationTranslator extends DocumentQueryTranslator {

	public RelationTranslator(Logger log) {
		super(log, "Statement");
		addApplicableIndex(IIndexInformationService.Indexes.Relations.name);
		addApplicableTask(SearchTask.STATEMENTS);
		acceptsWildcards = true;
	}

	@Override
	public void translate(ISemedicoQuery query, 
			List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(query.getTask(), query.getIndex(), query.getSearchedFields()))
			return;

		BoolQuery eventFieldsQuery;
		ParseTree parseTree = query.getQuery();

		// TODO split terms to candidates for types and arguments and search in a more structured way
		SearchServerQuery argumentsTypesQuery = translateToBooleanQuery(parseTree,
				IIndexInformationService.Indexes.Relations.arguments, "2");


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
