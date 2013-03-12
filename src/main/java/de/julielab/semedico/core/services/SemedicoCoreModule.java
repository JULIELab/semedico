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

import static de.julielab.semedico.core.services.SemedicoSymbolConstants.*;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_MAX_CONN;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_NAME;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_PASSWORD;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_PORT;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_SERVER;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_USER;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.LABELS_DEFAULT_NUMBER_DISPLAYED;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.LABEL_HIERARCHY_INIT_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SOLR_SUGGESTIONS_CORE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SOLR_URL;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.TERMS_LOAD_AT_START;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.ServiceId;
import org.apache.tapestry5.ioc.annotations.Symbol;
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
import de.julielab.semedico.core.services.interfaces.IStopWordService;
import de.julielab.semedico.core.services.interfaces.IStringTermService;
import de.julielab.semedico.core.services.interfaces.ITermFileReaderService;
import de.julielab.semedico.core.services.interfaces.ITermOccurrenceFilterService;
import de.julielab.semedico.core.services.interfaces.ITermService;
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

	public static void bind(ServiceBinder binder) {
		binder.bind(IIndexInformationService.class, IndexInformationService.class);
		
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
		
		//added by hellrich for parsing
		binder.bind(IParsingService.class, ParsingService.class);
		//added by hellrich for rdf support
		binder.bind(IRdfSearchService.class, RdfSearchService.class);
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
