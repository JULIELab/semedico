package de.julielab.semedico.core.docmod.base.defaultmodule.entities;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.AbstractSemedicoElasticQuery;
import de.julielab.semedico.core.search.query.translation.ConceptTranslation;
import de.julielab.semedico.core.search.query.translation.DocumentQueryTranslator;
import de.julielab.semedico.core.search.query.translation.IQueryTranslator;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

public class DefaultDocModAllTextTranslator extends DocumentQueryTranslator {
    private final String allTextFieldName;

    public DefaultDocModAllTextTranslator(Logger log, String name, String allTextFieldName, String allTextIndexName, ConceptTranslation conceptTranslation) {
        super(log, name, conceptTranslation);
        this.allTextFieldName = allTextFieldName;
        addApplicableIndex(allTextIndexName);
        addApplicableField();
    }

    @Override
    public void translate(AbstractSemedicoElasticQuery query, List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries) {
        if (!applies(null, query.getIndex(), query.getSearchedFields())) {
            return;
        }

        SearchServerQuery allTextQuery = translateToBooleanQuery(query.<ParseTree>getQuery(),
                allTextFieldName, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);
        searchQueries.add(allTextQuery);
    }
}
