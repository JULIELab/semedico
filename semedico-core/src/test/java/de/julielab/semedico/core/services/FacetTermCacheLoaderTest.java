package de.julielab.semedico.core.services;

import com.google.common.collect.Lists;
import de.julielab.neo4j.plugins.datarepresentation.constants.ConceptConstants;
import de.julielab.neo4j.plugins.datarepresentation.constants.NodeIDPrefixConstants;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.concepts.SyncDbConcept;
import de.julielab.semedico.core.services.ConceptNeo4jService.TermCacheLoader;
import de.julielab.semedico.core.services.interfaces.IConceptCreator;
import de.julielab.semedico.core.services.interfaces.IConceptDatabaseService;
import org.easymock.EasyMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

public class FacetTermCacheLoaderTest {

	private final static Logger log = LoggerFactory.getLogger(FacetTermCacheLoaderTest.class);
	private static TermCacheLoader cacheLoader;
	private static AsyncCacheLoader<String, IConcept>.LoadingWorkerReference loadingWorkerReference;

	@AfterClass
	public static void shutDown() {
		loadingWorkerReference.interruptAndJoin();
	}
	
	@Test
	public void testLoad() throws Exception {
        String conceptId = NodeIDPrefixConstants.TERM + 1;

        IConceptDatabaseService neo4jService = EasyMock.createMock(IConceptDatabaseService.class);
        //EasyMock.expect(neo4jService.getConcepts(Arrays.asList(conceptId))).andReturn()
        EasyMock.replay(neo4jService);
        IConceptCreator termFactory = EasyMock.createMock(IConceptCreator.class);
        EasyMock.expect(termFactory.createDatabaseProxyConcept(conceptId, SyncDbConcept.class)).andReturn(new SyncDbConcept(conceptId, null));
        EasyMock.replay(termFactory);
        cacheLoader = new ConceptNeo4jService.TermCacheLoader(
                LoggerFactory.getLogger(ConceptNeo4jService.TermCacheLoader.class), neo4jService,
                termFactory);
        loadingWorkerReference = cacheLoader.getLoadingWorkerReference();


		// We will load a term an immediately check on its preferredName value.
		// Since the loading worker thread is waiting for a moment, the check
		// should give that the value is not there yet.
		// Then, we wait for the worker thread to finish and check again. Now
		// the value should be present.
		Field preferredNameField = Concept.class.getDeclaredField(ConceptConstants.PROP_PREF_NAME);
		preferredNameField.setAccessible(true);

		// Get the proxy term with no values except its ID.
        IConcept term = cacheLoader.load(conceptId);

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
			assertNotNull(term.getPreferredName());
		}
		log.debug("Waiting for last term batch to be loaded, i.e. the loading worker to finish, then loading another batch.");
		loadingWorkerReference.awaitCurrentBatchLoaded();
		for (int i = 0; i < 1500; i++) {
			int nextInt = random.nextInt(1500);
			String termId = NodeIDPrefixConstants.TERM + nextInt;
			IConcept term = cacheLoader.load(termId);
			assertNotNull(term.getPreferredName());
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
            assertThat(term.getPreferredName()).isNotNull();
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
