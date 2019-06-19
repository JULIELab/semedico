package de.julielab.semedico.core.services;

import com.google.common.collect.Lists;
import de.julielab.neo4j.plugins.datarepresentation.constants.ConceptConstants;
import de.julielab.neo4j.plugins.datarepresentation.constants.NodeIDPrefixConstants;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.ConceptCreator;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.concepts.SyncDbConcept;
import de.julielab.semedico.core.services.ConceptNeo4jService.ConceptCacheLoader;
import de.julielab.semedico.core.services.interfaces.IConceptCreator;
import de.julielab.semedico.core.services.interfaces.IConceptDatabaseService;
import org.easymock.EasyMock;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

@Test(groups="integration")
public class FacetConceptCacheLoaderTest {

	private final static Logger log = LoggerFactory.getLogger(FacetConceptCacheLoaderTest.class);


	@Test
	public void testLoad() {
        String conceptId = NodeIDPrefixConstants.TERM + 1;
        IConceptDatabaseService neo4jService = EasyMock.createNiceMock(IConceptDatabaseService.class);
        EasyMock.replay(neo4jService);
        IConceptCreator conceptCreator = EasyMock.createMock(IConceptCreator.class);

        // Create the test concept
        final SyncDbConcept testConcept = new SyncDbConcept(conceptId, null);
        final Facet testFacet = new Facet("45");
        testConcept.setFacets(Arrays.asList(testFacet));

        testConcept.setPreferredName("TEST CONCEPT");
        EasyMock.expect(conceptCreator.createDatabaseProxyConcept(conceptId, SyncDbConcept.class)).andReturn(testConcept);
        EasyMock.replay(conceptCreator);
        ConceptCacheLoader cacheLoader =  new ConceptCacheLoader(
                LoggerFactory.getLogger(ConceptCacheLoader.class), neo4jService,
                conceptCreator);


		// Get the proxy term with no values except its ID.
        IConcept concept = cacheLoader.load( NodeIDPrefixConstants.TERM + 1);

		// Now we should have the name.
		assertNotNull(concept.getPreferredName());

		assertNotNull(concept.getFacets());
		assertTrue(concept.getFacets().size() > 0);
		// Just to provoke an exception if the internal facet set is null and thus, loading has failed.
		assertFalse(concept.isContainedInFacet(new Facet("42")));
	}


    @Test
	public void testLoadNonExistent() {
		// Here, we assert that an exception is thrown when we ask for a term
		// that is not present in the database.

        ConceptCacheLoader cacheLoader = getConceptCacheLoader();


        AsyncCacheLoader<String, IConcept>.LoadingWorkerReference loadingWorkerReference =cacheLoader.getLoadingWorkerReference();
        // We use the list as a connection into the UncaughtExceptionHandler.
		final List<Boolean> errorIndicatorList = Lists.newArrayList(false);
		// The handler will catch the exception in the loading worker thread.
		UncaughtExceptionHandler eh = new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				errorIndicatorList.set(0, true);
			}

		};
		cacheLoader.load("notexistingid");
		// Quickly set the UncaughtExceptionHandler - we have a Moment to do
		// this before the Exception will be thrown because the worker waits a
		// few milliseconds before beginning.
		loadingWorkerReference.loadingWorker.setUncaughtExceptionHandler(eh);
		// We must wait until the worker finishes because we won't see the
		// exception before that.
		loadingWorkerReference.awaitCurrentBatchLoaded();
		loadingWorkerReference.joinWorkerThread();
		assertTrue(errorIndicatorList.get(0));
	}

    @NotNull
    private ConceptNeo4jService.ConceptCacheLoader getConceptCacheLoader() {
        IConceptDatabaseService neo4jService = Neo4jServiceTest.neo4jService;
        final FacetNeo4jService facetService = new FacetNeo4jService(LoggerFactory.getLogger(FacetNeo4jService.class), true, true, neo4jService);
        IConceptCreator conceptCreator = new ConceptCreator(facetService);

        return new ConceptCacheLoader(
                LoggerFactory.getLogger(ConceptCacheLoader.class), neo4jService,
                conceptCreator);
    }

    @Test
	public void testLoadInLoop() throws Exception {
		AsyncCacheLoader.BATCH_CAPACITY = 300;

        final ConceptCacheLoader cacheLoader = getConceptCacheLoader();
        final AsyncCacheLoader<String, IConcept>.LoadingWorkerReference loadingWorkerReference = cacheLoader.getLoadingWorkerReference();

        Random random = new Random(1);
		for (int i = 0; i < 1500; i++) {
			int nextInt = random.nextInt(50);
			String termId = NodeIDPrefixConstants.TERM + nextInt;
			IConcept term = cacheLoader.load(termId);
			assertNotNull(term.getId());
		}
		log.debug("Waiting for last term batch to be loaded, i.e. the loading worker to finish, then loading another batch.");
		loadingWorkerReference.awaitCurrentBatchLoaded();
		for (int i = 0; i < 1500; i++) {
			int nextInt = random.nextInt(50);
			String termId = NodeIDPrefixConstants.TERM + nextInt;
			IConcept term = cacheLoader.load(termId);
			assertNotNull(term.getId());
		}
		// We need this join to for the JUnit test engine to wait for our thread
		// to finish. Otherwise, the test JVM would be terminated now,
		// regardless of any threads still running.
		log.debug("Waiting for the loading worker to finish the queue.");
		loadingWorkerReference.awaitCurrentBatchLoaded();
		loadingWorkerReference.interruptAndJoin();
		log.debug("Finished.");
	}

	@Test
	public void testDontLoadDuplicates() {

        final ConceptCacheLoader cacheLoader = getConceptCacheLoader();
        final AsyncCacheLoader<String, IConcept>.LoadingWorkerReference loadingWorkerReference = cacheLoader.getLoadingWorkerReference();

        IConcept loadingProxy = null;
		for (int i = 0; i < 50; i++) {
			// We constantly request the 100 term and hope that it is still
			// loaded only once; not because of caching - which doesn't happen
			// on this level - but because the loader itself avoid duplicates
			// before loading.
			String conceptId = NodeIDPrefixConstants.TERM + 100;
			IConcept concept = cacheLoader.load(conceptId);
            assertThat(concept.getId()).isNotNull();
			if (loadingProxy == null)
				loadingProxy = concept;
			else
				// It should be the same proxy being returned (for each unique
				// batch anyway; running this on a computer where not all terms
				// can be enqueued while the async loader is still waiting would
				// break the test. But more probably, this computer could not
				// run Eclipse ;-)
				assertEquals(System.identityHashCode(loadingProxy), System.identityHashCode(concept));
		}
		loadingWorkerReference.awaitCurrentBatchLoaded();
	}
}
