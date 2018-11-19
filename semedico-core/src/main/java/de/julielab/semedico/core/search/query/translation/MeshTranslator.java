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
 * Searches the {@link IIndexInformationService.GeneralIndexStructure#docmeta}
 * field.
 * 
 * @author faessler
 *
 */
public class MeshTranslator extends DocumentQueryTranslator {

	public MeshTranslator(Logger log, @Symbol(SemedicoSymbolConstants.BIOMED_PUBLICATIONS_INDEX_NAME) String biomedIndexName , @Symbol(SemedicoSymbolConstants.CONCEPT_TRANSLATION) ConceptTranslation conceptTranslation) {
		super(log, "Mesh", conceptTranslation);
		addApplicableIndex(biomedIndexName);
		addApplicableField(IIndexInformationService.Indices.Documents.mesh);
	}

	@Override
	public void translate(AbstractSemedicoElasticQuery query,
			List<SearchServerQuery> queries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(query.getIndex(), query.getSearchedFields()))
			return;

		SearchServerQuery meshQuery = translateToBooleanQuery(query.<ParseTree>getQuery(),
				IIndexInformationService.Indices.Documents.mesh, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);

		if (null != meshQuery)
			queries.add(meshQuery);
	}

}
