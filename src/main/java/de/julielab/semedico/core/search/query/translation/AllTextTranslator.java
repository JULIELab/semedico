package de.julielab.semedico.core.search.query.translation;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public class AllTextTranslator extends DocumentQueryTranslator {

	
	public AllTextTranslator(Logger log) {
		super(log, "AllText");
		addApplicableIndex(IIndexInformationService.Indexes.Documents.name);
		addApplicableTask(SearchTask.DOCUMENTS);
		addApplicableField(IIndexInformationService.Indexes.Documents.documenttext);
	}
	
	@Override
	public void translate(ISemedicoQuery query,
			List<SearchServerQuery> queries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(query.getTask(), query.getIndex(), query.getSearchedFields()))
			return;
		
		SearchServerQuery allTextQuery = translateToBooleanQuery(query.<ParseTree> getQuery(),
				IIndexInformationService.Indexes.Documents.documenttext, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);
		queries.add(allTextQuery);

	}

}
