package de.julielab.semedico.core.docmod.base.defaultmodule.entities;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.AbstractSemedicoElasticQuery;
import de.julielab.semedico.core.search.query.translation.ConceptTranslation;
import de.julielab.semedico.core.search.query.translation.DocumentQueryTranslator;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

public class DefaultTextFieldTranslator extends DocumentQueryTranslator {
    private final String titleFieldName;

    public DefaultTextFieldTranslator(Logger log, String name, String fieldName, String indexName, ConceptTranslation conceptTranslation) {
        super(log, name, conceptTranslation);
        this.titleFieldName = fieldName;
        addApplicableIndex(indexName);
        addApplicableField(fieldName);
    }

    @Override
    public void translate(AbstractSemedicoElasticQuery query, List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries) {
        if (!applies(null, query.getIndex(), query.getSearchedFields())) {
            return;
        }

        SearchServerQuery allTextQuery = translateToBooleanQuery(query.<ParseTree>getQuery(),
                titleFieldName, DEFAULT_TEXT_MINIMUM_SHOULD_MATCH);
        searchQueries.add(allTextQuery);
    }
}
