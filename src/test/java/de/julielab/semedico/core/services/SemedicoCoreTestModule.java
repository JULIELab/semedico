package de.julielab.semedico.core.services;

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
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.FACETS_LOAD_AT_START;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.FACET_ROOT_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.LABEL_HIERARCHY_INIT_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.MAX_DISPLAYED_FACETS;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SUGGESTIONS_ACTIVATED;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SUGGESTIONS_FILTER_INDEX_TERMS;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SUGGESTIONS_INDEX_NAME;

import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.ImportModule;

import de.julielab.elastic.query.ElasticQuerySymbolConstants;

@ImportModule(SemedicoCoreProductionModule.class)
public class SemedicoCoreTestModule {
	
	public static final String neo4jTestEndpoint = "http://localhost:7474/";
	public static final String neo4jTestUser = "neo4j";
	public static final String neo4jTestPassword = "julielab";
	public static final String searchServerUrl = "http://localhost:9200/";
	
	public void contributeTermDictionaryChunker(Configuration<DictionaryEntry> configuration) {	
		configuration.add(new DictionaryEntry("water", "tid0"));
	}
	
	public void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
	{
		configuration.add(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT,
				neo4jTestEndpoint);
		configuration.add(SemedicoSymbolConstants.NEO4J_USERNAME, neo4jTestUser);
		configuration.add(SemedicoSymbolConstants.NEO4J_PASSWORD, neo4jTestPassword);
		configuration.add(SemedicoSymbolConstants.TERM_CACHE_SIZE, "500");
		configuration.add(SemedicoSymbolConstants.RELATION_CACHE_SIZE, "500");
		configuration.add(SemedicoSymbolConstants.ROOT_PATH_CACHE_SIZE, "500");
		configuration.add(ElasticQuerySymbolConstants.ES_HOST, "localhost");
		configuration.add(ElasticQuerySymbolConstants.ES_PORT, "9300");
		// Deactivate the node client to avoid the long startup times in unit
		// tests.
		configuration.add(ElasticQuerySymbolConstants.ES_CLUSTER_NAME, "semedicoDev");
		configuration.add(SemedicoSymbolConstants.STOP_WORDS_FILE,
				"src/test/resources/test_stopwords.txt");
		configuration.add(SemedicoSymbolConstants.TERM_DICT_FILE,
				"src/test/resources/query-test.dic");
		configuration.add(SemedicoSymbolConstants.GET_HOLLOW_FACETS, "true");
		
		
		configuration.add(DATABASE_NAME, "semedico_stag_poc");
		configuration.add(DATABASE_SERVER, "darwin");
		configuration.add(DATABASE_USER, "postgres");
		configuration.add(DATABASE_PASSWORD, "$postgr3s$$");
		configuration.add(DATABASE_PORT, "5432");
		configuration.add(DATABASE_MAX_CONN, "4");
		configuration.add(DATABASE_INIT_CONN, "1");

		configuration.add(SUGGESTIONS_ACTIVATED, "true");
		configuration.add(SUGGESTIONS_FILTER_INDEX_TERMS, "false");
		configuration.add(SUGGESTIONS_INDEX_NAME, "suggestions_completion");

		configuration.add(FACETS_LOAD_AT_START, "true");
		configuration.add(LABEL_HIERARCHY_INIT_CACHE_SIZE, "5");
		configuration.add(MAX_DISPLAYED_FACETS, "20");
		configuration.add(DISPLAY_TERMS_MIN_HITS, "0");
		configuration.add(DISPLAY_MESSAGE_WHEN_NO_CHILDREN_HIT, "false");
		configuration.add(DISPLAY_MESSAGE_WHEN_NO_FACET_ROOTS_HIT, "false");
		configuration.add(DISPLAY_FACET_COUNT, "true");
		configuration.add(FACET_ROOT_CACHE_SIZE, "10");
		configuration.add("semedico.core.search.maxFacettedDocuments", "300000");
	}
}
