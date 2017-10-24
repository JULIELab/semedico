package de.julielab.semedico.core.search.query.translation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public class SentenceTranslator extends DocumentQueryTranslator {

	public SentenceTranslator(Logger log) {
		super(log, "Sentence");
		addApplicableIndexType(IIndexInformationService.Indexes.Sentences.name);
		addApplicableTask(SearchTask.SENTENCES);
	}

	@Override
	public void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes,
			List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(tasks, indexTypes, query.getSearchedFields()))
			return;
		SearchServerQuery fieldQuery = translateToBooleanQuery(query.<ParseTree> getQuery(),
				IIndexInformationService.Indexes.Sentences.text,
				DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);

		searchQueries.add(fieldQuery);
	}

}
