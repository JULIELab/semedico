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
 * Searches the {@link IIndexInformationService.GeneralIndexStructure#docmeta}
 * field.
 * 
 * @author faessler
 *
 */
public class DocMetaTranslator extends DocumentQueryTranslator {

	public DocMetaTranslator(Logger log) {
		super(log, "DocMeta");
		addApplicableIndexType(
				IIndexInformationService.Indexes.documents + "."
						+ IIndexInformationService.Indexes.DocumentTypes.medline,
				IIndexInformationService.Indexes.documents + "." + IIndexInformationService.Indexes.DocumentTypes.pmc);
		addApplicableTask(SearchTask.DOCUMENTS);
		addApplicableField(IIndexInformationService.GeneralIndexStructure.docmeta);
	}

	@Override
	public void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes,
			List<SearchServerQuery> queries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(tasks, indexTypes, query.getSearchFieldFilter()))
			return;

		SearchServerQuery searchQuery = translateToBooleanQuery(query.<ParseTree> getQuery(),
				IIndexInformationService.GeneralIndexStructure.docmeta, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);
		if (null != searchQuery) {
			searchQuery.boost = .3f;
			queries.add(searchQuery);
		}
	}

}
