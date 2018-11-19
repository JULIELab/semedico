package de.julielab.semedico.core.search.query.translation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.julielab.semedico.core.search.query.AbstractSemedicoElasticQuery;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.NestedQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.SearchScope;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public class FigureCaptionTranslator extends DocumentQueryTranslator {

	public FigureCaptionTranslator(Logger log,
			@Symbol(SemedicoSymbolConstants.BIOMED_PUBLICATIONS_INDEX_NAME) String biomedPublications , @Symbol(SemedicoSymbolConstants.CONCEPT_TRANSLATION) ConceptTranslation conceptTranslation) {
		super(log, "FigureCaption", conceptTranslation);
		addApplicableIndex(biomedPublications);
	}

	@Override
	public void translate(AbstractSemedicoElasticQuery query,
			List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries) {
//		if (!applies(tasks, indexTypes, query.getSearchedFields()))
//			return;
//
//		NestedQuery nestedQuery = translateForNestedTextField(query.<ParseTree>getQuery(),
//				IIndexInformationService.PmcIndexStructure.figurecaptions, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH, false);
//		// nestedQuery.innerHits = new InnerHits();
//		// nestedQuery.innerHits.addField(IIndexInformationService.PmcIndexStructure.Nested.figurecaptionstext);
//		// nestedQuery.innerHits.explain = true;
//		searchQueries.add(nestedQuery);
	}

}
