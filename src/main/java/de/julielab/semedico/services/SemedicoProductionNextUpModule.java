package de.julielab.semedico.services;

import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_INIT_CONN;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_MAX_CONN;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_NAME;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_PASSWORD;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_PORT;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_SERVER;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_USER;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DISPLAY_FACET_COUNT;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DISPLAY_MESSAGE_WHEN_NO_CHILDREN_HIT;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DISPLAY_MESSAGE_WHEN_NO_FACET_ROOTS_HIT;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DISPLAY_TERMS_MIN_HITS;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.EVENT_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.FACETS_LOAD_AT_START;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.FACET_ROOT_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.GET_HOLLOW_FACETS;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.LABELS_DEFAULT_NUMBER_DISPLAYED;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.LABEL_HIERARCHY_INIT_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.MAX_DISPLAYED_FACETS;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.MAX_NUMBER_SEARCH_NODES;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.NEO4J_REST_ENDPOINT;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.RELATION_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.ROOT_PATH_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SEARCH_MAX_NUMBER_DOC_HITS;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.STOP_WORDS_FILE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SUGGESTIONS_ACTIVATED;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SUGGESTIONS_FILTER_INDEX_TERMS;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SUGGESTIONS_INDEX_NAME;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.TERM_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.TERM_DICT_FILE;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.MappedConfiguration;

import de.julielab.elastic.query.ElasticQuerySymbolConstants;

public class SemedicoProductionNextUpModule {
	public static void contributeApplicationDefaults(MappedConfiguration<String, Object> configuration) {
		// The factory default is true but during the early stages of an application
		// overriding to false is a good idea. In addition, this is often overridden
		// on the command line as -Dtapestry.production-mode=false
		configuration.add(SymbolConstants.PRODUCTION_MODE, true);

		// The application version number is incorprated into URLs for some
		// assets. Web browsers will cache assets because of the far future expires
		// header. If existing assets are changed, the version number should also
		// change, to force the browser to download new versions.
		configuration.add(SymbolConstants.APPLICATION_VERSION, "3.0.0-SNAPSHOT-PRODUCTION-NEXTUP");

		// Contributions to ApplicationDefaults will be used when the
		// corresponding symbol is not delivered by any SymbolProvider and
		// override
		// any contributions to
		// FactoryDefaults (with the same key).
		// In Semedico, the defaults are meant to reflect the productive
		// environment while for testing a separate configuration file can be
		// used via SemedicoSymbolProvider.
		// Postgres is currently still used by hibernate (BBatchAnalysis)
		configuration.add(DATABASE_NAME, "semedico_stag_poc");
		configuration.add(DATABASE_SERVER, "darwin");
		configuration.add(DATABASE_USER, "postgres");
		configuration.add(DATABASE_PASSWORD, "$postgr3s$$");
		configuration.add(DATABASE_PORT, "5432");
		configuration.add(DATABASE_MAX_CONN, "4");
		configuration.add(DATABASE_INIT_CONN, "1");

		// ------------ Neo4j ---------------
		configuration.add(NEO4J_REST_ENDPOINT, "http://darwin:7474/");

		// configuration.add(SOLR_URL, "http://192.168.1.15:8983/solr/");

		// ------------ ELASTIC SEARCH ---------------
		// We use the "Transport Client" for ElasticSearch, so we need host and port.
		configuration.add(ElasticQuerySymbolConstants.ES_HOST, "dawkins");
		configuration.add(ElasticQuerySymbolConstants.ES_PORT, "9300");
		// We have to give the cluster name anyway because the ES client service requires it.
		configuration.add(ElasticQuerySymbolConstants.ES_CLUSTER_NAME, "semedicoNextUp");
		configuration.add(SUGGESTIONS_INDEX_NAME, "suggestions");
		configuration.add(SUGGESTIONS_ACTIVATED, "true");
		configuration.add(SUGGESTIONS_FILTER_INDEX_TERMS, "true");

		configuration.add(FACETS_LOAD_AT_START, "true");
		configuration.add(GET_HOLLOW_FACETS, "false");
		configuration.add(LABELS_DEFAULT_NUMBER_DISPLAYED, "3");
		configuration.add(LABEL_HIERARCHY_INIT_CACHE_SIZE, "5");
		configuration.add(MAX_NUMBER_SEARCH_NODES, "2");
		configuration.add(MAX_DISPLAYED_FACETS, "20");
		// configuration.add(TERMS_DO_NOT_BUILD_STRUCTURE, "false");
		configuration.add(DISPLAY_TERMS_MIN_HITS, "0");
		configuration.add(DISPLAY_MESSAGE_WHEN_NO_CHILDREN_HIT, "false");
		configuration.add(DISPLAY_MESSAGE_WHEN_NO_FACET_ROOTS_HIT, "false");
		configuration.add(DISPLAY_FACET_COUNT, "true");
		configuration.add(TERM_CACHE_SIZE, "5000000");
		configuration.add(EVENT_CACHE_SIZE, "100000");
		configuration.add(RELATION_CACHE_SIZE, "10000000");
		configuration.add(FACET_ROOT_CACHE_SIZE, "500");
		configuration.add(ROOT_PATH_CACHE_SIZE, "1000");
		// store into the DB?
		configuration.add(STOP_WORDS_FILE, "/data/semedico/production-data-nextup/stopwords.txt");
		// store into the DB?
		configuration.add(TERM_DICT_FILE, "/data/semedico/production-data-nextup/query.dic");
		configuration.add("semedico.core.search.maxFacettedDocuments", "300000");
		configuration.add(SEARCH_MAX_NUMBER_DOC_HITS, "10");
	}
}
