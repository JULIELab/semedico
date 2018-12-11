package de.julielab.semedico.core.services;

import de.julielab.elastic.query.ElasticQuerySymbolConstants;
import de.julielab.java.utilities.prerequisites.PrerequisiteChecker;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.search.query.QueryAnalysis;
import de.julielab.semedico.core.search.query.translation.ConceptTranslation;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.ImportModule;

import static de.julielab.semedico.core.services.SemedicoSymbolConstants.*;

@ImportModule(SemedicoCoreModule.class)
public class SemedicoCoreTestModule {

    public static String neo4jTestEndpoint = "http://localhost:7474/";
    public static String neo4jTestUser = "neo4j";
    public static String neo4jTestPassword = "julielab";
    public static String esCluster = "semedicoDev";
    public static String esHost = "localhost";
    public static String esPort = "9300";


    public void contributeApplicationDefaults(MappedConfiguration<String, String> configuration) {
        System.setProperty(PrerequisiteChecker.PREREQUISITE_CHECKS_ENABLED, "true");

        configuration.add(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT, neo4jTestEndpoint);
        configuration.add(SemedicoSymbolConstants.NEO4J_USERNAME, neo4jTestUser);
        configuration.add(SemedicoSymbolConstants.NEO4J_PASSWORD, neo4jTestPassword);
        configuration.add(SemedicoSymbolConstants.TERM_CACHE_SIZE, "500");
        configuration.add(SemedicoSymbolConstants.RELATION_CACHE_SIZE, "500");
        configuration.add(SemedicoSymbolConstants.ROOT_PATH_CACHE_SIZE, "500");
        configuration.add(ElasticQuerySymbolConstants.ES_HOST, esHost);
        configuration.add(ElasticQuerySymbolConstants.ES_PORT, esPort);
        // Deactivate the node client to avoid the long startup times in unit
        // tests.
        configuration.add(ElasticQuerySymbolConstants.ES_CLUSTER_NAME, esCluster);
        configuration.add(SemedicoSymbolConstants.BIOMED_PUBLICATIONS_INDEX_NAME, "semedico_it");
        configuration.add(SemedicoSymbolConstants.STOP_WORDS_FILE, "src/test/resources/test_stopwords.txt");
        configuration.add(SemedicoSymbolConstants.TERM_DICT_FILE, "src/test/resources/query-test.dic");
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
        configuration.add(SUGGESTIONS_INDEX_NAME, "suggestions_it");

        configuration.add(FACETS_LOAD_AT_START, "true");
        configuration.add(LABEL_HIERARCHY_INIT_CACHE_SIZE, "5");
        configuration.add(MAX_DISPLAYED_FACETS, "20");
        configuration.add(DISPLAY_TERMS_MIN_HITS, "0");
        configuration.add(DISPLAY_MESSAGE_WHEN_NO_CHILDREN_HIT, "false");
        configuration.add(DISPLAY_MESSAGE_WHEN_NO_FACET_ROOTS_HIT, "false");
        configuration.add(DISPLAY_FACET_COUNT, "true");
        configuration.add(FACET_ROOT_CACHE_SIZE, "10");

        configuration.add(SemedicoSymbolConstants.QUERY_ANALYSIS, QueryAnalysis.CONCEPTS.name());
        configuration.add(SemedicoSymbolConstants.CONCEPT_TRANSLATION, ConceptTranslation.ID.name());
        configuration.add(SemedicoSymbolConstants.PARSING_DEFAULT_OPERATOR, Node.NodeType.AND.name());
    }
}
