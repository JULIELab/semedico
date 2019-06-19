package de.julielab.semedico.core.services;


import com.google.common.cache.LoadingCache;
import de.julielab.neo4j.plugins.datarepresentation.constants.NodeIDPrefixConstants;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.DatabaseConcept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.services.ConceptNeo4jService.ConceptCacheLoader;
import de.julielab.semedico.core.services.interfaces.ICacheService;
import de.julielab.semedico.core.services.interfaces.ICacheService.Region;
import org.apache.tapestry5.ioc.Registry;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class CacheServiceTest {
    private static Registry registry;

    @BeforeClass(groups = {"neo4jtests"})
    public static void setup() {
        registry = TestUtils.createTestRegistry();
    }

    @AfterClass(groups = {"neo4jtests"})
    public static void shutdown() {
        registry.shutdown();
    }

    @Test(groups = {"neo4jtests"})
    public void testGetTerm() throws InterruptedException {
        final int numberRequestedTerms = 100;

        ICacheService cacheService = registry.getService(ICacheService.class);
        ConceptCacheLoader conceptCacheLoader = registry
                .getService(ConceptNeo4jService.ConceptCacheLoader.class);

        // Get the reference to the loading worker thread. We have to
        // synchronize on it because the tests will fail otherwise (tapestry
        // errors when the registry is supposed to be shut down too early; the
        // exact reasons I don't know).
        AsyncCacheLoader<String, IConcept>.LoadingWorkerReference loadingWorkerReference = conceptCacheLoader
                .getLoadingWorkerReference();
        LoadingCache<String, DatabaseConcept> cache = cacheService
                .getCache(Region.TERM);
        assertNotNull(cache, "TermCache built successfully");

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
        assertEquals(numberRequestedTerms, cache.size(), "Size of term cache");
        for (String termId : requestedTermIds) {
            DatabaseConcept term = cache.getIfPresent(termId);
            assertNotNull(term, "Cached Term");
        }
        // Just to be sure.
//		loadingWorkerReference.awaitCurrentBatchLoaded();
        loadingWorkerReference.interruptAndJoin();
    }
}
