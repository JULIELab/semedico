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

import de.julielab.semedico.core.concepts.interfaces.IConceptRelation;
import de.julielab.semedico.core.search.services.IdSearchTermProvider;
import de.julielab.semedico.core.search.services.SearchService;
import de.julielab.semedico.core.services.interfaces.*;
import de.julielab.semedico.core.services.query.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Autobuild;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.Startup;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.apache.tapestry5.ioc.services.cron.PeriodicExecutor;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tartarus.snowball.SnowballProgram;

import com.aliasi.chunk.Chunker;
import com.aliasi.dict.Dictionary;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;

import de.julielab.elastic.query.components.ISearchComponent;
import de.julielab.elastic.query.components.ISearchServerComponent;
import de.julielab.semedico.core.TermFacetKey;
import de.julielab.semedico.core.ConceptRelationKey;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.interfaces.IPath;
import de.julielab.semedico.core.db.DBConnectionService;
import de.julielab.semedico.core.db.IDBConnectionService;
import de.julielab.semedico.core.concepts.CoreConcept;
//import de.julielab.semedico.core.facetterms.ConceptFactory;
import de.julielab.semedico.core.concepts.ConceptCreator;
import de.julielab.semedico.core.lingpipe.DictionaryReaderService;
import de.julielab.semedico.core.lingpipe.IDictionaryReaderService;
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
import de.julielab.semedico.core.search.components.FacetCountPreparationComponent.FacetCountPreparation;
import de.julielab.semedico.core.search.components.FacetIndexTermsProcessComponent.FacetIndexTermsProcess;
import de.julielab.semedico.core.search.components.FacetIndexTermsRetrievalComponent.FacetIndexTermsRetrieval;
import de.julielab.semedico.core.search.components.FacetResponseProcessComponent.FacetResponseProcess;
import de.julielab.semedico.core.search.components.QueryTranslationComponent.QueryTranslation;
import de.julielab.semedico.core.search.components.ResultListCreationComponent.ResultListCreation;
import de.julielab.semedico.core.search.components.SuggestionPreparationComponent.SuggestionPreparation;
import de.julielab.semedico.core.search.components.SuggestionProcessComponent.SuggestionProcess;
import de.julielab.semedico.core.search.components.TextSearchPreparationComponent.TextSearchPreparation;
import de.julielab.semedico.core.search.components.TotalNumDocsPreparationComponent.TotalNumDocsPreparation;
import de.julielab.semedico.core.search.components.TotalNumDocsResponseProcessComponent.TotalNumDocsResponseProcess;
import de.julielab.semedico.core.search.interfaces.IHighlightingService;
import de.julielab.semedico.core.search.interfaces.ILabelCacheService;
import de.julielab.semedico.core.search.services.SemedicoSearchModule;
import de.julielab.semedico.core.services.CacheService.CacheWrapper;
import de.julielab.semedico.core.services.ConceptNeo4jService.AllRootPathsInFacetCacheLoader;
import de.julielab.semedico.core.services.ConceptNeo4jService.FacetRootCacheLoader;
import de.julielab.semedico.core.services.ConceptNeo4jService.FacetTermRelationsCacheLoader;
import de.julielab.semedico.core.services.ConceptNeo4jService.ShortestRootPathCacheLoader;
import de.julielab.semedico.core.services.ConceptNeo4jService.ShortestRootPathInFacetCacheLoader;
import de.julielab.semedico.core.services.ConceptNeo4jService.TermCacheLoader;
import de.julielab.semedico.core.services.interfaces.ICacheService.Region;
import de.julielab.semedico.core.services.interfaces.IHttpClientService.GeneralHttpClient;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService.Neo4jHttpClient;
import de.julielab.semedico.core.search.services.ISearchService;
import de.julielab.semedico.core.search.services.ISearchTermProvider;
import de.julielab.semedico.core.suggestions.ITermSuggestionService;
import de.julielab.semedico.core.suggestions.TermSuggestionService;

/**
 * This is the Tapestry5 IoC module class to define all services which belong to
 * Semedico's core functionality.
 * 
 * @author faessler
 */
@ImportModule({ SemedicoSearchModule.class })
public class SemedicoCoreModule {

	public static final String NEO4J_VERSION = "3.3.1";

	private ChainBuilder chainBuilder;
	private ITermService termService;
	public static Logger searchTraceLog = LoggerFactory.getLogger("de.julielab.semedico.SearchTraceLogger");

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

		// Load eagerly for registration with the reconfiguration service.
		binder.bind(IConceptRecognitionService.class, ConceptRecognitionService.class).eagerLoad();
		binder.bind(IParsingService.class, ParsingService.class).eagerLoad();
		
		binder.bind(ILexerService.class, LexerService.class);

		binder.bind(ITermSuggestionService.class, TermSuggestionService.class);
		binder.bind(ITermService.class, ConceptNeo4jService.class);
		binder.bind(IFacetService.class, FacetNeo4jService.class);
		binder.bind(IDictionaryReaderService.class, DictionaryReaderService.class);

		binder.bind(ISearchTermProvider.class, IdSearchTermProvider.class).withSimpleId();

		binder.bind(IIndexInformationService.class, IndexInformationService.class);

		binder.bind(IHttpClientService.class, HttpClientService.class).withMarker(GeneralHttpClient.class);
		binder.bind(INeo4jHttpClientService.class, Neo4jHttpClientService.class).withMarker(Neo4jHttpClient.class);

		binder.bind(IDBConnectionService.class, DBConnectionService.class);
		binder.bind(IConceptDatabaseService.class, Neo4jService.class);
		binder.bind(ConceptNeo4jService.ShortestRootPathInFacetCacheLoader.class,
				ConceptNeo4jService.ShortestRootPathInFacetCacheLoader.class);
		binder.bind(ConceptNeo4jService.ShortestRootPathCacheLoader.class,
				ConceptNeo4jService.ShortestRootPathCacheLoader.class);
		binder.bind(ConceptNeo4jService.AllRootPathsInFacetCacheLoader.class,
				ConceptNeo4jService.AllRootPathsInFacetCacheLoader.class);
		binder.bind(IConceptCreator.class, ConceptCreator.class);
		//binder.bind(IConceptFactory.class, ConceptFactory.class);
		// binder.bind(IEventFactory.class, EventFactory.class);
		binder.bind(IStringTermService.class, StringTermService.class).withId("StringTermService");
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

	@Marker(TermDocumentFrequencyChain.class)
	public ISearchComponent buildTermDocumentFrequencyChain(List<ISearchComponent> components) {
		return chainBuilder.build(ISearchComponent.class, components);
	}

	public ICacheService buildCacheService(Map<Region, CacheWrapper> configuration, Logger log) {
		return new CacheService(log, configuration);
	}

	public IServiceReconfigurationHub buildServiceReconfigurationHub(Collection<ReconfigurableService> configuration) {
		return new ServiceReconfigurationHub(configuration);
	}
	
	public static Chunker buildTermDictionaryChunker(
			@Symbol(SemedicoSymbolConstants.QUERY_ANALYSIS) boolean recognizeQueryConcepts,
			IDictionaryReaderService dictionaryReaderService,
			@Inject @Symbol(SemedicoSymbolConstants.TERM_DICT_FILE) String dictionaryFilePath,
			final Collection<DictionaryEntry> configuration) throws IOException {
		Dictionary<String> dictionary;
		if (recognizeQueryConcepts)
			dictionary = dictionaryReaderService.getMapDictionary(dictionaryFilePath, configuration);
		else
			dictionary = new MapDictionary<>();
		Chunker chunker = new ExactDictionaryChunker(dictionary, IndoEuropeanTokenizerFactory.INSTANCE, true, false);
		return chunker;
	}

	public void contributeTermDictionaryChunker(Configuration<DictionaryEntry> configuration) {
		Map<String, CoreConcept> coreTerms = termService.getCoreTerms();
		for (CoreConcept concept : coreTerms.values()) {
			for (String occurrence : concept.getOccurrences())
				configuration.add(new DictionaryEntry(occurrence, concept.getId()));
		}
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

//	public static FacetRootCacheLoader buildFacetRootCacheLoader(LoggerSource loggerSource,
//			IConceptDatabaseService neo4jService, IConceptFactory conceptCreator) {
//		return new FacetRootCacheLoader(loggerSource.getLogger(FacetRootCacheLoader.class), neo4jService, conceptCreator);
//	}

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
	 *      IConceptDatabaseService)
	 */
	public static TermCacheLoader buildFacetTermCacheLoader(@Autobuild TermCacheLoader loader) {
		return loader;
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
			IConceptDatabaseService neo4jService) {
		return new ConceptNeo4jService.FacetTermRelationsCacheLoader(
				loggerSource.getLogger(ConceptNeo4jService.FacetTermRelationsCacheLoader.class), neo4jService,
				termService);
	}

	/**
	 * <p>
	 * Builds an ICU Collator for string comparison.
	 * </p>
	 * <p>
	 * The Collator's original searchScopes is to help for localized sorting, e.g. in
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
		LoadingCache<ConceptRelationKey, IConceptRelation> relationshipCache = CacheBuilder.newBuilder()
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

	public Driver buildBoltDriver(Logger log, @Symbol(SemedicoSymbolConstants.NEO4J_BOLT_URI) String boltUri, @Symbol(SemedicoSymbolConstants.NEO4J_USERNAME) String user, @Symbol(SemedicoSymbolConstants.NEO4J_PASSWORD) String password) {
		log.info("Connecting BOLT Neo4j client for the concept database to {}", boltUri);
		return GraphDatabase.driver(boltUri, AuthTokens.basic(user, password));
	}
	
}
