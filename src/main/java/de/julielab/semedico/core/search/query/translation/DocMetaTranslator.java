package de.julielab.semedico.core.search.query.translation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

/**
 * Searches the {@link IIndexInformationService.GeneralIndexStructure#docmeta}
 * field.
 * 
 * @author faessler
 *
 */
public class DocMetaTranslator extends DocumentQueryTranslator {

	public DocMetaTranslator(Logger log,
			@Symbol(SemedicoSymbolConstants.BIOMED_PUBLICATIONS_INDEX_NAME) String biomedPublications) {
		super(log, "DocMeta");
		addApplicableIndexType(biomedPublications + "." + IIndexInformationService.Indexes.DocumentTypes.medline, biomedPublications + IIndexInformationService.Indexes.DocumentTypes.pmc);
		addApplicableTask(SearchTask.DOCUMENTS);
		addApplicableField(IIndexInformationService.GeneralIndexStructure.docmeta);
	}

	@Override
	public void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes,
			List<SearchServerQuery> queries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(tasks, indexTypes, query.getSearchedFields()))
			return;

		SearchServerQuery searchQuery = translateToBooleanQuery(query.<ParseTree>getQuery(),
				IIndexInformationService.GeneralIndexStructure.docmeta, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);
		if (null != searchQuery) {
			searchQuery.boost = .3f;
			queries.add(searchQuery);
		}
	}

}
