package de.julielab.semedico.core.query.translation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.NestedQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.ISemedicoQuery;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

/**
 * Searches the {@link IIndexInformationService.GeneralIndexStructure#journal}
 * field.
 * 
 * @author faessler
 *
 */
public class JournalTranslator extends DocumentQueryTranslator {

	public JournalTranslator(Logger log) {
		super(log, "Journal");
		addApplicableIndexType(
				IIndexInformationService.Indexes.documents + "."
						+ IIndexInformationService.Indexes.DocumentTypes.medline,
				IIndexInformationService.Indexes.documents + "." + IIndexInformationService.Indexes.DocumentTypes.pmc);
		addApplicableTask(SearchTask.DOCUMENTS);
		addApplicableField(IIndexInformationService.GeneralIndexStructure.journaltitle);
	}

	@Override
	public void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes,
			List<SearchServerQuery> queries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(tasks, indexTypes, query.getSearchedFields()))
			return;
		NestedQuery searchQuery = translateForNestedTextField(query.<ParseTree> getQuery(),
				IIndexInformationService.GeneralIndexStructure.journal, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH, false);
		searchQuery.boost = .3f;
		queries.add(searchQuery);
	}

}
