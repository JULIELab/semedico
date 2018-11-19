package de.julielab.semedico.core.search.services;

import de.julielab.elastic.query.components.ISearchComponent;
import de.julielab.elastic.query.components.ISearchServerComponent;
import de.julielab.elastic.query.services.ElasticQueryComponentsModule;
import de.julielab.semedico.core.search.annotations.SearchChain;
import de.julielab.semedico.core.search.annotations.TopicModelSearchChain;
import de.julielab.semedico.core.search.components.*;
import de.julielab.semedico.core.search.components.ArticleResponseProcessComponent.ArticleResponseProcess;
import de.julielab.semedico.core.search.components.ArticleSearchPreparationComponent.ArticleSearchPreparation;
import de.julielab.semedico.core.search.components.FacetCountPreparationComponent.FacetCountPreparation;
import de.julielab.semedico.core.search.components.FacetIndexTermsProcessComponent.FacetIndexTermsProcess;
import de.julielab.semedico.core.search.components.FacetIndexTermsRetrievalComponent.FacetIndexTermsRetrieval;
import de.julielab.semedico.core.search.components.FacetResponseProcessComponent.FacetResponseProcess;
import de.julielab.semedico.core.search.components.FromQueryUIPreparatorComponent.FromQueryUIPreparation;
import de.julielab.semedico.core.search.components.NewSearchUIPreparationComponent.NewSearchUIPreparation;
import de.julielab.semedico.core.search.components.QueryAnalysisComponent.QueryAnalysis;
import de.julielab.semedico.core.search.components.QueryTranslationComponent.QueryTranslation;
import de.julielab.semedico.core.search.components.ResultListCreationComponent.ResultListCreation;
import de.julielab.semedico.core.search.components.SearchOptionsConfigurationComponent.SearchOptionsConfiguration;
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
import de.julielab.semedico.core.search.query.translation.*;
import de.julielab.semedico.core.services.CoreTermSearchTermProvider;
import de.julielab.semedico.core.services.interfaces.IServiceReconfigurationHub;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.*;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.apache.tapestry5.ioc.services.SymbolSource;

import java.util.List;

@ImportModule(ElasticQueryComponentsModule.class)
public class SemedicoSearchModule {

    private ChainBuilder chainBuilder;
    private ISearchComponent textSearchPreparationComponent;
    private ISearchComponent resultListCreationComponent;

    public SemedicoSearchModule(ChainBuilder chainBuilder,
                                @TextSearchPreparation ISearchComponent textSearchPreparationComponent,
                                @ResultListCreation ISearchComponent resultListCreationComponent) {
        this.chainBuilder = chainBuilder;
        this.textSearchPreparationComponent = textSearchPreparationComponent;
        this.resultListCreationComponent = resultListCreationComponent;

    }

    @SuppressWarnings("unchecked")
    public static void bind(ServiceBinder binder) {
        binder.bind(ISearchServerComponent.class, TopicModelSearchComponent.class).withSimpleId().withMarker(TopicModelSearch.class);

        binder.bind(ISearchComponent.class, QueryAnalysisComponent.class).withMarker(QueryAnalysis.class)
                .withId(QueryAnalysis.class.getSimpleName());
        binder.bind(ISearchComponent.class, QueryTranslationComponent.class).withMarker(QueryTranslation.class)
                .withId(QueryTranslation.class.getSimpleName());
        binder.bind(ISearchComponent.class, SearchOptionsConfigurationComponent.class).withSimpleId()
                .withMarker(SearchOptionsConfiguration.class);
        binder.bind(ISearchComponent.class, SemedicoConfigurationApplicationComponent.class).withSimpleId()
                .withMarker(SemedicoConfigurationApplication.class);
        binder.bind(ISearchComponent.class, SearchServerResponseErrorShortCircuitComponent.class).withSimpleId()
                .withMarker(SearchServerResponseErrorShortCircuit.class);
        binder.bind(ISearchComponent.class, SearchServerRequestCreationComponent.class).withSimpleId()
                .withMarker(SearchServerRequestCreation.class);

        binder.bind(IQueryTranslator.class, AbstractTextTranslator.class).withSimpleId();
        binder.bind(IQueryTranslator.class, AbstractSectionTranslator.class).withSimpleId();

        binder.bind(ISearchComponent.class, TextSearchPreparationComponent.class)
                .withMarker(TextSearchPreparation.class).withId(TextSearchPreparation.class.getSimpleName());
        binder.bind(ISearchComponent.class, ArticleSearchPreparationComponent.class)
                .withMarker(ArticleSearchPreparation.class).withId(ArticleSearchPreparation.class.getSimpleName());
        binder.bind(ISearchComponent.class, FacetResponseProcessComponent.class).withMarker(FacetResponseProcess.class)
                .withId(FacetResponseProcess.class.getSimpleName());
        binder.bind(ISearchComponent.class, ArticleResponseProcessComponent.class)
                .withMarker(ArticleResponseProcess.class).withId(ArticleResponseProcess.class.getSimpleName());
        binder.bind(ISearchComponent.class, ResultListCreationComponent.class).withMarker(ResultListCreation.class)
                .withId(ResultListCreation.class.getSimpleName());
        binder.bind(ISearchComponent.class, FacetCountPreparationComponent.class)
                .withMarker(FacetCountPreparation.class).withId(FacetCountPreparation.class.getSimpleName());
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
    }

    @Marker(Primary.class)
    // We build this service eagerly to have all the translators be registered
    // with the ServiceRegconfigurationHub
    @EagerLoad
    public IQueryTranslator buildQueryTranslatorChain(List<IQueryTranslator> translators, IServiceReconfigurationHub reconfigurationHub, SymbolSource symbolSource) {
        translators.forEach(reconfigurationHub::registerService);
        return chainBuilder.build(IQueryTranslator.class, translators);
    }

    @Contribute(ISearchComponent.class)
    @SearchChain
    public void contributeSearchChain(OrderedConfiguration<ISearchComponent> configuration,
                                      @QueryTranslation ISearchComponent queryTranslationComponent, ISearchServerComponent searchServerComponent,
                                      @SearchOptionsConfiguration ISearchComponent searchOptionsConfigurationComponent,
                                      @SemedicoConfigurationApplication ISearchComponent semedicoConfigurationApplicationComponent,
                                      @SearchServerResponseErrorShortCircuit ISearchComponent shortCircuitComponent,
                                      @SearchServerRequestCreation ISearchComponent requestCreationComponent) {
        configuration.add("QueryTranslation", queryTranslationComponent);
        configuration.add("RequestCreation", requestCreationComponent);
        configuration.add("SearchOptionConfiguration", searchOptionsConfigurationComponent);
        configuration.add("SemedicoConfigurationApplication", semedicoConfigurationApplicationComponent);
        configuration.add("SearchServer", searchServerComponent);
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
