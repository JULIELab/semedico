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

import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_INIT_CONN;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_MAX_CONN;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_NAME;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_PASSWORD;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_PORT;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_SERVER;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_USER;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.LABELS_DEFAULT_NUMBER_DISPLAYED;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.LABEL_HIERARCHY_INIT_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.MAX_NUMBER_SEARCH_NODES;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SOLR_SUGGESTIONS_CORE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SOLR_URL;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.TERMS_LOAD_AT_START;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.ServiceId;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.slf4j.Logger;

import com.aliasi.chunk.Chunker;
import com.aliasi.dict.Dictionary;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;

import de.julielab.db.DBConnectionService;
import de.julielab.db.IDBConnectionService;
import de.julielab.lingpipe.DictionaryReaderService;
import de.julielab.lingpipe.IDictionaryReaderService;
import de.julielab.semedico.bterms.BTermService;
import de.julielab.semedico.bterms.interfaces.IBTermService;
import de.julielab.semedico.core.services.interfaces.IDocumentCacheService;
import de.julielab.semedico.core.services.interfaces.IDocumentService;
import de.julielab.semedico.core.services.interfaces.IExternalLinkService;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.IJournalService;
import de.julielab.semedico.core.services.interfaces.IRelatedArticlesService;
import de.julielab.semedico.core.services.interfaces.IRuleBasedCollatorWrapper;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.core.services.interfaces.IStopWordService;
import de.julielab.semedico.core.services.interfaces.IStringTermService;
import de.julielab.semedico.core.services.interfaces.ITermFileReaderService;
import de.julielab.semedico.core.services.interfaces.ITermOccurrenceFilterService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.IUIService;
import de.julielab.semedico.query.IQueryDictionaryBuilderService;
import de.julielab.semedico.query.IQueryDisambiguationService;
import de.julielab.semedico.query.IQueryTranslationService;
import de.julielab.semedico.query.QueryDictionaryBuilderService;
import de.julielab.semedico.query.QueryDisambiguationService;
import de.julielab.semedico.query.QueryTranslationService;
import de.julielab.semedico.search.IRdfSearchService;
import de.julielab.semedico.search.KwicService;
import de.julielab.semedico.search.LabelCacheService;
import de.julielab.semedico.search.RdfSearchService;
import de.julielab.semedico.search.SolrSearchService;
import de.julielab.semedico.search.components.FacetChildrenCountPreparatorComponent;
import de.julielab.semedico.search.components.FacetChildrenCountPreparatorComponent.FacetChildrenCountPreparator;
import de.julielab.semedico.search.components.FacetCountPreparatorComponent;
import de.julielab.semedico.search.components.FacetCountPreparatorComponent.FacetCountPreparator;
import de.julielab.semedico.search.components.FacetDfCountPreparatorComponent;
import de.julielab.semedico.search.components.FacetDfCountPreparatorComponent.FacetDfCountPreparator;
import de.julielab.semedico.search.components.FacetDfResponseProcessComponent;
import de.julielab.semedico.search.components.FacetDfResponseProcessComponent.FacetDfResponseProcess;
import de.julielab.semedico.search.components.FacetResponseProcessComponent;
import de.julielab.semedico.search.components.FacetResponseProcessComponent.FacetResponseProcess;
import de.julielab.semedico.search.components.ISearchComponent;
import de.julielab.semedico.search.components.ISearchComponent.DocumentChain;
import de.julielab.semedico.search.components.ISearchComponent.FacetCountChain;
import de.julielab.semedico.search.components.ISearchComponent.FacetedDocumentSearchSubchain;
import de.julielab.semedico.search.components.ISearchComponent.IndirectLinksChain;
import de.julielab.semedico.search.components.ISearchComponent.SearchNodeTermCountsSubchain;
import de.julielab.semedico.search.components.ISearchComponent.SwitchSearchNodeChain;
import de.julielab.semedico.search.components.ISearchComponent.TermSelectChain;
import de.julielab.semedico.search.components.ISearchComponent.TotalNumDocsChain;
import de.julielab.semedico.search.components.IndirectLinksDeterminationComponent;
import de.julielab.semedico.search.components.IndirectLinksDeterminationComponent.IndirectLinksDetermination;
import de.julielab.semedico.search.components.IndirectLinksProcessComponent;
import de.julielab.semedico.search.components.IndirectLinksProcessComponent.IndirectLinksProcess;
import de.julielab.semedico.search.components.NewSearchUIPreparationComponent;
import de.julielab.semedico.search.components.NewSearchUIPreparationComponent.NewSearchUIPreparation;
import de.julielab.semedico.search.components.QueryAnalysisComponent;
import de.julielab.semedico.search.components.QueryAnalysisComponent.QueryAnalysis;
import de.julielab.semedico.search.components.QueryTranslationComponent;
import de.julielab.semedico.search.components.QueryTranslationComponent.QueryTranslation;
import de.julielab.semedico.search.components.ResultListCreatorComponent;
import de.julielab.semedico.search.components.ResultListCreatorComponent.ResultListCreator;
import de.julielab.semedico.search.components.SearchNodeQueryTranslationComponent;
import de.julielab.semedico.search.components.SearchNodeQueryTranslationComponent.SearchNodeQueryTranslation;
import de.julielab.semedico.search.components.SearchNodeTermCountCollectorComponent;
import de.julielab.semedico.search.components.SearchNodeTermCountCollectorComponent.SearchNodeTermCountCollector;
import de.julielab.semedico.search.components.SolrSearchComponent;
import de.julielab.semedico.search.components.SolrSearchComponent.SolrSearch;
import de.julielab.semedico.search.components.TermSelectUIPreparationComponent;
import de.julielab.semedico.search.components.TermSelectUIPreparationComponent.TermSelectUIPreparation;
import de.julielab.semedico.search.components.TextSearchPreparatorComponent;
import de.julielab.semedico.search.components.TextSearchPreparatorComponent.TextSearchPreparator;
import de.julielab.semedico.search.components.TotalNumDocsPreparatorComponent;
import de.julielab.semedico.search.components.TotalNumDocsPreparatorComponent.TotalNumDocsPreparator;
import de.julielab.semedico.search.components.TotalNumDocsResponseProcessComponent;
import de.julielab.semedico.search.components.TotalNumDocsResponseProcessComponent.TotalNumDocsResponseProcess;
import de.julielab.semedico.search.interfaces.IFacetedSearchService;
import de.julielab.semedico.search.interfaces.IKwicService;
import de.julielab.semedico.search.interfaces.ILabelCacheService;
import de.julielab.semedico.suggestions.ITermSuggestionService;
import de.julielab.semedico.suggestions.SolrTermSuggestionService;

/**
 * This is the Tapestry5 IoC module class to define all services which belong to
 * Semedico's core functionality.
 * 
 * @author faessler
 */
// This module is loaded by the SubModule annotation from the frontend or the
// tools.
public class SemedicoCoreModule {

	@Marker(DocumentChain.class)
	public static ISearchComponent buildDocumentChain(
			List<ISearchComponent> commands,
			@InjectService("ChainBuilder") ChainBuilder chainBuilder) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Contribute(ISearchComponent.class)
	@DocumentChain
	public static void contributeDocumentChain(
			OrderedConfiguration<ISearchComponent> configuration,
			@NewSearchUIPreparation ISearchComponent newSearchUIPreparationComponent,
			@QueryAnalysis ISearchComponent queryAnalysisComponent,
			@QueryTranslation ISearchComponent queryTranslationComponent,
			@TextSearchPreparator ISearchComponent textSearchPreparatorComponent,
			@FacetCountPreparator ISearchComponent facetCountComponent,
			@FacetedDocumentSearchSubchain ISearchComponent facetedDocumentSearchSubchain) {
		configuration.add("NewSearchUIPreparation",
				newSearchUIPreparationComponent);
		configuration.add("QueryAnalysis", queryAnalysisComponent);
		configuration.add("QueryTranslation", queryTranslationComponent);
		configuration
				.add("TextSearchPreparator", textSearchPreparatorComponent);
		configuration.add("FacetCountPreparator", facetCountComponent);
		configuration.add("FacetedDocumentSearch",
				facetedDocumentSearchSubchain);
	}

	@Marker(TermSelectChain.class)
	public static ISearchComponent buildTermSelectChain(
			List<ISearchComponent> commands,
			@InjectService("ChainBuilder") ChainBuilder chainBuilder) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Contribute(ISearchComponent.class)
	@TermSelectChain
	public static void contributeTermSelectChain(
			OrderedConfiguration<ISearchComponent> configuration,
			@TermSelectUIPreparation ISearchComponent TermSelectUIPreparationComponent,
			@QueryTranslation ISearchComponent queryTranslationComponent,
			@TextSearchPreparator ISearchComponent textSearchPreparatorComponent,
			@FacetCountPreparator ISearchComponent facetCountComponent,
			@FacetedDocumentSearchSubchain ISearchComponent facetedDocumentSearchSubchain) {
		configuration.add("TermSelectUIPreparation",
				TermSelectUIPreparationComponent);
		configuration.add("QueryTranslation", queryTranslationComponent);
		configuration
				.add("TextSearchPreparator", textSearchPreparatorComponent);
		configuration.add("FacetCountPreparator", facetCountComponent);
		configuration.add("FacetedDocumentSearch",
				facetedDocumentSearchSubchain);
	}

	@Marker(FacetCountChain.class)
	public static ISearchComponent buildFacetCountChain(
			List<ISearchComponent> commands,
			@InjectService("ChainBuilder") ChainBuilder chainBuilder) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Contribute(ISearchComponent.class)
	@FacetCountChain
	public static void contributeFacetCountChain(
			OrderedConfiguration<ISearchComponent> configuration,
			@FacetCountPreparator ISearchComponent facetCountPreparatorComponent,
			@SolrSearch ISearchComponent solrSearchComponent,
			@FacetResponseProcess ISearchComponent facetResponseProcessComponent,
			@FacetChildrenCountPreparator ISearchComponent facetChildrenSearchPreparatorComponent) {
		configuration
				.add("FacetCountPreparator", facetCountPreparatorComponent);
		configuration.add("SolrSearch", solrSearchComponent);
		configuration
				.add("FacetResponseProcess", facetResponseProcessComponent);
		configuration.add("FacetChildrenSearchPreparator",
				facetChildrenSearchPreparatorComponent);
		configuration.add("SolrSearchForChildren", solrSearchComponent);
		configuration.add("FacetResponseProcessForChildren",
				facetResponseProcessComponent);
	}

	@Marker(SwitchSearchNodeChain.class)
	public static ISearchComponent buildSwitchSearchNodeChain(
			List<ISearchComponent> commands,
			@InjectService("ChainBuilder") ChainBuilder chainBuilder) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Contribute(ISearchComponent.class)
	@SwitchSearchNodeChain
	public static void contributeSwitchSearchNodeChain(
			OrderedConfiguration<ISearchComponent> configuration,
			@NewSearchUIPreparation ISearchComponent NewSearchUIPreparationComponent,
			@TextSearchPreparator ISearchComponent textSearchPreparatorComponent,
			@FacetCountPreparator ISearchComponent facetCountComponent,
			@FacetedDocumentSearchSubchain ISearchComponent facetedDocumentSearchSubchain) {
		// We currently don't memorize the UI state of search nodes, so just
		// reset everything
		configuration.add("NewSearchUIPreparation",
				NewSearchUIPreparationComponent);
		configuration
				.add("TextSearchPreparator", textSearchPreparatorComponent);
		configuration.add("FacetCountPreparator", facetCountComponent);
		configuration.add("FacetedDocumentSearch",
				facetedDocumentSearchSubchain);
	}

	@Marker(TotalNumDocsChain.class)
	public static ISearchComponent buildTotalNumDocsChain(
			List<ISearchComponent> commands,
			@InjectService("ChainBuilder") ChainBuilder chainBuilder) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Contribute(ISearchComponent.class)
	@TotalNumDocsChain
	public static void contributeTotalNumDocsChain(
			OrderedConfiguration<ISearchComponent> configuration,
			@TotalNumDocsPreparator ISearchComponent totalNumDocsPreparatorComponent,
			@SolrSearch ISearchComponent solrSearchComponent,
			@TotalNumDocsResponseProcess ISearchComponent totalNumDocsResponseComponent) {
		configuration.add("TotalNumDocsPreparator",
				totalNumDocsPreparatorComponent);
		configuration.add("SolrSearch", solrSearchComponent);
		configuration.add("TotalNumDocsResponseProcess",
				totalNumDocsResponseComponent);
	}

	@Marker(IndirectLinksChain.class)
	public static ISearchComponent buildIndirectLinksChain(
			List<ISearchComponent> commands,
			@InjectService("ChainBuilder") ChainBuilder chainBuilder) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Contribute(ISearchComponent.class)
	@IndirectLinksChain
	public static void contributeIndirectLinksChain(
			OrderedConfiguration<ISearchComponent> configuration,
			@SearchNodeTermCountCollector ISearchComponent searchNodeTermCountsCollectorComponent,
			@TotalNumDocsChain ISearchComponent totalNumDocsChain,
			@IndirectLinksDetermination ISearchComponent indirectLinksDeterminationComponent,
			@IndirectLinksProcess ISearchComponent indirectLinksProcessComponent) {
		configuration.add("SearchNodeTermCountCollector",
				searchNodeTermCountsCollectorComponent);
		configuration.add("TotalNumDocsChain", totalNumDocsChain);
		configuration.add("IndirectLinksDetermination",
				indirectLinksDeterminationComponent);
		configuration
				.add("IndirectLinksProcess", indirectLinksProcessComponent);

	}

	@Marker(FacetedDocumentSearchSubchain.class)
	public static ISearchComponent buildFacetedDocumentSearchSubchain(
			List<ISearchComponent> commands,
			@InjectService("ChainBuilder") ChainBuilder chainBuilder) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Contribute(ISearchComponent.class)
	@FacetedDocumentSearchSubchain
	public static void contributeFacetedDocumentSearchSubchain(
			OrderedConfiguration<ISearchComponent> configuration,
			@SolrSearch ISearchComponent solrSearchComponent,
			@FacetResponseProcess ISearchComponent facetResponseProcessComponent,
			@ResultListCreator ISearchComponent resultListCreatorComponent,
			@FacetChildrenCountPreparator ISearchComponent facetChildrenSearchPreparatorComponent) {
		configuration.add("SolrSearch", solrSearchComponent);
		configuration
				.add("FacetResponseProcess", facetResponseProcessComponent);
		configuration.add("ResultListCreator", resultListCreatorComponent);
		configuration.add("FacetChildrenSearchPreparator",
				facetChildrenSearchPreparatorComponent);
		configuration.add("SolrSearchForChildren", solrSearchComponent);
		configuration.add("FacetResponseProcessForChildren",
				facetResponseProcessComponent);
	}

	@Marker(SearchNodeTermCountsSubchain.class)
	public static ISearchComponent buildSearchNodeTermCountsSubchain(
			List<ISearchComponent> commands,
			@InjectService("ChainBuilder") ChainBuilder chainBuilder) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Contribute(ISearchComponent.class)
	@SearchNodeTermCountsSubchain
	public static void contributeSearchNodeTermCountsSubchain(
			OrderedConfiguration<ISearchComponent> configuration,
			@SearchNodeQueryTranslation ISearchComponent searchNodeQueryTranslationComponent,
			@FacetDfCountPreparator ISearchComponent facetDfCountPreparatorComponent,
			@SolrSearch ISearchComponent solrSearchComponent,
			@FacetDfResponseProcess ISearchComponent facetDfResponseProcessComponent) {
		configuration.add("SearchNodeQueryTranslation",
				searchNodeQueryTranslationComponent);
		configuration.add("FacetDfCountPreparator",
				facetDfCountPreparatorComponent);
		configuration.add("SolrSearch", solrSearchComponent);
		configuration.add("FacetDfResponseProcess",
				facetDfResponseProcessComponent);
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
			RuleBasedCollator collator = new RuleBasedCollator(
					"& a << ae & o << oe & u << ue"
							+ "& A << Ae & O << Oe & U << Ue"
							+ "A << AE & O << OE & U << UE");
			collator.setStrength(Collator.PRIMARY);
			return new RuleBasedCollatorWrapper(collator);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Chunker buildDictionaryChunker(
			IDictionaryReaderService dictionaryReaderService) {
		Dictionary<String> dictionary = dictionaryReaderService
				.getMapDictionary();
		Chunker chunker = new ExactDictionaryChunker(dictionary,
				IndoEuropeanTokenizerFactory.INSTANCE, true, false);
		return chunker;
	}

	@ServiceId("SolrSearcher")
	public static SolrServer buildSolrServer(Logger logger,
			@Symbol(SemedicoSymbolConstants.SOLR_URL) String url) {
		try {
			return new CommonsHttpSolrServer(url);
		} catch (MalformedURLException e) {
			logger.error(
					"URL \"{}\" to the Solr search server is malformed: {}",
					url, e);
		}
		return null;
	}

	@ServiceId("SolrSuggester")
	public static SolrServer buildSolrSuggestionServer(Logger logger,
			@Symbol(SemedicoSymbolConstants.SOLR_URL) String url,
			@Symbol(SOLR_SUGGESTIONS_CORE) String suggestionsCoreName) {
		String suggestionsCoreUrl = url;
		suggestionsCoreUrl += url.endsWith("/") ? suggestionsCoreName : "/"
				+ suggestionsCoreName;
		logger.info("Connecting to Suggestion Solr core at {}.",
				suggestionsCoreUrl);
		return new HttpSolrServer(suggestionsCoreUrl);
	}

	// TODO use solr spelling correction
	@Deprecated
	public static org.apache.lucene.search.spell.Dictionary buildSpellingDictionary(
			@Symbol(SemedicoSymbolConstants.SPELLING_DICT) String file) {
		try {
			return new PlainTextDictionary(new File(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static void bind(ServiceBinder binder) {
		binder.bind(IIndexInformationService.class,
				IndexInformationService.class);

		binder.bind(IDBConnectionService.class, DBConnectionService.class);
		binder.bind(IFacetService.class, FacetService.class);

		binder.bind(ITermService.class, TermService.class).eagerLoad();
		binder.bind(IStringTermService.class, StringTermService.class).withId(
				"StringTermService");

		binder.bind(ITermSuggestionService.class,
				SolrTermSuggestionService.class);

		binder.bind(IStopWordService.class, StopWordService.class);
		binder.bind(IDictionaryReaderService.class,
				DictionaryReaderService.class);
		binder.bind(IQueryDisambiguationService.class,
				QueryDisambiguationService.class);

		binder.bind(IQueryTranslationService.class,
				QueryTranslationService.class);
		binder.bind(IDocumentService.class, DocumentService.class);
		binder.bind(IDocumentCacheService.class, DocumentCacheService.class);
		binder.bind(IKwicService.class, KwicService.class);
		binder.bind(IFacetedSearchService.class, SolrSearchService.class);
		binder.bind(ILabelCacheService.class, LabelCacheService.class);

		binder.bind(IExternalLinkService.class, ExternalLinkService.class);
		binder.bind(IRelatedArticlesService.class, RelatedArticlesService.class);

		binder.bind(IJournalService.class, JournalService.class);

		binder.bind(IBTermService.class, BTermService.class);

		// Binding for tool services
		binder.bind(ITermFileReaderService.class, TermFileReaderService.class);
		binder.bind(ITermOccurrenceFilterService.class,
				TermOccurrenceFilterService.class);
		binder.bind(IQueryDictionaryBuilderService.class,
				QueryDictionaryBuilderService.class);

		// added by hellrich for parsing
		binder.bind(IParsingService.class, ParsingService.class);
		// added by hellrich for rdf support
		binder.bind(IRdfSearchService.class, RdfSearchService.class);

		binder.bind(IUIService.class, UIService.class);
		binder.bind(ISearchService.class, SearchService.class);

		binder.bind(ISearchComponent.class, QueryAnalysisComponent.class)
				.withMarker(QueryAnalysis.class).withId("QueryAnalysis");
		binder.bind(ISearchComponent.class, QueryTranslationComponent.class)
				.withMarker(QueryTranslation.class).withId("QueryTranslation");
		binder.bind(ISearchComponent.class, TextSearchPreparatorComponent.class)
				.withMarker(TextSearchPreparator.class)
				.withId("TextSearchPreparator");
		binder.bind(ISearchComponent.class, SolrSearchComponent.class)
				.withMarker(SolrSearch.class).withId("SolrSearch");
		binder.bind(ISearchComponent.class, FacetResponseProcessComponent.class)
				.withMarker(FacetResponseProcess.class)
				.withId("FacetResponseProcess");
		binder.bind(ISearchComponent.class, ResultListCreatorComponent.class)
				.withMarker(ResultListCreator.class)
				.withId("ResultListeCreator");
		binder.bind(ISearchComponent.class,
				FacetChildrenCountPreparatorComponent.class)
				.withMarker(FacetChildrenCountPreparator.class)
				.withId("FacetChildrenCountPreparator");
		binder.bind(ISearchComponent.class, FacetCountPreparatorComponent.class)
				.withMarker(FacetCountPreparator.class)
				.withId("FacetCountPreparator");
		binder.bind(ISearchComponent.class,
				NewSearchUIPreparationComponent.class)
				.withMarker(NewSearchUIPreparation.class)
				.withId("NewSearchUIPreparator");
		binder.bind(ISearchComponent.class,
				TermSelectUIPreparationComponent.class)
				.withMarker(TermSelectUIPreparation.class)
				.withId("TermSelectUIPreparator");
		binder.bind(ISearchComponent.class,
				FacetDfCountPreparatorComponent.class)
				.withMarker(FacetDfCountPreparator.class)
				.withId("FacetDfCountPreparator");
		binder.bind(ISearchComponent.class,
				FacetDfResponseProcessComponent.class)
				.withMarker(FacetDfResponseProcess.class)
				.withId("FacetDfResponseProcess");
		binder.bind(ISearchComponent.class,
				SearchNodeTermCountCollectorComponent.class)
				.withMarker(SearchNodeTermCountCollector.class)
				.withId("SearchNodeTermCountCollector");
		binder.bind(ISearchComponent.class,
				IndirectLinksDeterminationComponent.class)
				.withMarker(IndirectLinksDetermination.class)
				.withId("IndirectLinksDetermination");
		binder.bind(ISearchComponent.class, IndirectLinksProcessComponent.class)
				.withMarker(IndirectLinksProcess.class)
				.withId("IndirectLinksProcess");
		binder.bind(ISearchComponent.class,
				SearchNodeQueryTranslationComponent.class)
				.withMarker(SearchNodeQueryTranslation.class)
				.withId("SearchNodeQueryTranslation");

		binder.bind(ISearchComponent.class,
				TotalNumDocsPreparatorComponent.class)
				.withMarker(TotalNumDocsPreparator.class)
				.withId("TotalNumDocsPreparator");
		binder.bind(ISearchComponent.class,
				TotalNumDocsResponseProcessComponent.class)
				.withMarker(TotalNumDocsResponseProcess.class)
				.withId("TotalNumDocsResponseProcess");
	}

	public static void contributeFactoryDefaults(
			MappedConfiguration<String, String> configuration) {
		// Contributions to ApplicationDefaults will be used when the
		// corresponding symbol is not delivered by any SymbolProvider and
		// override
		// any contributions to
		// FactoryDefaults (with the same key).
		// In Semedico, the defaults are meant to reflect the productive
		// environment while for testing a separate configuration file can be
		// used via SemedicoSymbolProvider.
		configuration.add(DATABASE_NAME, "semedico_stag_poc");
		configuration.add(DATABASE_SERVER, "darwin");
		configuration.add(DATABASE_USER, "postgres");
		configuration.add(DATABASE_PASSWORD, "$postgr3s$$");
		configuration.add(DATABASE_PORT, "5432");
		configuration.add(DATABASE_MAX_CONN, "4");
		configuration.add(DATABASE_INIT_CONN, "1");

		configuration.add(SOLR_URL, "http://192.168.1.15:8983/solr/");
		configuration.add(SOLR_SUGGESTIONS_CORE, "suggestions");

		configuration.add(TERMS_LOAD_AT_START, "true");
		configuration.add(LABELS_DEFAULT_NUMBER_DISPLAYED, "3");
		configuration.add(LABEL_HIERARCHY_INIT_CACHE_SIZE, "5");
		configuration.add(MAX_NUMBER_SEARCH_NODES, "2");
		configuration.add("semedico.search.index.path",
				"/home/chew/Coding/stemnet-frontend/mainIndex");
		configuration.add("semedico.suggestions.index.path",
				"/mnt/work/data/semedico/suggestionIndex");
		// store into the DB?
		configuration.add("semedico.search.stopwords.file",
				"/mnt/work/data/semedico_ageing/stopwords.txt");
		configuration.add("semedico.query.termindex",
				"/home/chew/Coding/stemnet-frontend/queryIndex");
		// store into the DB?
		configuration.add("semedico.query.dictionary.file",
				"/mnt/work/data/semedico_ageing/query.dic");
		configuration
				.add("semedico.core.search.maxFacettedDocuments", "300000");
		configuration.add("semedico.core.search.maxNumberOfDocumentHits", "10");
		configuration.add("semedico.spelling.dictionary.file",
				"/mnt/work/data/semedico_ageing/spelling.dic");

	}

}
