package de.julielab.semedico.core.search.query.translation;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.java.utilities.prerequisites.PrerequisiteChecker;
import de.julielab.semedico.core.entities.documents.SemedicoIndexField;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.AbstractSemedicoElasticQuery;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IServiceReconfigurationHub;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * This translator is located at the very end of the query translation chain. It is only active if no query
 * has been created before. It makes no requirements about specified fields or indices but just translates
 * the query taking into account concepts, wildcards and possibly other available features. Thus, this is a
 * good default or fallback translator. If a more specific translation is required, a new query translator must
 * be created that precedes this one in the translation chain.
 *
 * @see {@link de.julielab.semedico.core.search.services.SemedicoSearchModule#buildQueryTranslatorChain(List, IServiceReconfigurationHub, SymbolSource)}
 */
public class DefaultQueryTranslator extends AbstractQueryTranslator<AbstractSemedicoElasticQuery> {
    private ConceptTranslation conceptTranslation;

    public DefaultQueryTranslator(Logger log) {
        super(log, "Default Query Translator");
    }

    @Override
    public void translate(AbstractSemedicoElasticQuery query, List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries) {
        if (searchQueries.isEmpty()) {
            PrerequisiteChecker.checkThat().notEmpty(query.getSearchedFields()).withNames("searched fields").execute();
            for (SemedicoIndexField field : query.getSearchedFields()) {
                final SearchServerQuery searchServerQuery = QueryTranslation.translateToBooleanQuery((ParseTree) query.getQuery(), field, "0%", true, conceptTranslation);
                searchQueries.add(searchServerQuery);
            }
        }
    }

    @Override
    public void configure(SymbolProvider symbolProvider) {
        getEnum(SemedicoSymbolConstants.CONCEPT_TRANSLATION, symbolProvider, ConceptTranslation.class)
                .ifPresent(this::setConceptTranslation);
    }

    private void setConceptTranslation(ConceptTranslation translation) {
        log.debug("Setting concept translation to {}", translation);
        this.conceptTranslation = translation;
    }
}
