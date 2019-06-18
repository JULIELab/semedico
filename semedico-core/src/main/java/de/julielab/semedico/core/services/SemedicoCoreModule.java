/**
 * SemedicoCoreModule.java
 * <p>
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * <p>
 * Author: faessler
 * <p>
 * Current version: 1.0
 * Since version:   1.0
 * <p>
 * Creation date: 06.06.2011
 **/

package de.julielab.semedico.core.services;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Multimap;
import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;
import de.julielab.elastic.query.components.ISearchComponent;
import de.julielab.elastic.query.components.ISearchServerComponent;
import de.julielab.scicopia.core.elasticsearch.ElasticsearchQueryBuilder;
import de.julielab.scicopia.core.elasticsearch.IElasticsearchQueryBuilder;
import de.julielab.scicopia.core.parsing.DisambiguatingRangeChunker;
import de.julielab.scicopia.core.parsing.LexerService;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.ConceptCreator;
import de.julielab.semedico.core.concepts.CoreConcept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.interfaces.IConceptRelation;
import de.julielab.semedico.core.concepts.interfaces.IPath;
import de.julielab.semedico.core.entities.ConceptRelationKey;
import de.julielab.semedico.core.entities.TermFacetKey;
import de.julielab.semedico.core.search.LabelCacheService;
import de.julielab.semedico.core.search.annotations.*;
import de.julielab.semedico.core.search.components.FacetIndexTermsProcessComponent.FacetIndexTermsProcess;
import de.julielab.semedico.core.search.components.FacetIndexTermsRetrievalComponent.FacetIndexTermsRetrieval;
import de.julielab.semedico.core.search.components.QueryTranslationComponent.QueryTranslation;
import de.julielab.semedico.core.search.components.SuggestionPreparationComponent.SuggestionPreparation;
import de.julielab.semedico.core.search.components.SuggestionProcessComponent.SuggestionProcess;
import de.julielab.semedico.core.search.components.TextSearchPreparationComponent.TextSearchPreparation;
import de.julielab.semedico.core.search.components.TotalNumDocsPreparationComponent.TotalNumDocsPreparation;
import de.julielab.semedico.core.search.components.TotalNumDocsResponseProcessComponent.TotalNumDocsResponseProcess;
import de.julielab.semedico.core.search.interfaces.ILabelCacheService;
import de.julielab.semedico.core.search.services.*;
import de.julielab.semedico.core.services.CacheService.CacheWrapper;
import de.julielab.semedico.core.services.ConceptNeo4jService.*;
import de.julielab.semedico.core.services.interfaces.*;
import de.julielab.semedico.core.services.interfaces.ICacheService.Region;
import de.julielab.semedico.core.services.interfaces.IHttpClientService.GeneralHttpClient;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService.Neo4jHttpClient;
import de.julielab.semedico.core.services.query.*;
import de.julielab.semedico.core.suggestions.ConceptSuggestionService;
import de.julielab.semedico.core.suggestions.IConceptSuggestionService;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.annotations.*;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.apache.tapestry5.ioc.services.cron.PeriodicExecutor;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tartarus.snowball.SnowballProgram;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

//import de.julielab.semedico.core.facetterms.ConceptFactory;

/**
 * This is the Tapestry5 IoC module class to define all services which belong to
 * Semedico's core functionality.
 *
 * @author faessler
 */
@ImportModule({SemedicoSearchModule.class})
public class SemedicoCoreModule {

    public static final String NEO4J_VERSION = "3.3.1";
    public static Logger searchTraceLog = LoggerFactory.getLogger("de.julielab.semedico.SearchTraceLogger");
    private ChainBuilder chainBuilder;
    private IConceptService termService;

    public SemedicoCoreModule(ChainBuilder chainBuilder, IConceptService termService) {
        this.chainBuilder = chainBuilder;
        this.termService = termService;
    }

    /**
     * Activates the prerequisite checking classes if the production mode is set to <tt>false</tt>.
     * @param productionMode The application-wide production mode.
     */
    @Startup
    public static void activatePrerequisiteChecks(@Symbol(SymbolConstants.PRODUCTION_MODE) boolean productionMode) {
        if (!productionMode) {
            // Activates the prerequisite checks
            System.setProperty("de.julielab.prerequisitechecksenabled", "true");
        }
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

        binder.bind(IConceptSuggestionService.class, ConceptSuggestionService.class);
        binder.bind(IConceptService.class, ConceptNeo4jService.class);
        binder.bind(IFacetService.class, FacetNeo4jService.class);
        binder.bind(IDictionaryReaderService.class, DictionaryReaderService.class);
        binder.bind(IElasticsearchQueryBuilder.class, ElasticsearchQueryBuilder.class);

        binder.bind(ISearchTermProvider.class, IdSearchTermProvider.class).withSimpleId();

        binder.bind(IHttpClientService.class, HttpClientService.class).withMarker(GeneralHttpClient.class);
        binder.bind(INeo4jHttpClientService.class, Neo4jHttpClientService.class).withMarker(Neo4jHttpClient.class);

        binder.bind(IConceptDatabaseService.class, Neo4jService.class);
        binder.bind(ConceptNeo4jService.ShortestRootPathInFacetCacheLoader.class,
                ConceptNeo4jService.ShortestRootPathInFacetCacheLoader.class);
        binder.bind(ConceptNeo4jService.ShortestRootPathCacheLoader.class,
                ConceptNeo4jService.ShortestRootPathCacheLoader.class);
        binder.bind(ConceptNeo4jService.AllRootPathsInFacetCacheLoader.class,
                ConceptNeo4jService.AllRootPathsInFacetCacheLoader.class);
        binder.bind(IConceptCreator.class, ConceptCreator.class);
        binder.bind(ITermDocumentFrequencyService.class, TermDocumentFrequencyService.class);
        binder.bind(IQueryAnalysisService.class, QueryAnalysisService.class);

        binder.bind(IFacetDeterminerManager.class, FacetDeterminerManager.class);

        binder.bind(IStopWordService.class, StopWordService.class);

        binder.bind(ILabelCacheService.class, LabelCacheService.class);

        binder.bind(IExternalLinkService.class, ExternalLinkService.class);
        binder.bind(IRelatedArticlesService.class, RelatedArticlesService.class);

        // Binding for tool services
        binder.bind(ITermOccurrenceFilterService.class, TermOccurrenceFilterService.class);

        binder.bind(IUIService.class, UIService.class);
        binder.bind(ISearchService.class, SearchService.class);

    }

    public static DisambiguatingRangeChunker buildTermDictionaryChunker(IDictionaryReaderService dictionaryReaderService,
                                                                        @Inject @Symbol(SemedicoSymbolConstants.TERM_DICT_FILE) String dictionaryFilePath, final Collection<DictionaryEntry> configuration) throws IOException {
        Multimap<String, String> dictionary = dictionaryReaderService.readDictionary(dictionaryFilePath);
        return new DisambiguatingRangeChunker(dictionary);
    }

    /**
     * The cache loader for facets. The main purpose to make it a service on
     * itself is the ability to get hold of the loading worker thread inside it
     * so we can synchronize on it (mainly used for tests).
     *
     * @return
     * @see #contributeCacheService(MappedConfiguration, ConceptCacheLoader, ConceptRelationsCacheLoader, FacetRootCacheLoader, ShortestRootPathInFacetCacheLoader, ShortestRootPathCacheLoader, AllRootPathsInFacetCacheLoader, int, int, int, int)
     * @see #buildFacetTermRelationsCacheLoader(LoggerSource,
     *      IConceptDatabaseService)
     */
    public static ConceptCacheLoader buildFacetTermCacheLoader(@Autobuild ConceptCacheLoader loader) {
        return loader;
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

    public static void contributeCacheService(MappedConfiguration<Region, CacheWrapper> configuration,
                                              ConceptCacheLoader conceptCacheLoader, ConceptRelationsCacheLoader relationshipCacheLoader,
                                              FacetRootCacheLoader facetRootCacheLoader, ShortestRootPathInFacetCacheLoader rootPathInFacetCacheLoader,
                                              ShortestRootPathCacheLoader rootPathCacheLoader,
                                              AllRootPathsInFacetCacheLoader allRootPathsInFacetCacheLoader,
                                              @Symbol(SemedicoSymbolConstants.TERM_CACHE_SIZE) int termCacheSize,
                                              @Symbol(SemedicoSymbolConstants.RELATION_CACHE_SIZE) int relationshipsCacheSize,
                                              @Symbol(SemedicoSymbolConstants.FACET_ROOT_CACHE_SIZE) int facetRootCacheSize,
                                              @Symbol(SemedicoSymbolConstants.ROOT_PATH_CACHE_SIZE) int rootPathCacheSize) {

        LoadingCache<String, IConcept> termCache = CacheBuilder.newBuilder().maximumSize(termCacheSize)
                .build(conceptCacheLoader);
        LoadingCache<ConceptRelationKey, IConceptRelation> relationshipCache = CacheBuilder.newBuilder()
                .maximumSize(relationshipsCacheSize).build(relationshipCacheLoader);
        // conceptCacheLoader.setRelationshipCache(relationshipCache);
        relationshipCacheLoader.setConceptCache(termCache);

        LoadingCache<String, List<Concept>> facetRootCache = CacheBuilder.newBuilder().maximumSize(facetRootCacheSize)
                .build(facetRootCacheLoader);
        facetRootCacheLoader.setConceptCache(termCache);
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
                                                     ISearchServerComponent solrSearchComponent) {
        configuration.add("QueryTranslation", queryTranslationComponent);
        configuration.add("TextSearchPreparation", textSearchPreparationComponent);
        configuration.add("SearchServer", solrSearchComponent);
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

//	public static FacetRootCacheLoader buildFacetRootCacheLoader(LoggerSource loggerSource,
//			IConceptDatabaseService neo4jService, IConceptFactory conceptCreator) {
//		return new FacetRootCacheLoader(loggerSource.getLogger(FacetRootCacheLoader.class), neo4jService, conceptCreator);
//	}

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

    public void contributeTermDictionaryChunker(Configuration<DictionaryEntry> configuration) {
        Map<String, CoreConcept> coreTerms = termService.getCoreConcepts();
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

    /**
     * The cache loader for facets. The main purpose to make it a service on
     * itself is the ability to get hold of the loading worker thread inside it
     * so we can synchronize on it (mainly used for tests).
     *
     * @param loggerSource
     * @param neo4jService
     * @return
     * @see #contributeCacheService(MappedConfiguration, ConceptCacheLoader, ConceptRelationsCacheLoader, FacetRootCacheLoader, ShortestRootPathInFacetCacheLoader, ShortestRootPathCacheLoader, AllRootPathsInFacetCacheLoader, int, int, int, int)
     * @see #buildFacetTermCacheLoader(ConceptCacheLoader)
     */
    public ConceptRelationsCacheLoader buildFacetTermRelationsCacheLoader(LoggerSource loggerSource,
                                                                          IConceptDatabaseService neo4jService) {
        return new ConceptRelationsCacheLoader(
                loggerSource.getLogger(ConceptRelationsCacheLoader.class), neo4jService,
                termService);
    }

    public FacetRootCacheLoader buildFacetRootCacheLoader(LoggerSource loggerSource, IConceptDatabaseService neo4jService, IConceptCreator conceptCreator) {
        return new ConceptNeo4jService.FacetRootCacheLoader(loggerSource.getLogger(FacetRootCacheLoader.class), neo4jService, conceptCreator);
    }

    @Marker(SuggestionsChain.class)
    public ISearchComponent buildSuggestionsChain(List<ISearchComponent> commands) {
        return chainBuilder.build(ISearchComponent.class, commands);
    }

    @Marker(TotalNumDocsChain.class)
    public ISearchComponent buildTotalNumDocsChain(List<ISearchComponent> commands) {
        return chainBuilder.build(ISearchComponent.class, commands);
    }

    public Driver buildBoltDriver(Logger log, @Symbol(SemedicoSymbolConstants.NEO4J_BOLT_URI) String boltUri, @Symbol(SemedicoSymbolConstants.NEO4J_USERNAME) String user, @Symbol(SemedicoSymbolConstants.NEO4J_PASSWORD) String password) {
        log.info("Connecting BOLT Neo4j client for the concept database to {}", boltUri);
        return GraphDatabase.driver(boltUri, AuthTokens.basic(user, password));
    }

}
