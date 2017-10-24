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
 * Not an abstract class but the class to translate the query for the index
 * field named for the abstract.
 * 
 * @author faessler
 *
 */
public class AbstractTextTranslator extends DocumentQueryTranslator {

	public AbstractTextTranslator(Logger log, @Symbol(SemedicoSymbolConstants.BIOMED_PUBLICATIONS_INDEX_NAME) String biomedPublications) {
		super(log, "AbstractText");
		addApplicableIndexType(
				biomedPublications + "."
						+ IIndexInformationService.Indexes.DocumentTypes.medline, biomedPublications + IIndexInformationService.Indexes.DocumentTypes.pmc);
		addApplicableTask(SearchTask.DOCUMENTS);
		addApplicableField(IIndexInformationService.GeneralIndexStructure.abstracttext);
	}

	@Override
	public void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes,
			List<SearchServerQuery> queries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(tasks, indexTypes, query.getSearchedFields()))
			return;

		// SearchServerQuery searchQuery = translateForMatch(query.<ParseTree>
		// getQuery(),
		// IIndexInformationService.GeneralIndexStructure.abstracttext);
		SearchServerQuery searchQuery = translateToBooleanQuery(query.<ParseTree> getQuery(),
				IIndexInformationService.ABSTRACT, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);
		queries.add(searchQuery);

	}

}
