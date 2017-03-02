package de.julielab.semedico.core.query.translation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.ISemedicoQuery;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public class TitleTranslator extends DocumentQueryTranslator {

	public TitleTranslator(Logger log) {
		super(log, "Title");
		addApplicableIndexType(IIndexInformationService.Indexes.documents + "."
				+ IIndexInformationService.Indexes.DocumentTypes.medline,
				IIndexInformationService.Indexes.documents + "."
						+ IIndexInformationService.Indexes.DocumentTypes.pmc);
		addApplicableTask(SearchTask.DOCUMENTS, SearchTask.GET_ARTICLE);
		addApplicableField(IIndexInformationService.TITLE);
	}

	@Override
	public void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes,
			List<SearchServerQuery> queries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(tasks, indexTypes, query.getSearchedFields()))
			return;

		SearchServerQuery titleQuery = translateToBooleanQuery(query.<ParseTree> getQuery(),
				IIndexInformationService.GeneralIndexStructure.title, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);
		// TODO check if an extra boost is necessary (apart from the already
		// applying length normalization "boost" of titles (they are short!))
		queries.add(titleQuery);
	}

}
