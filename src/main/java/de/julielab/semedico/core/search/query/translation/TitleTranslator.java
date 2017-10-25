package de.julielab.semedico.core.search.query.translation;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public class TitleTranslator extends DocumentQueryTranslator {

	public TitleTranslator(Logger log) {
		super(log, "Title");
		addApplicableIndex(IIndexInformationService.Indexes.Documents.name);
		addApplicableTask(SearchTask.DOCUMENTS);
		addApplicableField(IIndexInformationService.Indexes.Documents.title);
	}

	@Override
	public void translate(ISemedicoQuery query, 
			List<SearchServerQuery> queries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(query.getTask(), query.getIndex(), query.getSearchedFields()))
			return;

		SearchServerQuery titleQuery = translateToBooleanQuery(query.<ParseTree> getQuery(),
				IIndexInformationService.Indexes.Documents.title, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);
		// TODO check if an extra boost is necessary (apart from the already
		// applying length normalization "boost" of titles (they are short!))
		queries.add(titleQuery);
	}

}
