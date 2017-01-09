package de.julielab.semedico.core.query.translation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.ISemedicoQuery;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

/**
 * Searches the {@link IIndexInformationService.GeneralIndexStructure#authors} field.
 * @author faessler
 *
 */
public class AuthorTranslator extends DocumentQueryTranslator {

	public AuthorTranslator(Logger log) {
		super(log, "Authors");
		addApplicableIndexType(IIndexInformationService.Indexes.documents + "."
				+ IIndexInformationService.Indexes.DocumentTypes.medline,
				IIndexInformationService.Indexes.documents + "."
						+ IIndexInformationService.Indexes.DocumentTypes.pmc);
		addApplicableTask(SearchTask.DOCUMENTS, SearchTask.GET_ARTICLE);
		addApplicableField(IIndexInformationService.GeneralIndexStructure.authors);
	}

	@Override
	public void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes,
			List<SearchServerQuery> queries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(tasks, indexTypes, query.getSearchFieldFilter()))
			return;

		SearchServerQuery searchQuery = translateToBooleanQuery(query.<ParseTree> getQuery(),
				IIndexInformationService.GeneralIndexStructure.authors, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);
		searchQuery.boost = .3f;
		
		queries.add(searchQuery);
	}

}
