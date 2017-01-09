package de.julielab.semedico.core.query.translation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.HighlightCommand;
import de.julielab.elastic.query.components.data.HighlightCommand.HlField;
import de.julielab.elastic.query.components.data.query.BoolClause;
import de.julielab.elastic.query.components.data.query.BoolQuery;
import de.julielab.elastic.query.components.data.query.ConstantScoreQuery;
import de.julielab.elastic.query.components.data.query.FunctionScoreQuery;
import de.julielab.elastic.query.components.data.query.InnerHits;
import de.julielab.elastic.query.components.data.query.NestedQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.elastic.query.components.data.query.TermQuery;
import de.julielab.elastic.query.components.data.query.BoolClause.Occur;
import de.julielab.elastic.query.components.data.query.FunctionScoreQuery.FieldValueFactor;
import de.julielab.elastic.query.components.data.query.FunctionScoreQuery.FieldValueFactor.Modifier;
import de.julielab.elastic.query.components.data.query.NestedQuery.ScoreMode;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.ISemedicoQuery;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public class EventTranslator extends DocumentQueryTranslator {

	public EventTranslator(Logger log) {
		super(log, "Event");
		addApplicableIndexType(
				IIndexInformationService.Indexes.documents + "."
						+ IIndexInformationService.Indexes.DocumentTypes.medline,
				IIndexInformationService.Indexes.documents + "." + IIndexInformationService.Indexes.DocumentTypes.pmc);
		addApplicableTask(SearchTask.DOCUMENTS, SearchTask.EVENTS, SearchTask.GET_ARTICLE);
		addApplicableField(IIndexInformationService.events);
		acceptsWildcards = true;
	}

	@Override
	public void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes,
			List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(tasks, indexTypes, query.getSearchFieldFilter()))
			return;

		BoolQuery eventFieldsQuery;
		ParseTree parseTree = query.getQuery();

		SearchServerQuery argumentsTypesQuery = translateToBooleanQuery(parseTree,
				IIndexInformationService.GeneralIndexStructure.EventFields.allargumentsandtypes, "2");

//		BoolClause argumentsTypesClause = new BoolClause();
//		argumentsTypesClause.occur = Occur.MUST;
//		argumentsTypesClause.addQuery(argumentsTypesQuery);
//
//		eventFieldsQuery = new BoolQuery();
//		eventFieldsQuery.addClause(argumentsTypesClause);

		FieldValueFactor fieldValueFactor = new FunctionScoreQuery.FieldValueFactor();
		fieldValueFactor.field = IIndexInformationService.GeneralIndexStructure.EventFields.likelihood;
		// TODO experiment
		fieldValueFactor.modifier = Modifier.LOG;
		FunctionScoreQuery likelihoodScoreQuery = new FunctionScoreQuery();
		likelihoodScoreQuery.query = argumentsTypesQuery;
		likelihoodScoreQuery.fieldValueFactor = fieldValueFactor;

		NestedQuery eventQuery = new NestedQuery();
		eventQuery.query = likelihoodScoreQuery;
		// eventQuery.query = eventFieldsQuery;
		eventQuery.path = IIndexInformationService.GeneralIndexStructure.events;
		// TODO: currently set to find the best event; should be changed perhaps
		// if we explicitly distinguish between document and fact retrieval
		eventQuery.scoreMode = ScoreMode.max;
		// TODO make it to depend on the task
//		eventQuery.innerHits = new InnerHits();
//		eventQuery.innerHits.addField(IIndexInformationService.GeneralIndexStructure.EventFields.likelihood);
//		eventQuery.innerHits.addField(IIndexInformationService.GeneralIndexStructure.EventFields.sentence);

		searchQueries.add(eventQuery);

		/* --------------- HIGHLIGHTING QUERY --------------- */

		String hlField = IIndexInformationService.GeneralIndexStructure.EventFields.sentence;

		BoolClause scoringClause = new BoolClause();
		scoringClause.occur = Occur.MUST;
		scoringClause.addQuery(argumentsTypesQuery);

		TermQuery hlTerm = new TermQuery();
		hlTerm.term = "event";
		hlTerm.field = IIndexInformationService.GeneralIndexStructure.EventFields.sentence;

		ConstantScoreQuery nullScoreQuery = new ConstantScoreQuery();
		nullScoreQuery.query = hlTerm;
		nullScoreQuery.boost = 0f;

		BoolClause hlTermClause = new BoolClause();
		hlTermClause.occur = Occur.SHOULD;
		hlTermClause.addQuery(nullScoreQuery);

		BoolQuery hlQuery = new BoolQuery();
		hlQuery.addClause(scoringClause);
		hlQuery.addClause(hlTermClause);

		FunctionScoreQuery hlLikelihoodScoreQuery = new FunctionScoreQuery();
		hlLikelihoodScoreQuery.fieldValueFactor = likelihoodScoreQuery.fieldValueFactor;
		hlLikelihoodScoreQuery.query = hlQuery;

		NestedQuery hlEventQuery = new NestedQuery();
		hlEventQuery.path = eventQuery.path;
		hlEventQuery.innerHits = new InnerHits();
		hlEventQuery.innerHits.explain = false;
		hlEventQuery.innerHits.highlight = new HighlightCommand();
		hlEventQuery.innerHits.highlight.addField(hlField, 1, 1000);
//		innerHlField.pre = "<b>";
//		innerHlField.post = "</b>";
		hlEventQuery.query = hlLikelihoodScoreQuery;

		namedQueries.put("eventHl", hlEventQuery);
	}
}
