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

public class ChunkTranslator extends DocumentQueryTranslator {

	public ChunkTranslator(Logger log, @Symbol(SemedicoSymbolConstants.BIOMED_PUBLICATIONS_INDEX_NAME) String biomedIndexName , @Symbol(SemedicoSymbolConstants.CONCEPT_TRANSLATION) ConceptTranslation conceptTranslation) {
		super(log, "Chunk", conceptTranslation);
		addApplicableIndex(biomedIndexName);
	}

	@Override
	public void translate(AbstractSemedicoElasticQuery query, List<SearchServerQuery> searchQueries,
						  Map<String, SearchServerQuery> namedQueries) {
		if (!applies(query.getIndex(), query.getSearchedFields()))
			return;
		SearchServerQuery fieldQuery = translateToBooleanQuery(query.<ParseTree>getQuery(),
				IIndexInformationService.Indices.Chunks.text, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);

		searchQueries.add(fieldQuery);
	}

}
