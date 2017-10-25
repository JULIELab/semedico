package de.julielab.semedico.core.search.query.translation;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

/**
 * Searches the {@link IIndexInformationService.GeneralIndexStructure#docmeta}
 * field.
 * 
 * @author faessler
 *
 */
public class MeshTranslator extends DocumentQueryTranslator {

	public MeshTranslator(Logger log) {
		super(log, "Mesh");
		addApplicableIndex(IIndexInformationService.Indexes.Documents.name);
		addApplicableTask(SearchTask.DOCUMENTS);
		addApplicableField(IIndexInformationService.Indexes.Documents.mesh);
	}

	@Override
	public void translate(ISemedicoQuery query,
			List<SearchServerQuery> queries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(query.getTask(), query.getIndex(), query.getSearchedFields()))
			return;

		SearchServerQuery meshQuery = translateToBooleanQuery(query.<ParseTree>getQuery(),
				IIndexInformationService.Indexes.Documents.mesh, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);

		if (null != meshQuery)
			queries.add(meshQuery);
	}

}
