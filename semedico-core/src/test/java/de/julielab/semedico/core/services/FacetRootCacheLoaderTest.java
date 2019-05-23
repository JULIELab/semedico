package de.julielab.semedico.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.ioc.Registry;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.services.TermNeo4jService.FacetRootCacheLoader;
import de.julielab.semedico.core.services.interfaces.ICacheService;
import de.julielab.semedico.core.services.interfaces.ICacheService.Region;
import de.julielab.semedico.core.services.interfaces.IFacetTermFactory;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseService;

public class FacetRootCacheLoaderTest {
	private static FacetRootCacheLoader cacheLoader;
	static String id1 = NodeIDPrefixConstants.TERM + 56;
	static String id2 = NodeIDPrefixConstants.TERM + 65;

	@BeforeClass
	public static void setup() {
		org.junit.Assume.assumeTrue(TestUtils.isAddressReachable(TestUtils.neo4jTestEndpoint));

		Registry registry = TestUtils.createTestRegistry();

		ITermDatabaseService neo4jService = Neo4jServiceTest.createNeo4jService();
		IFacetTermFactory termFactory = registry.getService(IFacetTermFactory.class);
		cacheLoader = new TermNeo4jService.FacetRootCacheLoader(
				LoggerFactory.getLogger(TermNeo4jService.FacetRootCacheLoader.class), neo4jService, termFactory);
		LoadingCache<String, IConcept> termCache = registry.getService(ICacheService.class).getCache(Region.TERM);
		cacheLoader.setTermCache(termCache);
	}


	@Test
	public void testLoad() throws Exception {
		// Facet "Cellular Processes"
		List<Concept> rootTerms = cacheLoader.load("fid1");
		assertNotNull(rootTerms);
		assertTrue(rootTerms.size() == 22);
		Set<String> termIds = new HashSet<>();
		for (Concept term : rootTerms)
			termIds.add(term.getId());
		assertTrue(termIds.contains(id1));
		assertTrue(termIds.contains(id2));
	}
	
	@Test
	public void testLoadAll() throws Exception{
		// Facets "Cellular Processes" and "Immune Processes"
		Map<String, List<Concept>> rootTerms = cacheLoader.loadAll(Lists.newArrayList("fid1", "fid2"));
		assertNotNull(rootTerms);
		assertTrue(rootTerms.size() == 2);
		Set<String> termIds = new HashSet<>();
		for (IConcept term : rootTerms.get("fid1"))
			termIds.add(term.getId());
		assertTrue(termIds.contains(id1));
		assertTrue(termIds.contains(id2));
		List<Concept> rootTermsFid2 = rootTerms.get("fid2");
		assertEquals(Integer.valueOf(31), Integer.valueOf(rootTermsFid2.size()));
	}
}
