package de.julielab.semedico.core.search.services;

import de.julielab.elastic.query.components.ISearchComponent;
import de.julielab.elastic.query.components.ISearchServerComponent;
import de.julielab.elastic.query.services.ElasticQueryComponentsModule;
import de.julielab.semedico.core.search.annotations.SearchChain;
import de.julielab.semedico.core.search.annotations.TopicModelSearchChain;
import de.julielab.semedico.core.search.components.*;
import de.julielab.semedico.core.search.components.ArticleSearchPreparationComponent.ArticleSearchPreparation;
import de.julielab.semedico.core.search.components.FacetIndexTermsProcessComponent.FacetIndexTermsProcess;
import de.julielab.semedico.core.search.components.FacetIndexTermsRetrievalComponent.FacetIndexTermsRetrieval;
import de.julielab.semedico.core.search.components.FacetResponseProcessComponent.FacetResponseProcess;
import de.julielab.semedico.core.search.components.FromQueryUIPreparatorComponent.FromQueryUIPreparation;
import de.julielab.semedico.core.search.components.NewSearchUIPreparationComponent.NewSearchUIPreparation;
import de.julielab.semedico.core.search.components.QueryAnalysisComponent.QueryAnalysis;
import de.julielab.semedico.core.search.components.QueryTranslationComponent.QueryTranslation;
import de.julielab.semedico.core.search.components.SearchOptionsConfigurationComponent.SearchOptionsConfiguration;
import de.julielab.semedico.core.search.components.SearchResultPostprocessingComponent.SearchResultPostprocessing;
import de.julielab.semedico.core.search.components.SearchServerRequestCreationComponent.SearchServerRequestCreation;
import de.julielab.semedico.core.search.components.SearchServerResponseErrorShortCircuitComponent.SearchServerResponseErrorShortCircuit;
import de.julielab.semedico.core.search.components.SemedicoConfigurationApplicationComponent.SemedicoConfigurationApplication;
import de.julielab.semedico.core.search.components.SuggestionPreparationComponent.SuggestionPreparation;
import de.julielab.semedico.core.search.components.SuggestionProcessComponent.SuggestionProcess;
import de.julielab.semedico.core.search.components.TermSelectUIPreparationComponent.TermSelectUIPreparation;
import de.julielab.semedico.core.search.components.TextSearchPreparationComponent.TextSearchPreparation;
import de.julielab.semedico.core.search.components.TopicModelSearchComponent.TopicModelSearch;
import de.julielab.semedico.core.search.components.TotalNumDocsPreparationComponent.TotalNumDocsPreparation;
import de.julielab.semedico.core.search.components.TotalNumDocsResponseProcessComponent.TotalNumDocsResponseProcess;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.query.translation.DefaultQueryTranslator;
import de.julielab.semedico.core.search.query.translation.IQueryTranslator;
import de.julielab.semedico.core.services.CoreTermSearchTermProvider;
import de.julielab.semedico.core.services.HighlightingService;
import de.julielab.semedico.core.services.TopicModelService;
import de.julielab.semedico.core.services.interfaces.IHighlightingService;
import de.julielab.semedico.core.services.interfaces.IServiceReconfigurationHub;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.interfaces.ITopicModelService;
import de.julielab.semedico.core.services.query.TokenInputService;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.*;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.slf4j.Logger;

import java.util.List;

@ImportModule(ElasticQueryComponentsModule.class)
public class SemedicoSearchModule {
    private Logger log;
    private ChainBuilder chainBuilder;
    private ISearchComponent textSearchPreparationComponent;
    private ISearchComponent resultListCreationComponent;

    public SemedicoSearchModule(Logger log, ChainBuilder chainBuilder,
                                @TextSearchPreparation ISearchComponent textSearchPreparationComponent) {
        this.log = log;
        this.chainBuilder = chainBuilder;
        this.textSearchPreparationComponent = textSearchPreparationComponent;

    }

    @SuppressWarnings("unchecked")
    public static void bind(ServiceBinder binder) {
        binder.bind(ISearchServerComponent.class, TopicModelSearchComponent.class).withSimpleId().withMarker(TopicModelSearch.class);
        binder.bind(ITopicModelService.class, TopicModelService.class).withSimpleId();

        binder.bind(IHighlightingService.class, HighlightingService.class).withSimpleId();

        binder.bind(ISearchComponent.class, QueryAnalysisComponent.class).withMarker(QueryAnalysis.class)
                .withSimpleId();
        binder.bind(ISearchComponent.class, QueryTranslationComponent.class).withMarker(QueryTranslation.class)
                .withSimpleId();
        binder.bind(ISearchComponent.class, SearchOptionsConfigurationComponent.class).withSimpleId()
                .withMarker(SearchOptionsConfiguration.class);
        binder.bind(ISearchComponent.class, SemedicoConfigurationApplicationComponent.class).withSimpleId()
                .withMarker(SemedicoConfigurationApplication.class);
        binder.bind(ISearchComponent.class, SearchServerResponseErrorShortCircuitComponent.class).withSimpleId()
                .withMarker(SearchServerResponseErrorShortCircuit.class);
        binder.bind(ISearchComponent.class, SearchServerRequestCreationComponent.class).withSimpleId()
                .withMarker(SearchServerRequestCreation.class);


        binder.bind(ISearchComponent.class, TextSearchPreparationComponent.class)
                .withMarker(TextSearchPreparation.class).withId(TextSearchPreparation.class.getSimpleName());
        binder.bind(ISearchComponent.class, ArticleSearchPreparationComponent.class)
                .withMarker(ArticleSearchPreparation.class).withId(ArticleSearchPreparation.class.getSimpleName());
        binder.bind(ISearchComponent.class, FacetResponseProcessComponent.class).withMarker(FacetResponseProcess.class)
                .withId(FacetResponseProcess.class.getSimpleName());
        binder.bind(ISearchComponent.class, NewSearchUIPreparationComponent.class)
                .withMarker(NewSearchUIPreparation.class).withId(NewSearchUIPreparation.class.getSimpleName());
        binder.bind(ISearchComponent.class, FromQueryUIPreparatorComponent.class)
                .withMarker(FromQueryUIPreparation.class).withSimpleId();
        binder.bind(ISearchComponent.class, TermSelectUIPreparationComponent.class)
                .withMarker(TermSelectUIPreparation.class).withId(TermSelectUIPreparation.class.getSimpleName());

        binder.bind(ISearchComponent.class, TotalNumDocsPreparationComponent.class)
                .withMarker(TotalNumDocsPreparation.class).withId(TotalNumDocsPreparation.class.getSimpleName());
        binder.bind(ISearchComponent.class, TotalNumDocsResponseProcessComponent.class)
                .withMarker(TotalNumDocsResponseProcess.class)
                .withId(TotalNumDocsResponseProcess.class.getSimpleName());
        binder.bind(ISearchComponent.class, FacetIndexTermsRetrievalComponent.class)
                .withMarker(FacetIndexTermsRetrieval.class).withId(FacetIndexTermsRetrieval.class.getSimpleName());
        binder.bind(ISearchComponent.class, FacetIndexTermsProcessComponent.class)
                .withMarker(FacetIndexTermsProcess.class).withId(FacetIndexTermsProcess.class.getSimpleName());
        binder.bind(ISearchComponent.class, SuggestionPreparationComponent.class)
                .withMarker(SuggestionPreparation.class).withId(SuggestionPreparation.class.getSimpleName());
        binder.bind(ISearchComponent.class, SuggestionProcessComponent.class).withMarker(SuggestionProcess.class)
                .withId(SuggestionProcess.class.getSimpleName());
        binder.bind(ISearchComponent.class, SearchResultPostprocessingComponent.class).withMarker(SearchResultPostprocessing.class).withSimpleId();

        binder.bind(ITokenInputService.class, TokenInputService.class).withSimpleId();
    }

    @Marker(Primary.class)
    // We build this service eagerly to have all the translators be registered
    // with the ServiceRegconfigurationHub
    @EagerLoad
    public IQueryTranslator buildQueryTranslatorChain(List<IQueryTranslator> translators, IServiceReconfigurationHub reconfigurationHub, SymbolSource symbolSource) {
        translators.forEach(reconfigurationHub::registerService);
        if (translators.isEmpty())
            log.warn("No query translators have been contributed. Query creation will not be possible.");
        return chainBuilder.build(IQueryTranslator.class, translators);
    }

    public void contributeQueryTranslatorChain(OrderedConfiguration<IQueryTranslator<? extends ISemedicoQuery>> configuration, LoggerSource loggerSource) {
        // The default query translator should always come at the end, thus the after:* constraint
        // see http://tapestry.apache.org/ordering-by-constraints.html
        configuration.add("DefaultQueryTranslator", new DefaultQueryTranslator(loggerSource.getLogger(DefaultQueryTranslator.class)), "after:*");
    }

    @Contribute(ISearchComponent.class)
    @SearchChain
    public void contributeSearchChain(OrderedConfiguration<ISearchComponent> configuration,
                                      @QueryTranslation ISearchComponent queryTranslationComponent, @InjectService("ElasticSearchComponent") ISearchServerComponent searchServerComponent,
                                      @SearchOptionsConfiguration ISearchComponent searchOptionsConfigurationComponent,
                                      @SemedicoConfigurationApplication ISearchComponent semedicoConfigurationApplicationComponent,
                                      @SearchServerResponseErrorShortCircuit ISearchComponent shortCircuitComponent,
                                      @SearchServerRequestCreation ISearchComponent requestCreationComponent,
                                      @SearchResultPostprocessing ISearchComponent postprocessingComponent) {
        configuration.add("QueryTranslation", queryTranslationComponent);
        configuration.add("RequestCreation", requestCreationComponent);
        configuration.add("SearchOptionConfiguration", searchOptionsConfigurationComponent);
        configuration.add("SemedicoConfigurationApplication", semedicoConfigurationApplicationComponent);
        configuration.add("SearchServer", searchServerComponent);
        configuration.add("Postprocessing", postprocessingComponent);
        configuration.add("ShortCircuit", shortCircuitComponent);
    }

    @Contribute(ISearchComponent.class)
    @TopicModelSearchChain
    public void contributeTMSearchChain(OrderedConfiguration<ISearchComponent> configuration,
                                        @TopicModelSearch ISearchServerComponent topicModelSearchComponent,
                                        @SemedicoConfigurationApplication ISearchComponent semedicoConfigurationApplicationComponent,
                                        @SearchServerResponseErrorShortCircuit ISearchComponent shortCircuitComponent) {
        configuration.add("SemedicoConfigurationApplication", semedicoConfigurationApplicationComponent);
        configuration.add("TopicModel", topicModelSearchComponent);
        configuration.add("ShortCircuit", shortCircuitComponent);
    }

    @Marker(TopicModelSearchChain.class)
    public ISearchComponent buildTMSearchChain(List<ISearchComponent> components) {
        return chainBuilder.build(ISearchComponent.class, components);
    }

    @Marker(SearchChain.class)
    public ISearchComponent buildSearchChain(List<ISearchComponent> components) {
        return chainBuilder.build(ISearchComponent.class, components);
    }

    @Marker(Primary.class)
    public ISearchTermProvider buildSearchTermProvider(List<ISearchTermProvider> provider) {
        return chainBuilder.build(ISearchTermProvider.class, provider);
    }

    public void contributeSearchTermProvider(OrderedConfiguration<ISearchTermProvider> configuration) {
        configuration.addInstance("coreTermProvider", CoreTermSearchTermProvider.class);
        configuration.addInstance("idProvider", IdSearchTermProvider.class);
    }
}
