package de.julielab.semedico.core.query.translation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.HighlightCommand;
import de.julielab.elastic.query.components.data.HighlightCommand.HlField;
import de.julielab.elastic.query.components.data.query.FunctionScoreQuery;
import de.julielab.elastic.query.components.data.query.InnerHits;
import de.julielab.elastic.query.components.data.query.NestedQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.elastic.query.components.data.query.FunctionScoreQuery.FieldValueFactor;
import de.julielab.elastic.query.components.data.query.FunctionScoreQuery.FieldValueFactor.Modifier;
import de.julielab.elastic.query.components.data.query.NestedQuery.ScoreMode;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.ISemedicoQuery;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public class SentenceTranslator extends DocumentQueryTranslator {

	public SentenceTranslator(Logger log, @Symbol(SemedicoSymbolConstants.BIOMED_PUBLICATIONS_INDEX_NAME) String biomedPublications) {
		super(log, "Sentence");
		addApplicableIndexType(biomedPublications + "." + IIndexInformationService.Indexes.DocumentTypes.sentences);
		addApplicableTask(SearchTask.SENTENCES);
	}

	@Override
	public void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes,
			List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(tasks, indexTypes, query.getSearchedFields()))
			return;
		SearchServerQuery fieldQuery = translateToBooleanQuery(query.<ParseTree> getQuery(),
				IIndexInformationService.sentences + "." + IIndexInformationService.GeneralIndexStructure.text,
				DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);

		FieldValueFactor fieldValueFactor = new FunctionScoreQuery.FieldValueFactor();
		fieldValueFactor.field = IIndexInformationService.GeneralIndexStructure.Nested.sentenceslikelihood;
		// TODO experiment
		fieldValueFactor.modifier = Modifier.LOG;
		FunctionScoreQuery likelihoodScoreQuery = new FunctionScoreQuery();
		likelihoodScoreQuery.query = fieldQuery;
		likelihoodScoreQuery.fieldValueFactor = fieldValueFactor;

		NestedQuery nestedQuery = new NestedQuery();
		nestedQuery.path = IIndexInformationService.sentences;
		nestedQuery.query = likelihoodScoreQuery;
		nestedQuery.scoreMode = ScoreMode.max;
		searchQueries.add(nestedQuery);

		NestedQuery hlSentenceQuery = new NestedQuery();
		hlSentenceQuery.path = IIndexInformationService.sentences;
		hlSentenceQuery.query = likelihoodScoreQuery;
		hlSentenceQuery.innerHits = new InnerHits();
		hlSentenceQuery.innerHits.highlight = new HighlightCommand();
		hlSentenceQuery.innerHits.highlight
				.addField(IIndexInformationService.GeneralIndexStructure.Nested.sentencestext, 3, 1000);
//		hlField.pre = "<b>";
//		hlField.post = "</b>";

		namedQueries.put("sentenceHl", hlSentenceQuery);
	}

}
