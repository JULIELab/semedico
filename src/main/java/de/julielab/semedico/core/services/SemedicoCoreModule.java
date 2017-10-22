/**
 * SemedicoCoreModule.java
 *
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 06.06.2011
 **/

package de.julielab.semedico.core.services;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.Startup;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.cron.PeriodicExecutor;
import org.slf4j.Logger;
import org.tartarus.snowball.SnowballProgram;

import com.aliasi.chunk.Chunker;
import com.aliasi.dict.Dictionary;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;

import de.julielab.elastic.query.components.ISearchComponent;
import de.julielab.elastic.query.components.ISearchServerComponent;
import de.julielab.elastic.query.services.ElasticQueryComponentsModule;
import de.julielab.semedico.core.TermFacetKey;
import de.julielab.semedico.core.TermRelationKey;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.interfaces.IFacetTermRelation;
import de.julielab.semedico.core.concepts.interfaces.IPath;
import de.julielab.semedico.core.db.DBConnectionService;
import de.julielab.semedico.core.db.IDBConnectionService;
import de.julielab.semedico.core.facetterms.CoreTerm;
import de.julielab.semedico.core.facetterms.FacetTermFactory;
import de.julielab.semedico.core.facetterms.TermCreator;
import de.julielab.semedico.core.lingpipe.DictionaryReaderService;
import de.julielab.semedico.core.lingpipe.IDictionaryReaderService;
import de.julielab.semedico.core.query.translation.AbstractSectionTranslator;
import de.julielab.semedico.core.query.translation.AbstractTextTranslator;
import de.julielab.semedico.core.query.translation.AllTextTranslator;
import de.julielab.semedico.core.query.translation.DocMetaTranslator;
import de.julielab.semedico.core.query.translation.FigureCaptionTranslator;
import de.julielab.semedico.core.query.translation.IQueryTranslator;
import de.julielab.semedico.core.query.translation.MeshTranslator;
import de.julielab.semedico.core.query.translation.ParagraphTranslator;
import de.julielab.semedico.core.query.translation.SectionTranslator;
import de.julielab.semedico.core.query.translation.SentenceTranslator;
import de.julielab.semedico.core.query.translation.StatementTranslator;
import de.julielab.semedico.core.query.translation.TableCaptionTranslator;
import de.julielab.semedico.core.query.translation.TitleTranslator;
import de.julielab.semedico.core.search.HighlightingService;
import de.julielab.semedico.core.search.LabelCacheService;
import de.julielab.semedico.core.search.annotations.DocumentPagingChain;
import de.julielab.semedico.core.search.annotations.FacetCountChain;
import de.julielab.semedico.core.search.annotations.FacetIndexTermsChain;
import de.julielab.semedico.core.search.annotations.FacetedDocumentSearchSubchain;
import de.julielab.semedico.core.search.annotations.FieldTermsChain;
import de.julielab.semedico.core.search.annotations.SuggestionsChain;
import de.julielab.semedico.core.search.annotations.TermDocumentFrequencyChain;
import de.julielab.semedico.core.search.annotations.TotalNumDocsChain;
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
import de.julielab.semedico.core.search.interfaces.IHighlightingService;
import de.julielab.semedico.core.search.interfaces.ILabelCacheService;
import de.julielab.semedico.core.search.services.SemedicoSearchModule;
import de.julielab.semedico.core.services.CacheService.CacheWrapper;
import de.julielab.semedico.core.services.TermNeo4jService.AllRootPathsInFacetCacheLoader;
import de.julielab.semedico.core.services.TermNeo4jService.FacetRootCacheLoader;
import de.julielab.semedico.core.services.TermNeo4jService.FacetTermRelationsCacheLoader;
import de.julielab.semedico.core.services.TermNeo4jService.ShortestRootPathCacheLoader;
import de.julielab.semedico.core.services.TermNeo4jService.ShortestRootPathInFacetCacheLoader;
import de.julielab.semedico.core.services.TermNeo4jService.TermCacheLoader;
import de.julielab.semedico.core.services.interfaces.IBioPortalOntologyRecommender;
import de.julielab.semedico.core.services.interfaces.ICacheService;
import de.julielab.semedico.core.services.interfaces.ICacheService.Region;
import de.julielab.semedico.core.services.interfaces.IConceptRecognitionService;
import de.julielab.semedico.core.services.interfaces.IDocumentCacheService;
import de.julielab.semedico.core.services.interfaces.IDocumentService;
import de.julielab.semedico.core.services.interfaces.IExternalLinkService;
import de.julielab.semedico.core.services.interfaces.IFacetDeterminerManager;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IFacetTermFactory;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.IHttpClientService.GeneralHttpClient;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.ILexerService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService.Neo4jHttpClient;
import de.julielab.semedico.core.services.interfaces.IParsingService;
import de.julielab.semedico.core.services.interfaces.IQueryAnalysisService;
import de.julielab.semedico.core.services.interfaces.IRelatedArticlesService;
import de.julielab.semedico.core.services.interfaces.IRuleBasedCollatorWrapper;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.core.services.interfaces.ISearchTermProvider;
import de.julielab.semedico.core.services.interfaces.IStemmerService;
import de.julielab.semedico.core.services.interfaces.IStopWordService;
import de.julielab.semedico.core.services.interfaces.IStringTermService;
import de.julielab.semedico.core.services.interfaces.ITermCreator;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseService;
import de.julielab.semedico.core.services.interfaces.ITermDocumentFrequencyService;
import de.julielab.semedico.core.services.interfaces.ITermOccurrenceFilterService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.IUIService;
import de.julielab.semedico.core.services.query.ConceptRecognitionService;
import de.julielab.semedico.core.services.query.LexerService;
import de.julielab.semedico.core.services.query.ParsingService;
import de.julielab.semedico.core.services.query.QueryAnalysisService;
import de.julielab.semedico.core.suggestions.ITermSuggestionService;
import de.julielab.semedico.core.suggestions.TermSuggestionService;

/**
 * This is the Tapestry5 IoC module class to define all services which belong to
 * Semedico's core functionality.
 * 
 * @author faessler
 */
@ImportModule({ElasticQueryComponentsModule.class, SemedicoSearchModule.class})
public class SemedicoCoreModule {

	private ChainBuilder chainBuilder;
	private ITermService termService;

	public SemedicoCoreModule(ChainBuilder chainBuilder, ITermService termService) {
		this.chainBuilder = chainBuilder;
		this.termService = termService;
	}

	@Startup
	public static void scheduleJobs(PeriodicExecutor executor, ITermDocumentFrequencyService termDocFreqService) {
		// Cron-expression meaning "Trigger each day at 4 am"
		// executor.addJob(new CronSchedule("0 0 4 ? * *"),
		// "TermDocumentFrequency Job",
		// termDocFreqService);
	}

	@SuppressWarnings("unchecked")
	public static void bind(ServiceBinder binder) {

		binder.bind(ILexerService.class, LexerService.class);
		binder.bind(IParsingService.class, ParsingService.class);


		binder.bind(ITermSuggestionService.class, TermSuggestionService.class);
		binder.bind(ITermService.class, TermNeo4jService.class);
		binder.bind(IFacetService.class, FacetNeo4jService.class);
		binder.bind(IDictionaryReaderService.class, DictionaryReaderService.class);
		
		binder.bind(ISearchTermProvider.class, IdSearchTermProvider.class).withSimpleId();

		binder.bind(IIndexInformationService.class, IndexInformationService.class);

		binder.bind(IHttpClientService.class, HttpClientService.class).withMarker(GeneralHttpClient.class);
		binder.bind(INeo4jHttpClientService.class, Neo4jHttpClientService.class).withMarker(Neo4jHttpClient.class);

		binder.bind(IDBConnectionService.class, DBConnectionService.class);
		binder.bind(ITermDatabaseService.class, Neo4jService.class);
		binder.bind(TermNeo4jService.ShortestRootPathInFacetCacheLoader.class,
				TermNeo4jService.ShortestRootPathInFacetCacheLoader.class);
		binder.bind(TermNeo4jService.ShortestRootPathCacheLoader.class,
				TermNeo4jService.ShortestRootPathCacheLoader.class);
		binder.bind(TermNeo4jService.AllRootPathsInFacetCacheLoader.class,
				TermNeo4jService.AllRootPathsInFacetCacheLoader.class);
		binder.bind(ITermCreator.class, TermCreator.class);
		binder.bind(IFacetTermFactory.class, FacetTermFactory.class);
		// binder.bind(IEventFactory.class, EventFactory.class);
		binder.bind(IStringTermService.class, StringTermService.class).withId("StringTermService");
		binder.bind(IBioPortalOntologyRecommender.class, BioPortalOntologyRecommender.class);
		binder.bind(ITermDocumentFrequencyService.class, TermDocumentFrequencyService.class);
		binder.bind(IQueryAnalysisService.class, QueryAnalysisService.class);

		// binder.bind(IFacetDeterminer.class,
		// BioPortalFacetsFromQueryDeterminer.class).withMarker(
		// BioPortalFacetsFromQueryDetermination.class);

		binder.bind(IFacetDeterminerManager.class, FacetDeterminerManager.class);

		binder.bind(IStopWordService.class, StopWordService.class);

		binder.bind(IDocumentService.class, DocumentService.class);
		binder.bind(IDocumentCacheService.class, DocumentCacheService.class);
		binder.bind(IHighlightingService.class, HighlightingService.class);
		binder.bind(ILabelCacheService.class, LabelCacheService.class);

		binder.bind(IExternalLinkService.class, ExternalLinkService.class);
		binder.bind(IRelatedArticlesService.class, RelatedArticlesService.class);

		// Binding for tool services
		binder.bind(ITermOccurrenceFilterService.class, TermOccurrenceFilterService.class);

		// // added by hellrich for parsing
		// binder.bind(IParsingService.class, ParsingService.class);
		// // added by hellrich for rdf support
		// binder.bind(IRdfSearchService.class, RdfSearchService.class);

		binder.bind(IUIService.class, UIService.class);
		binder.bind(ISearchService.class, SearchService.class);

		binder.bind(ISearchComponent.class, QueryAnalysisComponent.class).withMarker(QueryAnalysis.class)
				.withId(QueryAnalysis.class.getSimpleName());
		binder.bind(ISearchComponent.class, QueryTranslationComponent.class).withMarker(QueryTranslation.class)
				.withId(QueryTranslation.class.getSimpleName());
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

		binder.bind(IQueryTranslator.class, TitleTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, AbstractTextTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, AllTextTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, StatementTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, SentenceTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, AbstractSectionTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, ParagraphTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, SectionTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, FigureCaptionTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, TableCaptionTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, DocMetaTranslator.class).withSimpleId();
		binder.bind(IQueryTranslator.class, MeshTranslator.class).withSimpleId();
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
			@InjectService("StatementTranslator") IQueryTranslator statementTranslator,
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
		configuration.add("EventTranslator", statementTranslator);
		configuration.add("TitleTranslator", titleTranslator);
		configuration.add("SentenceTranslator", sentencesTranslator);
		configuration.add("MeshTranslator", meshTranslator);
		configuration.add("sectionTranslator", sectionTranslator);
		
		configuration.add("DocMetaTranslator", docMetaTranslator);
//		configuration.add("AbstractSectionTranslator", abstractSectionTranslator);
//		configuration.add("paragraphTranslator", paragraphTranslator);
		configuration.add("FigureCaptionTranslator", figureCaptionTranslator);
		configuration.add("TableCaptionTranslator", tableCaptionTranslator);
	}

	/**
	 * @deprecated might be deprecated because we might just elasticsearch do
	 *             the query analysis
	 * @return
	 */
	@Deprecated
	@Scope(ScopeConstants.PERTHREAD)
	public IStemmerService buildSnowballStemmer() {
		SnowballProgram stemmer = null;
		try {
			Class<? extends SnowballProgram> stemClass = Class.forName("org.tartarus.snowball.ext.PorterStemmer")
					.asSubclass(SnowballProgram.class);
			stemmer = stemClass.newInstance();
			return new SnowballStemmerService(stemmer);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Marker(Primary.class)
	public ISearchTermProvider buildSearchTermProvider(List<ISearchTermProvider> provider) {
		return chainBuilder.build(ISearchTermProvider.class, provider);
	}

	@Marker(TermDocumentFrequencyChain.class)
	public ISearchComponent buildTermDocumentFrequencyChain(List<ISearchComponent> components) {
		return chainBuilder.build(ISearchComponent.class, components);
	}

	public void contributeSearchTermProvider(OrderedConfiguration<ISearchTermProvider> configuration) {
		configuration.addInstance("coreTermProvider", CoreTermSearchTermProvider.class);
		configuration.addInstance("idProvider", IdSearchTermProvider.class);
	}

	public ICacheService buildCacheService(Map<Region, CacheWrapper> configuration, Logger log) {
		return new CacheService(log, configuration);
	}

	public static Chunker buildTermDictionaryChunker(IDictionaryReaderService dictionaryReaderService,
			@Inject @Symbol(SemedicoSymbolConstants.TERM_DICT_FILE) String dictionaryFilePath, final Collection<DictionaryEntry> configuration) throws IOException {
		Dictionary<String> dictionary = dictionaryReaderService.getMapDictionary(dictionaryFilePath, configuration);
		Chunker chunker = new ExactDictionaryChunker(dictionary, IndoEuropeanTokenizerFactory.INSTANCE, true, false);
		return chunker;
	}
	
	public void contributeTermDictionaryChunker(Configuration<DictionaryEntry> configuration) {
		Map<String, CoreTerm> coreTerms = termService.getCoreTerms();
		for (CoreTerm concept : coreTerms.values()) {
			for (String occurrence : concept.getOccurrences())
				configuration.add(new DictionaryEntry(occurrence, concept.getId()));
		}
	}

	public IConceptRecognitionService buildTermRecognitionService(Chunker termChunker, SymbolSource symbolSource) {
		return new ConceptRecognitionService(termChunker, termService, symbolSource);
	}

	@Marker(DocumentPagingChain.class)
	public ISearchComponent buildDocumentPagingChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Marker(FacetCountChain.class)
	public ISearchComponent buildFacetCountChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Deprecated
	@Marker(FacetedDocumentSearchSubchain.class)
	public ISearchComponent buildFacetedDocumentSearchSubchain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Marker(FacetIndexTermsChain.class)
	public ISearchComponent buildFacetIndexTermsChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Marker(FieldTermsChain.class)
	public ISearchComponent buildFieldTermsChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	public static FacetRootCacheLoader buildFacetRootCacheLoader(LoggerSource loggerSource,
			ITermDatabaseService neo4jService, IFacetTermFactory termFactory) {
		return new FacetRootCacheLoader(loggerSource.getLogger(FacetRootCacheLoader.class), neo4jService, termFactory);
	}

	/**
	 * The cache loader for facets. The main purpose to make it a service on
	 * itself is the ability to get hold of the loading worker thread inside it
	 * so we can synchronize on it (mainly used for tests).
	 * 
	 * @param loggerSource
	 * @param neo4jService
	 * @return
	 * @see #contributeCacheService(MappedConfiguration, TermCacheLoader,
	 *      FacetTermRelationsCacheLoader, int, int)
	 * @see #buildFacetTermRelationsCacheLoader(LoggerSource,
	 *      ITermDatabaseService)
	 */
	public static TermCacheLoader buildFacetTermCacheLoader(LoggerSource loggerSource,
			ITermDatabaseService neo4jService, IFacetTermFactory termFactory) {
		return new TermNeo4jService.TermCacheLoader(loggerSource.getLogger(TermNeo4jService.TermCacheLoader.class),
				neo4jService, termFactory);
	}

	/**
	 * The cache loader for facets. The main purpose to make it a service on
	 * itself is the ability to get hold of the loading worker thread inside it
	 * so we can synchronize on it (mainly used for tests).
	 * 
	 * @param loggerSource
	 * @param neo4jService
	 * @return
	 * @see #contributeCacheService(MappedConfiguration, TermCacheLoader,
	 *      FacetTermRelationsCacheLoader, int, int)
	 * @see #buildTermCacheLoader(LoggerSource, INeo4jService)t
	 */
	public FacetTermRelationsCacheLoader buildFacetTermRelationsCacheLoader(LoggerSource loggerSource,
			ITermDatabaseService neo4jService) {
		return new TermNeo4jService.FacetTermRelationsCacheLoader(
				loggerSource.getLogger(TermNeo4jService.FacetTermRelationsCacheLoader.class), neo4jService,
				termService);
	}


	/**
	 * <p>
	 * Builds an ICU Collator for string comparison.
	 * </p>
	 * <p>
	 * The Collator's original task is to help for localized sorting, e.g. in
	 * French accents have influence on sorting order. A Collator may be used
	 * furthermore to declare equality between several characters and their
	 * substitutes, for instance 'ü' and 'ue'. This is useful when dealing with
	 * author names, which are very diverse in writing and character usage.
	 * </p>
	 * 
	 * @see <a href="http://userguide.icu-project.org/collation">ICU User
	 *      Manual</a>
	 * @return A rule based <code>Collator</code> that treats umlauts correctly
	 *         (i.e. returns '0' when comparing 'o' and 'oe' for example).
	 */
	public static IRuleBasedCollatorWrapper buildRuleBasedCollatorWrapper() {
		try {
			RuleBasedCollator collator = new RuleBasedCollator("& a << ae & o << oe & u << ue"
					+ "& A << Ae & O << Oe & U << Ue & " + "A << AE & O << OE & U << UE");
			collator.setStrength(Collator.PRIMARY);
			return new RuleBasedCollatorWrapper(collator);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Marker(SuggestionsChain.class)
	public ISearchComponent buildSuggestionsChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Marker(TotalNumDocsChain.class)
	public ISearchComponent buildTotalNumDocsChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	public static void contributeCacheService(MappedConfiguration<Region, CacheWrapper> configuration,
			TermCacheLoader termCacheLoader, FacetTermRelationsCacheLoader relationshipCacheLoader,
			FacetRootCacheLoader facetRootCacheLoader, ShortestRootPathInFacetCacheLoader rootPathInFacetCacheLoader,
			ShortestRootPathCacheLoader rootPathCacheLoader,
			AllRootPathsInFacetCacheLoader allRootPathsInFacetCacheLoader,
			@Symbol(SemedicoSymbolConstants.TERM_CACHE_SIZE) int termCacheSize,
			@Symbol(SemedicoSymbolConstants.RELATION_CACHE_SIZE) int relationshipsCacheSize,
			@Symbol(SemedicoSymbolConstants.FACET_ROOT_CACHE_SIZE) int facetRootCacheSize,
			@Symbol(SemedicoSymbolConstants.ROOT_PATH_CACHE_SIZE) int rootPathCacheSize) {

		LoadingCache<String, IConcept> termCache = CacheBuilder.newBuilder().maximumSize(termCacheSize)
				.build(termCacheLoader);
		LoadingCache<TermRelationKey, IFacetTermRelation> relationshipCache = CacheBuilder.newBuilder()
				.maximumSize(relationshipsCacheSize).build(relationshipCacheLoader);
		// termCacheLoader.setRelationshipCache(relationshipCache);
		relationshipCacheLoader.setTermCache(termCache);

		LoadingCache<String, List<Concept>> facetRootCache = CacheBuilder.newBuilder().maximumSize(facetRootCacheSize)
				.build(facetRootCacheLoader);
		facetRootCacheLoader.setTermCache(termCache);
		// facetRootCacheLoader.setRelationshipCache(relationshipCache);

		LoadingCache<TermFacetKey, IPath> rootPathInFacetCache = CacheBuilder.newBuilder()
				.maximumSize(rootPathCacheSize).build(rootPathInFacetCacheLoader);

		LoadingCache<String, IPath> rootPathCache = CacheBuilder.newBuilder().maximumSize(rootPathCacheSize)
				.build(rootPathCacheLoader);

		LoadingCache<Pair<String, String>, Collection<IPath>> allRootPathsInFacetCache = CacheBuilder.newBuilder()
				.maximumSize(rootPathCacheSize).build(allRootPathsInFacetCacheLoader);

		configuration.add(Region.TERM, new CacheWrapper(termCache));
		configuration.add(Region.RELATIONSHIP, new CacheWrapper(relationshipCache));
		configuration.add(Region.FACET_ROOTS, new CacheWrapper(facetRootCache));
		configuration.add(Region.SHORTEST_ROOT_PATH_IN_FACET, new CacheWrapper(rootPathInFacetCache));
		configuration.add(Region.ROOT_PATHS, new CacheWrapper(rootPathCache));
		configuration.add(Region.ROOT_PATHS_IN_FACET, new CacheWrapper(allRootPathsInFacetCache));
	}

	@Contribute(ISearchComponent.class)
	@DocumentPagingChain
	public static void contributeDocumentPagingChain(OrderedConfiguration<ISearchComponent> configuration,
			@QueryTranslation ISearchComponent queryTranslationComponent,
			@TextSearchPreparation ISearchComponent textSearchPreparationComponent,
			ISearchServerComponent solrSearchComponent,
			@ResultListCreation ISearchComponent resultListCreationComponent) {
		configuration.add("QueryTranslation", queryTranslationComponent);
		configuration.add("TextSearchPreparation", textSearchPreparationComponent);
		configuration.add("SearchServer", solrSearchComponent);
		configuration.add("ResultListCreats", resultListCreationComponent);
	}

	@Contribute(ISearchComponent.class)
	@FacetCountChain
	public static void contributeFacetCountChain(OrderedConfiguration<ISearchComponent> configuration,
			@QueryTranslation ISearchComponent queryTranslationComponent,
			@FacetCountPreparation ISearchComponent facetCountPreparationComponent,
			ISearchServerComponent solrSearchComponent,
			@FacetResponseProcess ISearchComponent facetResponseProcessComponent) {
		configuration.add("QueryTranslation", queryTranslationComponent);
		configuration.add("FacetCountPreparation", facetCountPreparationComponent);
		configuration.add("SearchServer", solrSearchComponent);
		configuration.add("FacetResponseProcess", facetResponseProcessComponent);
		// configuration.add("FacetChildrenSearchPreparation",
		// facetChildrenSearchPreparationComponent);
		// configuration.add("SolrSearchForChildren", solrSearchComponent);
		// configuration.add("FacetResponseProcessForChildren",
		// facetResponseProcessComponent);
	}

	@Deprecated
	@Contribute(ISearchComponent.class)
	@FacetedDocumentSearchSubchain
	public static void contributeFacetedDocumentSearchSubchain(OrderedConfiguration<ISearchComponent> configuration,
			@FacetCountPreparation ISearchComponent facetCountComponent, ISearchServerComponent searchServerComponent,
			@FacetResponseProcess ISearchComponent facetResponseProcessComponent,
			@ResultListCreation ISearchComponent resultListCreationComponent) {
		// configuration.add("FacetCountPreparation", facetCountComponent);
		configuration.add("SearchServer", searchServerComponent);
		// configuration.add("FacetResponseProcess",
		// facetResponseProcessComponent);
		configuration.add("ResultListCreation", resultListCreationComponent);
		// configuration.add("FacetChildrenSearchPreparation",
		// facetChildrenSearchPreparationComponent);
		// configuration.add("SolrSearchForChildren", searchServerComponent);
		// configuration.add("FacetResponseProcessForChildren",
		// facetResponseProcessComponent);
	}

	@Contribute(ISearchComponent.class)
	@FacetIndexTermsChain
	public static void contributeFacetIndexTermsChain(OrderedConfiguration<ISearchComponent> configuration,
			@FacetIndexTermsRetrieval ISearchComponent indexTermsRetrievalComponent,
			ISearchServerComponent searchServerComponent,
			@FacetIndexTermsProcess ISearchComponent indexTermsProcessComponent) {
		configuration.add(FacetIndexTermsRetrieval.class.getSimpleName(), indexTermsRetrievalComponent);
		configuration.add("SearchServer", searchServerComponent);
		configuration.add(FacetIndexTermsProcess.class.getSimpleName(), indexTermsProcessComponent);
	}

	@Contribute(ISearchComponent.class)
	@FieldTermsChain
	public static void contributeFieldTermsChain(OrderedConfiguration<ISearchComponent> configuration,
			@QueryTranslation ISearchComponent queryTranslationComponent,
			@FieldTermsRetrievalPreparation ISearchComponent indexTermsRetrievalComponent,
			ISearchServerComponent searchServerComponent,
			@FieldTermsProcess ISearchComponent indexTermsProcessComponent) {
		configuration.add(QueryTranslation.class.getSimpleName(), queryTranslationComponent);
		configuration.add(FieldTermsRetrievalPreparation.class.getSimpleName(), indexTermsRetrievalComponent);
		configuration.add("SearchServer", searchServerComponent);
		configuration.add(FieldTermsProcess.class.getSimpleName(), indexTermsProcessComponent);
	}

	@Contribute(ISearchComponent.class)
	@SuggestionsChain
	public static void contributeSuggestionsChain(OrderedConfiguration<ISearchComponent> configuration,
			@SuggestionPreparation ISearchComponent suggestionPreparationComponent,
			ISearchServerComponent searchServerComponent,
			@SuggestionProcess ISearchComponent suggestionProcessComponent) {
		configuration.add(SuggestionPreparation.class.getSimpleName(), suggestionPreparationComponent);
		configuration.add("SearchServer", searchServerComponent);
		configuration.add(SuggestionProcess.class.getSimpleName(), suggestionProcessComponent);
	}


	@Contribute(ISearchComponent.class)
	@TotalNumDocsChain
	public static void contributeTotalNumDocsChain(OrderedConfiguration<ISearchComponent> configuration,
			@TotalNumDocsPreparation ISearchComponent totalNumDocsPreparationComponent,
			ISearchServerComponent solrSearchComponent,
			@TotalNumDocsResponseProcess ISearchComponent totalNumDocsResponseComponent) {
		configuration.add("TotalNumDocsPreparation", totalNumDocsPreparationComponent);
		configuration.add("SolrSearch", solrSearchComponent);
		configuration.add("TotalNumDocsResponseProcess", totalNumDocsResponseComponent);
	}

}
