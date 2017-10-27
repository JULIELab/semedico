package de.julielab.semedico.core.search.query.translation;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

/**
 * Not an abstract class but the class to translate the query for the index
 * field named for the abstract.
 * 
 * @author faessler
 *
 */
public class AbstractTextTranslator extends DocumentQueryTranslator {

	public AbstractTextTranslator(Logger log) {
		super(log, "AbstractText");
		addApplicableIndex(IIndexInformationService.Indexes.Documents.name);
		addApplicableTask(SearchTask.DOCUMENTS);
		addApplicableField(IIndexInformationService.Indexes.Documents.abstracttext);
	}

	@Override
	public void translate(ISemedicoQuery query, List<SearchServerQuery> queries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(query.getTask(), query.getIndex(), query.getSearchedFields()))
			return;

		SearchServerQuery searchQuery = translateToBooleanQuery(query.<ParseTree> getQuery(),
				IIndexInformationService.Indexes.Documents.abstracttext, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);
		queries.add(searchQuery);

	}

}
