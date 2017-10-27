package de.julielab.semedico.core.search.query.translation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.NestedQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public class TableCaptionTranslator extends DocumentQueryTranslator {

	public TableCaptionTranslator(Logger log, @Symbol(SemedicoSymbolConstants.BIOMED_PUBLICATIONS_INDEX_NAME) String biomedPublications) {
		super(log, "TableCaption");
		addApplicableIndex(
				biomedPublications + "."
						+ IIndexInformationService.Indexes.DocumentTypes.tablecaptions);
		addApplicableTask(SearchTask.DOCUMENTS);
	}

	@Override
	public void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes,
			List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(tasks, indexTypes, query.getSearchedFields()))
			return;

		NestedQuery nestedQuery = translateForNestedTextField(query.<ParseTree> getQuery(),
				IIndexInformationService.PmcIndexStructure.tablecaptions, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH, false);
		searchQueries.add(nestedQuery);
	}

}
