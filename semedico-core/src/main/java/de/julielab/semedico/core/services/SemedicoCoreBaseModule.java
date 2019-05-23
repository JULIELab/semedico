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
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.Startup;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.apache.tapestry5.ioc.services.cron.PeriodicExecutor;
import org.slf4j.Logger;
import org.tartarus.snowball.SnowballProgram;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Multimap;
import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;

import de.julielab.scicopia.core.elasticsearch.legacy.ISearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchServerComponent;
import de.julielab.scicopia.core.elasticsearch.ElasticsearchQueryBuilder;
import de.julielab.scicopia.core.elasticsearch.IElasticsearchQueryBuilder;
import de.julielab.scicopia.core.elasticsearch.legacy.ElasticQueryComponentsModule;
import de.julielab.semedico.core.TermFacetKey;
import de.julielab.semedico.core.TermRelationKey;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.interfaces.IFacetTermRelation;
import de.julielab.semedico.core.concepts.interfaces.IPath;
import de.julielab.semedico.core.facetterms.FacetTermFactory;
import de.julielab.semedico.core.facetterms.TermCreator;
import de.julielab.semedico.core.search.HighlightingService;
import de.julielab.semedico.core.search.LabelCacheService;
import de.julielab.semedico.core.search.annotations.ArticleChain;
import de.julielab.semedico.core.search.annotations.DocumentChain;
import de.julielab.semedico.core.search.annotations.DocumentPagingChain;
import de.julielab.semedico.core.search.annotations.FacetCountChain;
import de.julielab.semedico.core.search.annotations.FacetIndexTermsChain;
import de.julielab.semedico.core.search.annotations.FacetedDocumentSearchSubchain;
import de.julielab.semedico.core.search.annotations.FieldTermsChain;
import de.julielab.semedico.core.search.annotations.SuggestionsChain;
import de.julielab.semedico.core.search.annotations.TermDocumentFrequencyChain;
import de.julielab.semedico.core.search.annotations.TermSelectChain;
import de.julielab.semedico.core.search.annotations.TotalNumDocsChain;
import de.julielab.semedico.core.search.components.ArticleResponseProcessComponent;
import de.julielab.semedico.core.search.components.ArticleResponseProcessComponent.ArticleResponseProcess;
import de.julielab.semedico.core.search.components.ArticleSearchPreparationComponent;
import de.julielab.semedico.core.search.components.ArticleSearchPreparationComponent.ArticleSearchPreparation;
import de.julielab.semedico.core.search.components.FacetCountPreparationComponent;
import de.julielab.semedico.core.search.components.FacetCountPreparationComponent.FacetCountPreparation;
import de.julielab.semedico.core.search.components.FacetDfCountPreparationComponent;
import de.julielab.semedico.core.search.components.FacetDfCountPreparationComponent.FacetDfCountPreparation;
import de.julielab.semedico.core.search.components.FacetDfResponseProcessComponent;
import de.julielab.semedico.core.search.components.FacetDfResponseProcessComponent.FacetDfResponseProcess;
import de.julielab.semedico.core.search.components.FacetIndexTermsProcessComponent;
import de.julielab.semedico.core.search.components.FacetIndexTermsProcessComponent.FacetIndexTermsProcess;
import de.julielab.semedico.core.search.components.FacetIndexTermsRetrievalComponent;
import de.julielab.semedico.core.search.components.FacetIndexTermsRetrievalComponent.FacetIndexTermsRetrieval;
import de.julielab.semedico.core.search.components.FacetResponseProcessComponent;
import de.julielab.semedico.core.search.components.FacetResponseProcessComponent.FacetResponseProcess;
import de.julielab.semedico.core.search.components.FieldTermsProcessComponent;
import de.julielab.semedico.core.search.components.FieldTermsProcessComponent.FieldTermsProcess;
import de.julielab.semedico.core.search.components.FieldTermsRetrievalPreparationComponent;
import de.julielab.semedico.core.search.components.FieldTermsRetrievalPreparationComponent.FieldTermsRetrievalPreparation;
import de.julielab.semedico.core.search.components.FromQueryUIPreparatorComponent;
import de.julielab.semedico.core.search.components.FromQueryUIPreparatorComponent.FromQueryUIPreparation;
import de.julielab.scicopia.core.search.components.ElasticsearchQueryComponent.ElasticsearchQuery;
import de.julielab.scicopia.core.search.components.ElasticsearchQueryComponent;
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
import de.julielab.semedico.core.services.interfaces.ICacheService;
import de.julielab.semedico.core.services.interfaces.IDictionaryReaderService;
import de.julielab.semedico.core.services.interfaces.ICacheService.Region;
import de.julielab.semedico.core.services.interfaces.IDocumentService;
import de.julielab.semedico.core.services.interfaces.IExternalLinkService;
import de.julielab.semedico.core.services.interfaces.IFacetTermFactory;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.IHttpClientService.GeneralHttpClient;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.ILexerService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService.Neo4jHttpClient;
import de.julielab.semedico.core.services.interfaces.IParsingService;
import de.julielab.semedico.core.services.interfaces.IQueryAnalysisService;
import de.julielab.semedico.core.services.interfaces.IRuleBasedCollatorWrapper;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.core.services.interfaces.ISearchTermProvider;
import de.julielab.semedico.core.services.interfaces.IStemmerService;
import de.julielab.semedico.core.services.interfaces.IStopWordService;
import de.julielab.semedico.core.services.interfaces.ITermCreator;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseService;
import de.julielab.semedico.core.services.interfaces.ITermDocumentFrequencyService;
import de.julielab.semedico.core.services.interfaces.ITermOccurrenceFilterService;
import de.julielab.semedico.core.services.interfaces.ITermRecognitionService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.IUIService;
import de.julielab.scicopia.core.parsing.DisambiguatingRangeChunker;
import de.julielab.scicopia.core.parsing.LegacyLexerService;
import de.julielab.semedico.core.services.query.ParsingService;
import de.julielab.semedico.core.services.query.QueryAnalysisService;
import de.julielab.semedico.core.services.query.TermRecognitionService;

/**
 * This is the Tapestry5 IoC module class to define all services which belong to
 * Semedico's core functionality.
 * 
 * @author faessler
 */
@ImportModule({ElasticQueryComponentsModule.class, SemedicoSearchModule.class})
public class SemedicoCoreBaseModule {

	private ChainBuilder chainBuilder;
	private ITermService termService;

	public SemedicoCoreBaseModule(ChainBuilder chainBuilder, ITermService termService) {
		this.chainBuilder = chainBuilder;
		this.termService = termService;
	}

	@Startup
	public static void scheduleJobs(PeriodicExecutor executor, ITermDocumentFrequencyService termDocFreqService) {
	}

	@SuppressWarnings("unchecked")
	public static void bind(ServiceBinder binder) {

		// -------------- QUERY SERVICES --------------

		binder.bind(ILexerService.class, LegacyLexerService.class);
		binder.bind(IParsingService.class, ParsingService.class);

		// -------------- END QUERY SERVICES --------------

		binder.bind(ISearchTermProvider.class, IdSearchTermProvider.class).withSimpleId();

		binder.bind(IIndexInformationService.class, IndexInformationService.class);

		binder.bind(IHttpClientService.class, HttpClientService.class).withMarker(GeneralHttpClient.class);
		binder.bind(INeo4jHttpClientService.class, Neo4jHttpClientService.class).withMarker(Neo4jHttpClient.class);

		binder.bind(ITermDatabaseService.class, Neo4jService.class);
		binder.bind(TermNeo4jService.ShortestRootPathInFacetCacheLoader.class,
				TermNeo4jService.ShortestRootPathInFacetCacheLoader.class);
		binder.bind(TermNeo4jService.ShortestRootPathCacheLoader.class,
				TermNeo4jService.ShortestRootPathCacheLoader.class);
		binder.bind(TermNeo4jService.AllRootPathsInFacetCacheLoader.class,
				TermNeo4jService.AllRootPathsInFacetCacheLoader.class);
		binder.bind(ITermCreator.class, TermCreator.class);
		binder.bind(IFacetTermFactory.class, FacetTermFactory.class);
		binder.bind(ITermDocumentFrequencyService.class, TermDocumentFrequencyService.class);
		binder.bind(IQueryAnalysisService.class, QueryAnalysisService.class);
		binder.bind(IElasticsearchQueryBuilder.class, ElasticsearchQueryBuilder.class);

		binder.bind(IStopWordService.class, StopWordService.class);

		binder.bind(IDocumentService.class, DocumentService.class);
		binder.bind(IHighlightingService.class, HighlightingService.class);
		binder.bind(ILabelCacheService.class, LabelCacheService.class);

		binder.bind(IExternalLinkService.class, ExternalLinkService.class);

		// Binding for tool services
		binder.bind(ITermOccurrenceFilterService.class, TermOccurrenceFilterService.class);

		binder.bind(IUIService.class, UIService.class);
		binder.bind(ISearchService.class, SearchService.class);

		binder.bind(ISearchComponent.class, QueryAnalysisComponent.class).withMarker(QueryAnalysis.class)
		.withId(QueryAnalysis.class.getSimpleName());
		binder.bind(ISearchComponent.class, ElasticsearchQueryComponent.class).withMarker(ElasticsearchQuery.class)
		.withId(ElasticsearchQuery.class.getSimpleName());
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
		binder.bind(ISearchComponent.class, FacetDfCountPreparationComponent.class)
				.withMarker(FacetDfCountPreparation.class).withId(FacetDfCountPreparation.class.getSimpleName());
		binder.bind(ISearchComponent.class, FacetDfResponseProcessComponent.class)
				.withMarker(FacetDfResponseProcess.class).withId(FacetDfResponseProcess.class.getSimpleName());

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
		binder.bind(ISearchComponent.class, FieldTermsProcessComponent.class).withMarker(FieldTermsProcess.class)
				.withSimpleId();
	}

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
		configuration.addInstance("idProvider", IdSearchTermProvider.class);
	}

	@Marker(ArticleChain.class)
	public ISearchComponent buildArticleChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	public ICacheService buildCacheService(Map<Region, CacheWrapper> configuration, Logger log) {
		return new CacheService(log, configuration);
	}

	public static DisambiguatingRangeChunker buildTermDictionaryChunker(IDictionaryReaderService dictionaryReaderService,
			@Inject @Symbol(SemedicoSymbolConstants.TERM_DICT_FILE) String dictionaryFilePath, final Collection<DictionaryEntry> configuration) throws IOException {
		Multimap<String, String> dictionary = dictionaryReaderService.readDictionary(dictionaryFilePath);
		return new DisambiguatingRangeChunker(dictionary);
	}

	public ITermRecognitionService buildTermRecognitionService(DisambiguatingRangeChunker termChunker) {
		return new TermRecognitionService(termChunker, termService);
	}

	@Marker(DocumentChain.class)
	public ISearchComponent buildDocumentChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Marker(DocumentPagingChain.class)
	public ISearchComponent buildDocumentPagingChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Marker(FacetCountChain.class)
	public ISearchComponent buildFacetCountChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

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

	@Marker(TermSelectChain.class)
	public ISearchComponent buildTermSelectChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Marker(TotalNumDocsChain.class)
	public ISearchComponent buildTotalNumDocsChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Contribute(ISearchComponent.class)
	@TermDocumentFrequencyChain
	public void contributeTermDocumentFrequencyChain(OrderedConfiguration<ISearchComponent> configuration,
			@FacetDfCountPreparation ISearchComponent facetDfCountPreparationComponent,
			@QueryTranslation ISearchComponent queryTranslationComponent, ISearchServerComponent searchServerComponent,
			@FacetDfResponseProcess ISearchComponent facetDfResponseProcessComponent) {
		configuration.add("FacetDfCountPreparation", facetDfCountPreparationComponent);
		configuration.add("QueryTranslation", queryTranslationComponent);
		configuration.add("SearchServer", searchServerComponent);
		configuration.add("FacetDfResponseProcess", facetDfResponseProcessComponent);
	}

	@Contribute(ISearchComponent.class)
	@ArticleChain
	public static void contributeArticleChain(OrderedConfiguration<ISearchComponent> configuration,
			@QueryTranslation ISearchComponent queryTranslationComponent,
			@ArticleSearchPreparation ISearchComponent articleSearchPreparationComponent,
			ISearchServerComponent searchComponent,
			@ArticleResponseProcess ISearchComponent articleResponseProcessComponent) {
		configuration.add("QueryTranslation", queryTranslationComponent);
		configuration.add("ArticleSearchPreparation", articleSearchPreparationComponent);
		configuration.add("SearchServer", searchComponent);
		configuration.add("ArticleResponseProcess", articleResponseProcessComponent);
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
		relationshipCacheLoader.setTermCache(termCache);

		LoadingCache<String, List<Concept>> facetRootCache = CacheBuilder.newBuilder().maximumSize(facetRootCacheSize)
				.build(facetRootCacheLoader);
		facetRootCacheLoader.setTermCache(termCache);

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
	@DocumentChain
	public static void contributeDocumentChain(OrderedConfiguration<ISearchComponent> configuration,
			@NewSearchUIPreparation ISearchComponent newSearchUIPreparationComponent,
			@FromQueryUIPreparation ISearchComponent fromQueryUIPreparationComponent,
			@QueryAnalysis ISearchComponent queryAnalysisComponent,
			@ElasticsearchQuery ISearchComponent elasticSearchQueryComponent,
			@TextSearchPreparation ISearchComponent textSearchPreparationComponent,
			@FacetedDocumentSearchSubchain ISearchComponent facetedDocumentSearchSubchain
			) {
		configuration.add("NewSearchUIPreparation", newSearchUIPreparationComponent);
		configuration.add("ElasticsearchQuery", elasticSearchQueryComponent);
		configuration.add("QueryAnalysis", queryAnalysisComponent);
		configuration.add("TextSearchPreparation", textSearchPreparationComponent);
		configuration.add("FacetedDocumentSearch", facetedDocumentSearchSubchain);
		configuration.add("FromQueryUIPreparation", fromQueryUIPreparationComponent);
	}

	@Contribute(ISearchComponent.class)
	@DocumentPagingChain
	public static void contributeDocumentPagingChain(OrderedConfiguration<ISearchComponent> configuration,
			@ElasticsearchQuery ISearchComponent elasticSearchQueryComponent,
			@TextSearchPreparation ISearchComponent textSearchPreparationComponent,
			ISearchServerComponent solrSearchComponent,
			@ResultListCreation ISearchComponent resultListCreationComponent) {
		configuration.add("ElasticsearchQuery", elasticSearchQueryComponent);
		configuration.add("TextSearchPreparation", textSearchPreparationComponent);
		configuration.add("SearchServer", solrSearchComponent);
		configuration.add("ResultListCreats", resultListCreationComponent);
	}

	@Contribute(ISearchComponent.class)
	@FacetCountChain
	public static void contributeFacetCountChain(OrderedConfiguration<ISearchComponent> configuration,
			@QueryTranslation ISearchComponent queryTranslationComponent,
			@ElasticsearchQuery ISearchComponent elasticSearchQueryComponent,
			@FacetCountPreparation ISearchComponent facetCountPreparationComponent,
			ISearchServerComponent solrSearchComponent,
			@FacetResponseProcess ISearchComponent facetResponseProcessComponent) {
		configuration.add("QueryTranslation", queryTranslationComponent);
		configuration.add("FacetCountPreparation", facetCountPreparationComponent);
		configuration.add("SearchServer", solrSearchComponent);
		configuration.add("FacetResponseProcess", facetResponseProcessComponent);
	}

	@Contribute(ISearchComponent.class)
	@FacetedDocumentSearchSubchain
	public static void contributeFacetedDocumentSearchSubchain(OrderedConfiguration<ISearchComponent> configuration,
			ISearchServerComponent searchServerComponent,
			@ResultListCreation ISearchComponent resultListCreationComponent) {
		configuration.add("SearchServer", searchServerComponent);
		configuration.add("ResultListCreation", resultListCreationComponent);
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
	@TermSelectChain
	public static void contributeTermSelectChain(OrderedConfiguration<ISearchComponent> configuration,
			@TermSelectUIPreparation ISearchComponent TermSelectUIPreparationComponent,
			@QueryTranslation ISearchComponent queryTranslationComponent,
			@TextSearchPreparation ISearchComponent textSearchPreparationComponent,
			@FacetedDocumentSearchSubchain ISearchComponent facetedDocumentSearchSubchain) {
		configuration.add("TermSelectUIPreparation", TermSelectUIPreparationComponent);
		configuration.add("QueryTranslation", queryTranslationComponent);
		configuration.add("TextSearchPreparation", textSearchPreparationComponent);
		configuration.add("FacetedDocumentSearch", facetedDocumentSearchSubchain);
	}
}
