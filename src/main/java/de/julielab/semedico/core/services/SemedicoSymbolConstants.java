/**
 * ConfigurationService.java
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


public class SemedicoSymbolConstants {

	/**
	 * The name of the file ({@value #CONFIG_FILE_NAME}) which should hold the
	 * configuration values. It should be in a format which can be loaded by a
	 * <code>Properties</code> object. The file must be located on the class
	 * path in order to be found.
	 */
	public static final String CONFIG_FILE_NAME = "configuration.properties";

	public static final String DATABASE_SERVER = "semedico.database.server";
	public static final String DATABASE_NAME = "semedico.database.name";
	public static final String DATABASE_USER = "semedico.database.user";
	public static final String DATABASE_PASSWORD = "semedico.database.password";
	public static final String DATABASE_MAX_CONN = "semedico.database.maxConnections";
	public static final String DATABASE_PORT = "semedico.database.port";
	public static final String DATABASE_INIT_CONN = "semedico.database.initialConnections";

	public static final String TERMS_LOAD_AT_START = "semedico.terms.loadTermsAtStartUp";

	public static final String STOP_WORDS_FILE = "semedico.search.stopwords.file";
	public static final String DISAMBIGUATION_DICT_FILE = "semedico.query.dictionary.file";

	public static final String SOLR_URL = "semedico.solr.url";
	/**
	 * The name of the Solr core containing the suggestion index. The complete URL will be built
	 * by appending the value of this symbol to the value of {@value #SOLR_URL}.
	 */
	public static final String SOLR_SUGGESTIONS_CORE = "semedico.solr.suggestions.core";
	public static final String SEARCH_MAX_NUMBER_DOC_HITS = "semedico.core.search.maxNumberOfDocumentHits";
	public static final String SEARCH_MAX_FACETTED_DOCS = "semedico.core.search.maxFacettedDocuments";
	
	public static final String LABELS_DEFAULT_NUMBER_DISPLAYED = "semedico.frontend.labels.defaultNumberDisplayed";

	public static final String LABEL_HIERARCHY_INIT_CACHE_SIZE = "semedico.cache.labelHierarchy.size";

	// Tool configs
	public static final String TERM_FILE = "semedico.terms.file";

	@Deprecated
	public static final String SPELLING_DICT = "semedico.spelling.dictionary.file";


}
