package de.julielab.semedico.core.search.services;

import java.util.List;

import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.services.ChainBuilder;

import de.julielab.elastic.query.components.ISearchComponent;
import de.julielab.elastic.query.components.ISearchServerComponent;
import de.julielab.elastic.query.services.ElasticQueryComponentsModule;
import de.julielab.semedico.core.search.annotations.SearchChain;
import de.julielab.semedico.core.search.components.ArticleResponseProcessComponent;
import de.julielab.semedico.core.search.components.ArticleResponseProcessComponent.ArticleResponseProcess;
import de.julielab.semedico.core.search.components.ArticleSearchPreparationComponent;
import de.julielab.semedico.core.search.components.ArticleSearchPreparationComponent.ArticleSearchPreparation;
import de.julielab.semedico.core.search.components.FacetCountPreparationComponent;
import de.julielab.semedico.core.search.components.FacetCountPreparationComponent.FacetCountPreparation;
import de.julielab.semedico.core.search.components.FacetIndexTermsProcessComponent;
import de.julielab.semedico.core.search.components.FacetIndexTermsProcessComponent.FacetIndexTermsProcess;
import de.julielab.semedico.core.search.components.FacetIndexTermsRetrievalComponent;
import de.julielab.semedico.core.search.components.FacetIndexTermsRetrievalComponent.FacetIndexTermsRetrieval;
import de.julielab.semedico.core.search.components.FacetResponseProcessComponent;
import de.julielab.semedico.core.search.components.FacetResponseProcessComponent.FacetResponseProcess;
import de.julielab.semedico.core.search.components.FieldTermsResultComponent;
import de.julielab.semedico.core.search.components.FieldTermsResultComponent.FieldTermsProcess;
import de.julielab.semedico.core.search.components.FieldTermsRetrievalPreparationComponent;
import de.julielab.semedico.core.search.components.FieldTermsRetrievalPreparationComponent.FieldTermsRetrievalPreparation;
import de.julielab.semedico.core.search.components.FromQueryUIPreparatorComponent;
import de.julielab.semedico.core.search.components.FromQueryUIPreparatorComponent.FromQueryUIPreparation;
import de.julielab.semedico.core.search.components.NewSearchUIPreparationComponent;
import de.julielab.semedico.core.search.components.NewSearchUIPreparationComponent.NewSearchUIPreparation;
import de.julielab.semedico.core.search.components.QueryAnalysisComponent;
import de.julielab.semedico.core.search.components.QueryAnalysisComponent.QueryAnalysis;
import de.julielab.semedico.core.search.components.QueryTranslationComponent;
import de.julielab.semedico.core.search.components.QueryTranslationComponent.QueryTranslation;
import de.julielab.semedico.core.search.components.ResultListCreationComponent;
import de.julielab.semedico.core.search.components.ResultListCreationComponent.ResultListCreation;
import de.julielab.semedico.core.search.components.SearchOptionsConfigurationComponent;
import de.julielab.semedico.core.search.components.SearchOptionsConfigurationComponent.SearchOptionsConfiguration;
import de.julielab.semedico.core.search.components.SearchServerRequestCreationComponent;
import de.julielab.semedico.core.search.components.SearchServerRequestCreationComponent.SearchServerRequestCreation;
import de.julielab.semedico.core.search.components.SearchServerResponseErrorShortCircuitComponent;
import de.julielab.semedico.core.search.components.SearchServerResponseErrorShortCircuitComponent.SearchServerResponseErrorShortCircuit;
import de.julielab.semedico.core.search.components.SemedicoConfigurationApplicationComponent;
import de.julielab.semedico.core.search.components.SemedicoConfigurationApplicationComponent.SemedicoConfigurationApplication;
import de.julielab.semedico.core.search.components.SuggestionPreparationComponent;
import de.julielab.semedico.core.search.components.SuggestionPreparationComponent.SuggestionPreparation;
import de.julielab.semedico.core.search.components.SuggestionProcessComponent;
import de.julielab.semedico.core.search.components.SuggestionProcessComponent.SuggestionProcess;
import de.julielab.semedico.core.search.components.TermSelectUIPreparationComponent;
import de.julielab.semedico.core.search.components.TermSelectUIPreparationComponent.TermSelectUIPreparation;
import de.julielab.semedico.core.search.components.TextSearchPreparationComponent;
import de.julielab.semedico.core.search.components.TextSearchPreparationComponent.TextSearchPreparation;
import de.julielab.semedico.core.search.components.TotalNumDocsPreparationComponent;
import de.julielab.semedico.core.search.components.TotalNumDocsPreparationComponent.TotalNumDocsPreparation;
import de.julielab.semedico.core.search.components.TotalNumDocsResponseProcessComponent;
import de.julielab.semedico.core.search.components.TotalNumDocsResponseProcessComponent.TotalNumDocsResponseProcess;
import de.julielab.semedico.core.search.query.translation.AbstractSectionTranslator;
import de.julielab.semedico.core.search.query.translation.AbstractTextTranslator;
import de.julielab.semedico.core.search.query.translation.AllTextTranslator;
import de.julielab.semedico.core.search.query.translation.DocMetaTranslator;
import de.julielab.semedico.core.search.query.translation.FigureCaptionTranslator;
import de.julielab.semedico.core.search.query.translation.IQueryTranslator;
import de.julielab.semedico.core.search.query.translation.MeshTranslator;
import de.julielab.semedico.core.search.query.translation.ParagraphTranslator;
import de.julielab.semedico.core.search.query.translation.RelationTranslator;
import de.julielab.semedico.core.search.query.translation.SectionTranslator;
import de.julielab.semedico.core.search.query.translation.SentenceTranslator;
import de.julielab.semedico.core.search.query.translation.TableCaptionTranslator;
import de.julielab.semedico.core.search.query.translation.TitleTranslator;

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
		binder.bind(IResultCollectorService.class, ResultCollectorService.class).withSimpleId();
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

		binder.bind(IQueryTranslator.class, TitleTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, AbstractTextTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, AllTextTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, RelationTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, SentenceTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, AbstractSectionTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, ParagraphTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, SectionTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, FigureCaptionTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, TableCaptionTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, DocMetaTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, MeshTranslator.class).withSimpleId();

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
		binder.bind(ISearchComponent.class, FieldTermsRetrievalPreparationComponent.class)
				.withMarker(FieldTermsRetrievalPreparation.class).withSimpleId();
		binder.bind(ISearchComponent.class, FieldTermsResultComponent.class).withMarker(FieldTermsProcess.class)
				.withSimpleId();
	}

	@Marker(Primary.class)
	public IQueryTranslator buildQueryTranslatorChain(List<IQueryTranslator> translators) {
		return chainBuilder.build(IQueryTranslator.class, translators);
	}

	@Primary
	public void contributeQueryTranslatorChain(OrderedConfiguration<IQueryTranslator> configuration,
			@InjectService("TitleTranslator") IQueryTranslator titleTranslator,
			@InjectService("AbstractTextTranslator") IQueryTranslator abstractTextTranslator,
			@InjectService("AllTextTranslator") IQueryTranslator allTextTranslator,
			@InjectService("RelationTranslator") IQueryTranslator relationsTranslator,
			@InjectService("SentenceTranslator") IQueryTranslator sentencesTranslator,
			@InjectService("AbstractSectionTranslator") IQueryTranslator abstractSectionTranslator,
			@InjectService("SectionTranslator") IQueryTranslator sectionTranslator,
			@InjectService("ParagraphTranslator") IQueryTranslator paragraphTranslator,
			@InjectService("FigureCaptionTranslator") IQueryTranslator figureCaptionTranslator,
			@InjectService("TableCaptionTranslator") IQueryTranslator tableCaptionTranslator,
			@InjectService("DocMetaTranslator") IQueryTranslator docMetaTranslator,
			@InjectService("MeshTranslator") IQueryTranslator meshTranslator) {
		configuration.add("AllTextTranslator", allTextTranslator);
		configuration.add("AbstractTextTranslator", abstractTextTranslator);
		configuration.add("RelationsTranslator", relationsTranslator);
		configuration.add("TitleTranslator", titleTranslator);
		configuration.add("SentenceTranslator", sentencesTranslator);
		configuration.add("MeshTranslator", meshTranslator);
		// configuration.add("sectionTranslator", sectionTranslator);

		// configuration.add("DocMetaTranslator", docMetaTranslator);
		// configuration.add("AbstractSectionTranslator",
		// abstractSectionTranslator);
		// configuration.add("paragraphTranslator", paragraphTranslator);
		// configuration.add("FigureCaptionTranslator",
		// figureCaptionTranslator);
		// configuration.add("TableCaptionTranslator", tableCaptionTranslator);
	}

	@Contribute(ISearchComponent.class)
	@SearchChain
	public void contributeSearchChain(OrderedConfiguration<ISearchComponent> configuration,
			@QueryTranslation ISearchComponent queryTranslationComponent,
			ISearchServerComponent searchServerComponent,
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

	@Marker(SearchChain.class)
	public ISearchComponent buildSearchChain(List<ISearchComponent> components) {
		return chainBuilder.build(ISearchComponent.class, components);
	}
	//
	// @Contribute(ISearchComponent.class)
	// @SentenceSearchChain
	// public void
	// contributeSentenceSearchChain(OrderedConfiguration<ISearchComponent>
	// configuration) {
	// configuration.add(QueryTranslation.class.getSimpleName(),
	// queryTranslationComponent);
	// configuration.add(ISearchServerComponent.class.getSimpleName(),
	// searchServerComponent);
	// }
	//
	// @Marker(TermSelectChain.class)
	// public ISearchComponent buildTermSelectChain(List<ISearchComponent>
	// commands) {
	// return chainBuilder.build(ISearchComponent.class, commands);
	// }
	//
	// @Contribute(ISearchComponent.class)
	// @TermSelectChain
	// public void
	// contributeTermSelectChain(OrderedConfiguration<ISearchComponent>
	// configuration,
	// @TermSelectUIPreparation ISearchComponent
	// termSelectUIPreparationComponent,
	// @FacetedDocumentSearchSubchain ISearchComponent
	// facetedDocumentSearchSubchain) {
	// configuration.add("TermSelectUIPreparation",
	// termSelectUIPreparationComponent);
	// configuration.add("QueryTranslation", queryTranslationComponent);
	// configuration.add("TextSearchPreparation",
	// textSearchPreparationComponent);
	// configuration.add("FacetedDocumentSearch",
	// facetedDocumentSearchSubchain);
	// configuration.add("SearchServer", searchServerComponent);
	// configuration.add("ResultListCreation", resultListCreationComponent);
	// }
	//
	// @Marker(DocumentChain.class)
	// public ISearchComponent buildDocumentChain(List<ISearchComponent>
	// commands) {
	// return chainBuilder.build(ISearchComponent.class, commands);
	// }
	//
	// @Contribute(ISearchComponent.class)
	// @DocumentChain
	// public void
	// contributeDocumentChain(OrderedConfiguration<ISearchComponent>
	// configuration,
	// @NewSearchUIPreparation ISearchComponent newSearchUIPreparationComponent,
	// @FromQueryUIPreparation ISearchComponent fromQueryUIPreparationComponent)
	// {
	// configuration.add("NewSearchUIPreparation",
	// newSearchUIPreparationComponent);
	// configuration.add("QueryTranslation", queryTranslationComponent);
	// configuration.add("TextSearchPreparation",
	// textSearchPreparationComponent);
	// configuration.add("SearchServer", searchServerComponent);
	// configuration.add("ResultListCreation", resultListCreationComponent);
	// configuration.add("FromQueryUIPreparation",
	// fromQueryUIPreparationComponent);
	// }
	//
	// @Marker(ArticleChain.class)
	// public ISearchComponent buildArticleChain(List<ISearchComponent>
	// commands) {
	// return chainBuilder.build(ISearchComponent.class, commands);
	// }
	//
	// @Contribute(ISearchComponent.class)
	// @ArticleChain
	// public void contributeArticleChain(OrderedConfiguration<ISearchComponent>
	// configuration,
	// @ArticleSearchPreparation ISearchComponent
	// articleSearchPreparationComponent,
	// @ArticleResponseProcess ISearchComponent articleResponseProcessComponent)
	// {
	// configuration.add("QueryTranslation", queryTranslationComponent);
	// configuration.add("ArticleSearchPreparation",
	// articleSearchPreparationComponent);
	// configuration.add("SearchServer", searchServerComponent);
	// configuration.add("ArticleResponseProcess",
	// articleResponseProcessComponent);
	// }
}
