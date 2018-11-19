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

public class TitleTranslator extends DocumentQueryTranslator {

	public TitleTranslator(Logger log, @Symbol(SemedicoSymbolConstants.BIOMED_PUBLICATIONS_INDEX_NAME) String biomedIndexName , @Symbol(SemedicoSymbolConstants.CONCEPT_TRANSLATION) ConceptTranslation conceptTranslation) {
		super(log, "Title", conceptTranslation);
		addApplicableIndex(biomedIndexName);
		addApplicableField(IIndexInformationService.Indices.Documents.titletext);
	}

	@Override
	public void translate(AbstractSemedicoElasticQuery query,
			List<SearchServerQuery> queries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(query.getIndex(), query.getSearchedFields()))
			return;

		SearchServerQuery titleQuery = translateToBooleanQuery(query.<ParseTree> getQuery(),
				IIndexInformationService.Indices.Documents.titletext, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);
		// TODO check if an extra boost is necessary (apart from the already
		// applying length normalization "boost" of titles (they are short!))
		queries.add(titleQuery);
	}

}
