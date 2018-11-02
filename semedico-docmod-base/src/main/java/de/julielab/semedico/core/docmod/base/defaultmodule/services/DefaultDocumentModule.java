package de.julielab.semedico.core.docmod.base.defaultmodule.services;

import de.julielab.semedico.core.docmod.base.defaultmodule.entities.DefaultDocModAllTextTranslator;
import de.julielab.semedico.core.docmod.base.entities.DocModInfo;
import de.julielab.semedico.core.docmod.base.entities.DocumentPart;
import de.julielab.semedico.core.docmod.base.services.IDocModQueryService;
import de.julielab.semedico.core.docmod.base.services.IDocumentModule;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.query.translation.ConceptTranslation;
import de.julielab.semedico.core.search.query.translation.IQueryTranslator;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Symbol;

import java.util.Arrays;

/**
 * <p>
 * This is the default document module. It serves as a minimal example for document modules, as a proof of concept
 * for them and as a fallback position for new document types no specific document module yet exists.
 * </p>
 */
public class DefaultDocumentModule implements IDocumentModule {

    public static final String DEFAULT_DOCMOD_ALLTEXT_INDEX = "semedico.docmod.default.index.alltext";
    public static final String DEFAULT_DOCMOD_NAME = "semedico.docmod.default.name";

    public static final String FIELD_ALL_TEXT = "alltext";
    public static final String FIELD_FACETS = "conceptlist";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_AUTHORS = "authors";

    public static DocModInfo DEFAULT_INFO;

    private LoggerSource loggerSource;
    private String allTextIndexName;
    private ConceptTranslation conceptTranslation;

    public DefaultDocumentModule(LoggerSource loggerSource, @Symbol(DEFAULT_DOCMOD_NAME) String defaultDocModName, @Symbol(DEFAULT_DOCMOD_ALLTEXT_INDEX) String allTextIndexName, @Symbol(SemedicoSymbolConstants.CONCEPT_TRANSLATION) ConceptTranslation conceptTranslation) {
        this.loggerSource = loggerSource;
        this.allTextIndexName = allTextIndexName;
        this.conceptTranslation = conceptTranslation;
        DEFAULT_INFO = new DocModInfo(defaultDocModName, Arrays.asList(new DocumentPart("All Text", allTextIndexName)));
    }

    @Override
    public void contributeDocModInformationService(OrderedConfiguration<DocModInfo> configuration) {
        configuration.add("DefaultModuleInfo", DEFAULT_INFO);
    }

    @Override
    public void contributeQueryTranslatorChain(OrderedConfiguration<IQueryTranslator<? extends ISemedicoQuery>> configuration) {
        configuration.add("All Text", new DefaultDocModAllTextTranslator(loggerSource.getLogger(DefaultDocModAllTextTranslator.class), "All Text", FIELD_ALL_TEXT, allTextIndexName, conceptTranslation));
    }

    @Override
    public void contributeDocModQueryService(OrderedConfiguration<IDocModQueryService> configuration) {
        configuration.add("Default", new DefaultDocModQueryService(loggerSource.getLogger(DefaultDocModQueryService.class), DEFAULT_INFO));
    }


}
