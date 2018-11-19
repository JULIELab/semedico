package de.julielab.semedico.core.search.query.translation;

import java.util.List;
import java.util.Map;

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
 * Not an abstract class but the class to translate the query for the index
 * field named for the abstract.
 * 
 * @author faessler
 *
 */
public class AbstractTextTranslator extends DocumentQueryTranslator {

	public AbstractTextTranslator(Logger log, @Symbol(SemedicoSymbolConstants.BIOMED_PUBLICATIONS_INDEX_NAME) String biomedIndexName , @Symbol(SemedicoSymbolConstants.CONCEPT_TRANSLATION) ConceptTranslation conceptTranslation) {
		super(log, "AbstractText", conceptTranslation);
		addApplicableIndex(biomedIndexName);
		addApplicableField(IIndexInformationService.Indices.Documents.abstracttexttext);
	}

	@Override
	public void translate(AbstractSemedicoElasticQuery query, List<SearchServerQuery> queries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(query.getIndex(), query.getSearchedFields()))
			return;

		SearchServerQuery searchQuery = translateToBooleanQuery(query.<ParseTree> getQuery(),
				IIndexInformationService.Indices.Documents.abstracttexttext, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);
		queries.add(searchQuery);
	}

}
