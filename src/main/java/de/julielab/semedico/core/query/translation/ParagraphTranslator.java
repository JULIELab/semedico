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

public class ParagraphTranslator extends DocumentQueryTranslator {

	public ParagraphTranslator(Logger log) {
		super(log, "Paragraph");
		addApplicableIndexType(IIndexInformationService.Indexes.documents + "."
						+ IIndexInformationService.Indexes.DocumentTypes.pmc);
		addApplicableTask(SearchTask.DOCUMENTS);
		addApplicableField(IIndexInformationService.PmcIndexStructure.paragraphs);
	}

	@Override
	public void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes,
			List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(tasks, indexTypes, query.getSearchedFields()))
			return;

		NestedQuery nestedQuery = translateForNestedTextField(query.<ParseTree> getQuery(),
				IIndexInformationService.PmcIndexStructure.paragraphs, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH, false);
		searchQueries.add(nestedQuery);
	}

}
