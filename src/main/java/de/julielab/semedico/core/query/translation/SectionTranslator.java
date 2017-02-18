package de.julielab.semedico.core.query.translation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.HighlightCommand;
import de.julielab.elastic.query.components.data.query.BoolClause;
import de.julielab.elastic.query.components.data.query.BoolQuery;
import de.julielab.elastic.query.components.data.query.FunctionScoreQuery;
import de.julielab.elastic.query.components.data.query.InnerHits;
import de.julielab.elastic.query.components.data.query.NestedQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.elastic.query.components.data.query.BoolClause.Occur;
import de.julielab.elastic.query.components.data.query.FunctionScoreQuery.FieldValueFactor;
import de.julielab.elastic.query.components.data.query.FunctionScoreQuery.FieldValueFactor.Modifier;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.ISemedicoQuery;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public class SectionTranslator extends DocumentQueryTranslator {

	public SectionTranslator(Logger log) {
		super(log, "Section");
		addApplicableIndexType(IIndexInformationService.Indexes.documents + "."
						+ IIndexInformationService.Indexes.DocumentTypes.pmc);
		addApplicableTask(SearchTask.DOCUMENTS);
		addApplicableField(IIndexInformationService.PmcIndexStructure.sections);
	}

	@Override
	public void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes,
			List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(tasks, indexTypes, query.getSearchFieldFilter()))
			return;

		NestedQuery nestedQuery = translateForNestedTextField(query.<ParseTree> getQuery(),
				IIndexInformationService.PmcIndexStructure.sections, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH, false);
		nestedQuery.innerHits = new InnerHits();
		nestedQuery.innerHits.highlight = new HighlightCommand();
		nestedQuery.innerHits.highlight.addField(IIndexInformationService.PmcIndexStructure.Nested.sectionstitle, 1, 200);
		SearchServerQuery textQuery = nestedQuery.query;
		SearchServerQuery fieldQuery = translateToBooleanQuery(query.<ParseTree> getQuery(),
				IIndexInformationService.PmcIndexStructure.Nested.sectionstitle, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);

		
		FieldValueFactor fieldValueFactor = new FunctionScoreQuery.FieldValueFactor();
		fieldValueFactor.field = IIndexInformationService.PmcIndexStructure.Nested.sectiontitlelikelihood;
		// TODO experiment
		fieldValueFactor.modifier = Modifier.LOG;
		FunctionScoreQuery likelihoodScoreQuery = new FunctionScoreQuery();
		likelihoodScoreQuery.query = fieldQuery;
		likelihoodScoreQuery.fieldValueFactor = fieldValueFactor;
		

		BoolClause clause = new BoolClause();
		clause.occur = Occur.SHOULD;
		clause.addQuery(textQuery);
		clause.addQuery(likelihoodScoreQuery);
		
		BoolQuery boolQuery = new BoolQuery();
		boolQuery.addClause(clause);
		
		nestedQuery.query = boolQuery;

		searchQueries.add(nestedQuery);
		
		NestedQuery hlSectionQuery = new NestedQuery();
		hlSectionQuery.path = IIndexInformationService.PmcIndexStructure.sections;
		hlSectionQuery.query = translateToBooleanQuery(query.<ParseTree> getQuery(),
				IIndexInformationService.PmcIndexStructure.Nested.sectionstext, "1");
		hlSectionQuery.innerHits = new InnerHits();
		hlSectionQuery.innerHits.highlight = new HighlightCommand();
		hlSectionQuery.innerHits.highlight
				.addField(IIndexInformationService.PmcIndexStructure.Nested.sectionstext, 3, 1000);
		namedQueries.put("sectionHl", hlSectionQuery);
	}

}
