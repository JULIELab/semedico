package de.julielab.semedico.core;

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
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.LABEL_HIERARCHY_INIT_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.MAX_DISPLAYED_FACETS;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SUGGESTIONS_ACTIVATED;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SUGGESTIONS_FILTER_INDEX_TERMS;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SUGGESTIONS_INDEX_NAME;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.elastic.query.ElasticQuerySymbolConstants;
import de.julielab.semedico.core.services.SemedicoCoreProductionModule;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;

public class TestUtils {

	private static final Logger log = LoggerFactory.getLogger(TestUtils.class);

	public static final String neo4jTestEndpoint = "http://localhost:7474/";
	public static final String neo4jTestUser = "neo4j";
	public static final String neo4jTestPassword = "julielab";
	public static final String searchServerUrl = "http://localhost:9200/";

	public static Registry createTestRegistry() {
		return createTestRegistry(SemedicoCoreProductionModule.class);
	}
	
	public static Registry createTestRegistry(Class<?> moduleClass) {
		setTestConfigurationSystemProperties();
		
		RegistryBuilder builder = new RegistryBuilder();

		builder.add(moduleClass);

		return builder.build();
	}

	public static void setTestConfigurationSystemProperties() {
		System.setProperty(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT,
				neo4jTestEndpoint);
		System.setProperty(SemedicoSymbolConstants.NEO4J_USERNAME, neo4jTestUser);
		System.setProperty(SemedicoSymbolConstants.NEO4J_PASSWORD, neo4jTestPassword);
		System.setProperty(SemedicoSymbolConstants.TERM_CACHE_SIZE, "500");
		System.setProperty(SemedicoSymbolConstants.RELATION_CACHE_SIZE, "500");
		System.setProperty(SemedicoSymbolConstants.ROOT_PATH_CACHE_SIZE, "500");
		System.setProperty(ElasticQuerySymbolConstants.ES_HOST, "localhost");
		System.setProperty(ElasticQuerySymbolConstants.ES_PORT, "9300");
		// Deactivate the node client to avoid the long startup times in unit
		// tests.
		System.setProperty(ElasticQuerySymbolConstants.ES_CLUSTER_NAME, "semedicoDev");
		System.setProperty(SemedicoSymbolConstants.STOP_WORDS_FILE,
				"src/test/resources/test_stopwords.txt");
		System.setProperty(SemedicoSymbolConstants.TERM_DICT_FILE,
				"src/test/resources/query-test.dic");
		System.setProperty(SemedicoSymbolConstants.GET_HOLLOW_FACETS, "true");
		
		
		System.setProperty(DATABASE_NAME, "semedico_stag_poc");
		System.setProperty(DATABASE_SERVER, "darwin");
		System.setProperty(DATABASE_USER, "postgres");
		System.setProperty(DATABASE_PASSWORD, "$postgr3s$$");
		System.setProperty(DATABASE_PORT, "5432");
		System.setProperty(DATABASE_MAX_CONN, "4");
		System.setProperty(DATABASE_INIT_CONN, "1");

		System.setProperty(SUGGESTIONS_ACTIVATED, "true");
		System.setProperty(SUGGESTIONS_FILTER_INDEX_TERMS, "false");
		System.setProperty(SUGGESTIONS_INDEX_NAME, "suggestions");

		System.setProperty(FACETS_LOAD_AT_START, "true");
		System.setProperty(LABEL_HIERARCHY_INIT_CACHE_SIZE, "5");
		System.setProperty(MAX_DISPLAYED_FACETS, "20");
		System.setProperty(DISPLAY_TERMS_MIN_HITS, "0");
		System.setProperty(DISPLAY_MESSAGE_WHEN_NO_CHILDREN_HIT, "false");
		System.setProperty(DISPLAY_MESSAGE_WHEN_NO_FACET_ROOTS_HIT, "false");
		System.setProperty(DISPLAY_FACET_COUNT, "true");
		System.setProperty(EVENT_CACHE_SIZE, "100000");
		System.setProperty(FACET_ROOT_CACHE_SIZE, "10");
		System.setProperty("semedico.core.search.maxFacettedDocuments", "300000");
	}

	public static boolean isAddressReachable(String address) {
		boolean reachable = false;
		try {
			URLConnection connection = new URL(address).openConnection();
			connection.connect();
			// If we've come this far without an exception, the connection is
			// available.
			reachable = true;
		} catch (ConnectException e) {
			// don't do anything, the warning will be logged below.
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!reachable)
			log.warn(
					"TESTS INVOLVING ADDRESS \"{}\" ARE NOT PERFORMED BECAUSE THE SERVER COULD NOT BE REACHED.",
					address);
		return reachable;
	}
}
