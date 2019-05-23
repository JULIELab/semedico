package de.julielab.semedico.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.neo4j.plugins.constants.semedico.ConceptConstants;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.TermNeo4jService.TermCacheLoader;
import de.julielab.semedico.core.services.interfaces.IFacetTermFactory;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseService;

public class FacetTermCacheLoaderTest {

	private final static Logger log = LoggerFactory.getLogger(FacetTermCacheLoaderTest.class);
	private static TermCacheLoader cacheLoader;
	private static AsyncCacheLoader<String, IConcept>.LoadingWorkerReference loadingWorkerReference;
	private static Registry registry;

	@BeforeClass
	public static void setup() {
		org.junit.Assume.assumeTrue(TestUtils.isAddressReachable(TestUtils.neo4jTestEndpoint));
		registry = TestUtils.createTestRegistry();
		ITermDatabaseService neo4jService = Neo4jServiceTest.createNeo4jService();
		IFacetTermFactory termFactory = registry.getService(IFacetTermFactory.class);
		cacheLoader = new TermNeo4jService.TermCacheLoader(
				LoggerFactory.getLogger(TermNeo4jService.TermCacheLoader.class), neo4jService,
				termFactory);
		loadingWorkerReference = cacheLoader.getLoadingWorkerReference();
	}

	@AfterClass
	public static void shutDown() {
		loadingWorkerReference.interruptAndJoin();
		registry.shutdown();
	}
	
	@Test
	public void testLoad() throws Exception {
		// We will load a term an immediately check on its preferredName value.
		// Since the loading worker thread is waiting for a moment, the check
		// should give that the value is not there yet.
		// Then, we wait for the worker thread to finish and check again. Now
		// the value should be present.
		Field preferredNameField = Concept.class.getDeclaredField(ConceptConstants.PROP_PREF_NAME);
		preferredNameField.setAccessible(true);

		// Get the proxy term with no values except its ID.
		IConcept term = cacheLoader.load(NodeIDPrefixConstants.TERM + 1);

		// There should be not preferred name yet.
		assertNull(preferredNameField.get(term));

		loadingWorkerReference.awaitCurrentBatchLoaded();
		loadingWorkerReference.interruptAndJoin();
		// Now we should have the name.
		assertNotNull(preferredNameField.get(term));

		assertNotNull(term.getFacets());
		assertTrue(term.getFacets().size() > 0);
		// Just to provoke an exception if the internal facet set is null and thus, loading has failed.
		assertFalse(term.isContainedInFacet(new Facet("42")));
	}

	@Test
	public void testLoadNonExistent() {
		// Here, we assert that an exception is thrown when we ask for a term
		// that is not present in the database.

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

	@Test
	public void testLoadInLoop() throws Exception {
		AsyncCacheLoader.BATCH_CAPACITY = 300;

		Random random = new Random(1);
		for (int i = 0; i < 1500; i++) {
			int nextInt = random.nextInt(1500);
			String termId = NodeIDPrefixConstants.TERM + nextInt;
			IConcept term = cacheLoader.load(termId);
			assertNotNull(term);
		}
		log.debug("Waiting for last term batch to be loaded, i.e. the loading worker to finish, then loading another batch.");
		loadingWorkerReference.awaitCurrentBatchLoaded();
		for (int i = 0; i < 1500; i++) {
			int nextInt = random.nextInt(1500);
			String termId = NodeIDPrefixConstants.TERM + nextInt;
			IConcept term = cacheLoader.load(termId);
			assertNotNull(term);
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

		IConcept loadingProxy = null;
		for (int i = 0; i < 500; i++) {
			// We constantly request the 500th term and hope that it is still
			// loaded only once; not because of caching - which doesn't happen
			// on this level - but because the loader itself avoid duplicates
			// before loading.
			String termId = NodeIDPrefixConstants.TERM + 500;
			IConcept term = cacheLoader.load(termId);
			assertNotNull(term);
			if (loadingProxy == null)
				loadingProxy = term;
			else
				// It should be the same proxy being returned (for each unique
				// batch anyway; running this on a computer where not all terms
				// can be enqueued while the async loader is still waiting would
				// break the test. But more probably, this computer could not
				// run Eclipse ;-)
				assertEquals(System.identityHashCode(loadingProxy), System.identityHashCode(term));
		}
		loadingWorkerReference.awaitCurrentBatchLoaded();
	}
}
