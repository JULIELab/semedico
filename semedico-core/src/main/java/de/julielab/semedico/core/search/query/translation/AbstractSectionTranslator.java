package de.julielab.semedico.core.search.query.translation;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.AbstractSemedicoElasticQuery;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

public class AbstractSectionTranslator extends DocumentQueryTranslator {

    public AbstractSectionTranslator(Logger log, @Symbol(SemedicoSymbolConstants.BIOMED_PUBLICATIONS_INDEX_NAME) String biomedIndexName, @Symbol(SemedicoSymbolConstants.CONCEPT_TRANSLATION) ConceptTranslation conceptTranslation) {
        super(log, "AbstractSections", conceptTranslation);
        addApplicableIndex(biomedIndexName);
    }

    @Override
    public void translate(AbstractSemedicoElasticQuery query, List<SearchServerQuery> searchQueries,
                          Map<String, SearchServerQuery> namedQueries) {
        if (!applies(query.getIndex(), query.getSearchedFields()))
            return;
        SearchServerQuery searchServerQuery = translateToBooleanQuery((ParseTree) query.getQuery(),
                IIndexInformationService.Indices.AbstractSections.text, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);
        searchQueries.add(searchServerQuery);
    }

}
