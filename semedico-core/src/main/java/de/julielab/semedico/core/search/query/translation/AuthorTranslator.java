package de.julielab.semedico.core.search.query.translation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.julielab.semedico.core.search.query.AbstractSemedicoElasticQuery;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.SearchScope;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

/**
 * Searches the {@link IIndexInformationService.GeneralIndexStructure#authors} field.
 * @author faessler
 *
 */
public class AuthorTranslator extends DocumentQueryTranslator {

	public AuthorTranslator(Logger log, @Symbol(SemedicoSymbolConstants.BIOMED_PUBLICATIONS_INDEX_NAME) String biomedPublications , @Symbol(SemedicoSymbolConstants.CONCEPT_TRANSLATION) ConceptTranslation conceptTranslation) {
		super(log, "Authors", conceptTranslation);
		addApplicableIndex(biomedPublications);
		addApplicableField(IIndexInformationService.Indices.Documents.authors);
	}

	@Override
	public void translate(AbstractSemedicoElasticQuery query,
			List<SearchServerQuery> queries, Map<String, SearchServerQuery> namedQueries) {
//		if (!applies(tasks, indexTypes, query.getSearchedFields()))
//			return;
//
//		SearchServerQuery searchQuery = translateToBooleanQuery(query.<ParseTree> getQuery(),
//				IIndexInformationService.GeneralIndexStructure.authors, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);
//		searchQuery.boost = .3f;
//		
//		queries.add(searchQuery);
	}

}
