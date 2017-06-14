package de.julielab.semedico.core.search;

import static org.junit.Assert.assertTrue;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.julielab.elastic.query.services.ISearchClientProvider;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

/**
 * This class contains tests checking if the basic communication with the ElasticSearch server is working at all through Semedico's facilities.
 * @author faessler
 *
 */
public class BasicElasticSearchIT {
	private static Registry registry;
	
	@BeforeClass
	public static void setup() {
		registry = TestUtils.createTestRegistry();
	}
	
	@AfterClass
	public static void shutDown() {
		registry.shutdown();
	}
	
	@Test
	public void testClient() {
		// a very basic test: check if we can find some string in a document title
		Client client = registry.getService(ISearchClientProvider.class).getSearchClient().getClient();
		SymbolSource symbolSource = registry.getService(SymbolSource.class);
		String documentsIndexName = symbolSource.valueForSymbol(SemedicoSymbolConstants.DOCUMENTS_INDEX_NAME);
		SearchResponse response = client.prepareSearch(documentsIndexName).setQuery(QueryBuilders.matchQuery(IIndexInformationService.TITLE, "HMGA2")).execute().actionGet();
		assertTrue(response.getHits().totalHits() > 0);
	}
}
