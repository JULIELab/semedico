package de.julielab.semedico.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.facets.BioPortalFacet;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.Facet.Source;
import de.julielab.semedico.core.facets.FacetLabels;
import de.julielab.semedico.core.services.interfaces.IFacetService;

public class FacetNeo4jServiceTest {
	private static Registry registry;
	private static IFacetService facetService;
//	private static AsyncCacheLoader<String,SyncFacetTerm>.LoadingWorkerReference loadingWorkerReference;

	@BeforeClass
	public static void setup() {
		registry = TestUtils.createTestRegistry();
		facetService = registry.getService(IFacetService.class);
		
		// We get the service to get the loading worker reference to be able to
		// join on the loading worker; otherwise, we will run into concurrency
		// issues because when tests finish, the whole JVM is terminate even
		// when other threads are running.
//		FacetTermCacheLoader termCacheLoader = registry
//				.getService(FacetTermCacheLoader.class);
//		loadingWorkerReference = termCacheLoader.getLoadingWorkerReference();
		
		org.junit.Assume.assumeTrue(TestUtils
				.isAddressReachable(TestUtils.neo4jTestEndpoint));
	}
	
	@Test
	public void testGetFacets() {
		List<Facet> facets = facetService.getFacets();
		boolean testFacetFound = false;
		boolean keywordFacetFound = false;
		for (Facet facet : facets) {
			assertNotNull(facet.getName());
			assertFalse(StringUtils.isBlank(facet.getName()));
			assertNotNull(facet.getId());
			assertFalse(StringUtils.isBlank(facet.getId()));
			assertNotNull(facet.getSource());

			if (facet.getName().equals("Immune Processes")) {
				testFacetFound = true;
				assertTrue(facet.hasGeneralLabel(FacetLabels.General.USE_FOR_QUERY_DICTIONARY));
				assertTrue(facet.hasGeneralLabel(FacetLabels.General.USE_FOR_SUGGESTIONS));
				assertNotNull(facet.getSearchFieldNames());
//				assertTrue(facet.getSearchFieldNames().size() > 0);
				assertNotNull(facet.getFilterFieldNames());
				assertNotNull(facet.getFacetRoots());
				assertTrue(facet.getFacetRoots().size() > 0);
				assertTrue(facet.isHierarchic());
				assertFalse(facet.isFlat());
				assertEquals(31, facet.getNumRootsInDB());
			} else if (facet == Facet.KEYWORD_FACET) {
				keywordFacetFound = true;
				assertEquals(NodeIDPrefixConstants.FACET + -1, facet.getId());
				assertFalse(facet.hasGeneralLabel(FacetLabels.General.USE_FOR_QUERY_DICTIONARY));
				assertFalse(facet.hasGeneralLabel(FacetLabels.General.USE_FOR_SUGGESTIONS));
				assertTrue(facet.hasUniqueLabel(FacetLabels.Unique.KEYWORDS));
				Source source = facet.getSource();
				assertNotNull(source);
				assertEquals("keywords", source.getName());
				assertTrue(facet.isFlat());
				assertFalse(facet.isHierarchic());
				assertEquals(0, facet.getNumRootsInDB());
			}
		}
		assertTrue("Test facet found", testFacetFound);
		assertTrue("Keyword facet found", keywordFacetFound);
		// Neo4j facet count plus keyword facet.
		assertEquals(14, facets.size());
		
//		loadingWorkerReference.awaitCurrentBatchLoaded();
	}
	
	@Test
	public void testBioPortalFacet() {
		Facet go = facetService.getFacetByName("Gene Ontology");
		assertTrue(go instanceof BioPortalFacet);
	}
	
	
	@AfterClass
	public static void shutdown() {
		registry.shutdown();
	}
}
