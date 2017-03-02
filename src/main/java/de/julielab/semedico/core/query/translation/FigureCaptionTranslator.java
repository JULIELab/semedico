package de.julielab.semedico.core.query.translation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.NestedQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.ISemedicoQuery;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public class FigureCaptionTranslator extends DocumentQueryTranslator {

	public FigureCaptionTranslator(Logger log) {
		super(log, "FigureCaption");
		addApplicableIndexType(
				IIndexInformationService.Indexes.documents + "." + IIndexInformationService.Indexes.DocumentTypes.pmc);
		addApplicableTask(SearchTask.DOCUMENTS);
		addApplicableField(IIndexInformationService.PmcIndexStructure.figurecaptions);
	}

	@Override
	public void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes,
			List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries) {
		if (!applies(tasks, indexTypes, query.getSearchedFields()))
			return;

		NestedQuery nestedQuery = translateForNestedTextField(query.<ParseTree> getQuery(),
				IIndexInformationService.PmcIndexStructure.figurecaptions,DEFAULT_TEXT_MINIMUM_SHOULD_MATCH, false);
		// nestedQuery.innerHits = new InnerHits();
		// nestedQuery.innerHits.addField(IIndexInformationService.PmcIndexStructure.Nested.figurecaptionstext);
		// nestedQuery.innerHits.explain = true;
		searchQueries.add(nestedQuery);
	}

}
