package de.julielab.semedico.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import de.julielab.neo4j.plugins.datarepresentation.constants.NodeIDPrefixConstants;
import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.cache.LoadingCache;

import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.DatabaseConcept;
import de.julielab.semedico.core.services.ConceptNeo4jService.TermCacheLoader;
import de.julielab.semedico.core.services.interfaces.ICacheService;
import de.julielab.semedico.core.services.interfaces.ICacheService.Region;

public class CacheServiceTest {
	private static Registry registry;

	@BeforeClass
	public static void setup() {
		registry = TestUtils.createTestRegistry();
		
		org.junit.Assume.assumeTrue(TestUtils
				.isAddressReachable(TestUtils.neo4jTestEndpoint));
	}

	@Test
	public void testGetTerm() throws InterruptedException {
		final int numberRequestedTerms = 300;

		ICacheService cacheService = registry.getService(ICacheService.class);
		TermCacheLoader termCacheLoader = registry
				.getService(TermCacheLoader.class);

		// Get the reference to the loading worker thread. We have to
		// synchronize on it because the tests will fail otherwise (tapestry
		// errors when the registry is supposed to be shut down too early; the
		// exact reasons I don't know).
		AsyncCacheLoader<String, IConcept>.LoadingWorkerReference loadingWorkerReference = termCacheLoader
				.getLoadingWorkerReference();
		LoadingCache<String, DatabaseConcept> cache = cacheService
				.getCache(Region.TERM);
		assertNotNull("TermCache built successfully", cache);

		List<String> requestedTermIds = new ArrayList<>();
		for (int i = 0; i < numberRequestedTerms; i++) {
			String termId = NodeIDPrefixConstants.TERM + i;
			requestedTermIds.add(termId);
			cache.getUnchecked(termId);
		}
		// Synchronize of the worker thread - this is important, also for
		// following test cases because the thread might run across test cases,
		// invalidating the tests.
		loadingWorkerReference.awaitCurrentBatchLoaded();
		// Cache entries are being evicted BEFORE the cache is completely full.
		// Thus, we must check for significantly less entries than maximum
		// capacity for this test to work.
		assertEquals("Size of term cache", numberRequestedTerms, cache.size());
		for (String termId : requestedTermIds) {
			DatabaseConcept term = cache.getIfPresent(termId);
			assertNotNull("Cached Term", term);
		}
		// Just to be sure.
//		loadingWorkerReference.awaitCurrentBatchLoaded();
		loadingWorkerReference.interruptAndJoin();
	}

	@AfterClass
	public static void shutdown() {
		registry.shutdown();
	}
}
