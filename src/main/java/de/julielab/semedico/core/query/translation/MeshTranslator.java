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
public class MeshTranslator extends DocumentQueryTranslator {

	public MeshTranslator(Logger log) {
		super(log, "Mesh");
		addApplicableIndexType(IIndexInformationService.Indexes.documents + "."
				+ IIndexInformationService.Indexes.DocumentTypes.medline);
		addApplicableTask(SearchTask.DOCUMENTS, SearchTask.GET_ARTICLE);
		addApplicableField(IIndexInformationService.GeneralIndexStructure.mesh);
	}

	@Override
	public void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes,
			List<SearchServerQuery> queries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(tasks, indexTypes, query.getSearchFieldFilter()))
			return;

		SearchServerQuery meshQuery = translateToBooleanQuery(query.<ParseTree> getQuery(),
				IIndexInformationService.GeneralIndexStructure.mesh, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);

		if (null != meshQuery)
			queries.add(meshQuery);
	}

}
