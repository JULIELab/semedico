/**
 * ConfigurationService.java
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


public class SemedicoSymbolConstants {

    /**
     * The name of the file ({@value #CONFIG_FILE_NAME}) which should hold the
     * configuration values. It should be in a format which can be loaded by a
     * <code>Properties</code> object. The file must be located on the class
     * path in order to be found.
     */
    public static final String CONFIG_FILE_NAME = "configuration.properties";

    /**
     * @Deprecated Use host and port instead
     */
    @Deprecated
    public static final String NEO4J_REST_ENDPOINT = "semedico.neo4j.rest.endpoint";
    /**
     * @Deprecated Use host and port instead
     */
    public static final String NEO4J_BOLT_URI = "semedico.neo4j.bolt.uri";
    public static final String NEO4J_HOST = "semedico.neo4j.host";
    public static final String NEO4J_HTTP_PORT = "semedico.neo4j.http.port";
    public static final String NEO4J_BOLT_PORT = "semedico.neo4j.bolt.port";
    public static final String NEO4J_USERNAME = "semedico.neo4j.username";
    public static final String NEO4J_PASSWORD = "semedico.neo4j.password";

    public static final String DATABASE_SERVER = "semedico.database.server";
    public static final String DATABASE_NAME = "semedico.database.name";
    public static final String DATABASE_USER = "semedico.database.user";
    public static final String DATABASE_PASSWORD = "semedico.database.password";
    public static final String DATABASE_MAX_CONN = "semedico.database.maxConnections";
    public static final String DATABASE_PORT = "semedico.database.port";
    public static final String DATABASE_INIT_CONN = "semedico.database.initialConnections";

    @Deprecated
    public static final String TERMS_LOAD_AT_START = "semedico.terms.loadAtStartup";
    public static final String TERMS_DO_NOT_BUILD_STRUCTURE = "semedico.terms.doNotBuildStructure";

    public static final String FACETS_LOAD_AT_START = "semedico.facets.loadAtStartup";
    /**
     * A parameter specifying whether or not to return facets that have only
     * HOLLOW root terms. Default is false, mainly set to true for unit tests.
     */
    public static final String GET_HOLLOW_FACETS = "semedico.facets.returnHollow";

    public static final String STOP_WORDS_FILE = "semedico.search.stopwords.file";
    public static final String TERM_DICT_FILE = "semedico.query.dictionary.terms.file";
    // public static final String EVENT_DICT_FILE =
    // "semedico.query.dictionary.events.file";

    public static final String SOLR_URL = "semedico.solr.url";
    /**
     * The name of the index containing the suggestions. The complete URL will
     * be built by appending the value of this symbol to the value of
     * {@value #SOLR_URL}.
     */
    public static final String SUGGESTIONS_INDEX_NAME = "semedico.index.biomed.suggestions.name";
    public static final String SUGGESTIONS_ACTIVATED = "semedico.suggestions.activated";

    public static final String BIOMED_PUBLICATIONS_INDEX_NAME = "semedico.index.biomed.publications.name";

    /**
     * If this symbol is set to <tt>true</tt>, not all terms in the database are
     * indexed but only those which are actually found in the search server
     * index.
     */
    public static final String SUGGESTIONS_FILTER_INDEX_TERMS = "semedico.suggestions.filterIndexTerms";

    public static final String LABEL_HIERARCHY_INIT_CACHE_SIZE = "semedico.cache.labelHierarchy.size";

    public static final String DISPLAY_TERMS_MIN_HITS = "semedico.terms.show.minhits";

    public static final String DISPLAY_MESSAGE_WHEN_NO_CHILDREN_HIT = "semedico.terms.show.messagewhennochildrenhit";

    public static final String DISPLAY_MESSAGE_WHEN_NO_FACET_ROOTS_HIT = "semedico.terms.show.messagewhennotfacetrootshit";

    public static final String DISPLAY_FACET_COUNT = "semedico.terms.show.facetcount";

    // Tool configs
    public static final String TERM_FILE = "semedico.terms.file";

    public static final String MAX_DISPLAYED_FACETS = "semedico.frontend.facets.maxdisplayed";

    public static final String TERM_CACHE_SIZE = "semedico.cache.terms.size";
    public static final String RELATION_CACHE_SIZE = "semedico.cache.relationships.size";
    public static final String FACET_ROOT_CACHE_SIZE = "semedico.cache.facetroots.size";
    public static final String ROOT_PATH_CACHE_SIZE = "semedico.cache.rootpaths.size";

    // Query and search settings
    /**
     * Expects the name of an enum constant in {@link de.julielab.semedico.core.search.query.QueryAnalysis}
     */
    public static final String QUERY_ANALYSIS = "semedico.query.concepts.analysis";
    /**
     * Expects the name of an enum constant in {@link de.julielab.semedico.core.search.query.translation.ConceptTranslation}
     */
    public static final String CONCEPT_TRANSLATION = "semedico.query.concepts.translation";

    /**
     * Expects the name of either {@link de.julielab.semedico.core.parsing.Node.NodeType#AND} or
     * {@link de.julielab.semedico.core.parsing.Node.NodeType#OR}
     */
    public static final String PARSING_DEFAULT_OPERATOR = "semedico.query.parsing.defaultoperator";

    /**
     * File path or classpath location of the topic model.
     */
    public static final String TOPIC_MODEL_PATH = "semedico.topicmodel.path";
    /**
     * Boolean parameter stating whether the topic model should be used completely for search.
     * This only applies for queries that completely consist of {@link de.julielab.semedico.core.concepts.TopicTag}
     * tokens, i.e. are preceded by the '#' character.
     */
    public static final String TOPIC_MODEL_SEARCH = "semedico.topicmodel.search";
    public static final String TOPIC_MODEL_CONFIG = "semedico.topicmodel.configuration";
}
