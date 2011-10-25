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
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.LABEL_HIERARCHY_INIT_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.LABELS_DEFAULT_NUMBER_DISPLAYED;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SOLR_SUGGESTIONS_CORE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SOLR_URL;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.TERMS_LOAD_AT_START;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.StringDistance;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import com.aliasi.chunk.Chunker;
import com.aliasi.dict.Dictionary;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import de.julielab.db.DBConnectionService;
import de.julielab.db.IDBConnectionService;
import de.julielab.lingpipe.DictionaryReaderService;
import de.julielab.lingpipe.IDictionaryReaderService;
import de.julielab.semedico.query.IQueryDictionaryBuilderService;
import de.julielab.semedico.query.IQueryDisambiguationService;
import de.julielab.semedico.query.IQueryTranslationService;
import de.julielab.semedico.query.QueryDictionaryBuilderService;
import de.julielab.semedico.query.QueryDisambiguationService;
import de.julielab.semedico.query.QueryTranslationService;
import de.julielab.semedico.search.IFacetedSearchService;
import de.julielab.semedico.search.IKwicService;
import de.julielab.semedico.search.ILabelCacheService;
import de.julielab.semedico.search.KwicService;
import de.julielab.semedico.search.LabelCacheService;
import de.julielab.semedico.search.SolrSearchService;
import de.julielab.semedico.spelling.ISpellCheckerService;
import de.julielab.semedico.spelling.SpellCheckerService;
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

	public static Chunker buildDictionaryChunker(
			IDictionaryReaderService dictionaryReaderService) {
		Dictionary<String> dictionary = dictionaryReaderService
				.getMapDictionary();
		Chunker chunker = new ExactDictionaryChunker(dictionary,
				IndoEuropeanTokenizerFactory.FACTORY, true, false);
		return chunker;
	}

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
		binder.bind(IDBConnectionService.class, DBConnectionService.class);
		binder.bind(IFacetService.class, FacetService.class);
		
		binder.bind(ITermService.class, TermService.class).eagerLoad();

		binder.bind(ITermSuggestionService.class, SolrTermSuggestionService.class);

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

		// TODO remove together with lucene spelling correction when replace by
		// solr spelling correction
		binder.bind(StringDistance.class, LevensteinDistance.class);
		binder.bind(ISpellCheckerService.class, SpellCheckerService.class);
		
		//Binding for tool services
		binder.bind(ITermFileReaderService.class, TermFileReaderService.class);
		binder.bind(ITermOccurrenceFilterService.class, TermOccurrenceFilterService.class);
		binder.bind(IQueryDictionaryBuilderService.class, QueryDictionaryBuilderService.class);
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
		configuration.add(DATABASE_NAME, "semedico_stag");
		configuration.add(DATABASE_SERVER,
				"s15");
		configuration.add(DATABASE_USER, "postgres");
		configuration.add(DATABASE_PASSWORD, "postgres");
		configuration.add(DATABASE_PORT, "5432");
		configuration.add(DATABASE_MAX_CONN, "4");
		configuration.add(DATABASE_INIT_CONN, "1");

		configuration.add(SOLR_URL, "http://s15:8983/solr/");
		configuration.add(SOLR_SUGGESTIONS_CORE, "suggestions");

		configuration.add(TERMS_LOAD_AT_START, "true");
		configuration.add(LABELS_DEFAULT_NUMBER_DISPLAYED, "3");
		configuration.add(LABEL_HIERARCHY_INIT_CACHE_SIZE, "5");
		configuration.add("semedico.search.index.path",
				"/home/chew/Coding/stemnet-frontend/mainIndex");
		configuration.add("semedico.suggestions.index.path",
				"/mnt/work/data/semedico/suggestionIndex");
		// store into the DB?
		configuration.add("semedico.search.stopwords.file",
				"/mnt/work/data/semedico/stopwords.txt");
		configuration.add("semedico.query.termindex",
				"/home/chew/Coding/stemnet-frontend/queryIndex");
		// store into the DB?
		configuration.add("semedico.query.dictionary.file",
				"/mnt/work/data/semedico/query.dic");
		configuration
				.add("semedico.core.search.maxFacettedDocuments", "300000");
		configuration.add("semedico.core.search.maxNumberOfDocumentHits", "10");
		configuration.add("semedico.spelling.dictionary.file",
				"/mnt/work/data/semedico/spelling.dic");

	}

}
