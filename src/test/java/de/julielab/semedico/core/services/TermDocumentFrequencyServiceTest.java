package de.julielab.semedico.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.tapestry5.ioc.Registry;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.services.interfaces.ITermDocumentFrequencyService;

@Deprecated
@Ignore
public class TermDocumentFrequencyServiceTest {
	private static Registry registry;
	private static ITermDocumentFrequencyService termDocFreqService;

	@BeforeClass
	public static void setup() {
		org.junit.Assume.assumeTrue(TestUtils.isAddressReachable(TestUtils.neo4jTestEndpoint));
		org.junit.Assume.assumeTrue(TestUtils.isAddressReachable(TestUtils.searchServerUrl));
	}

	@Before
	public void before() {
		registry = TestUtils.createTestRegistry();
		termDocFreqService = registry.getService(ITermDocumentFrequencyService.class);
	}

	@After
	public void after() {
		registry.shutdown();
	}

	@Test
	public void testUpdate() throws InterruptedException {
		assertFalse(termDocFreqService.isInitialized());
		termDocFreqService.updateTermDocumentFrequencies();
		assertTrue(termDocFreqService.isInitialized());
		assertEquals(1, termDocFreqService.getDocumentFrequencyForTerm(NodeIDPrefixConstants.TERM + 1063));
		assertEquals(170, termDocFreqService.getDocumentFrequencyForTerm(NodeIDPrefixConstants.TERM + 103));
		assertEquals(589, termDocFreqService.getDocumentFrequencyForTerm(NodeIDPrefixConstants.AGGREGATE_TERM + 0));
	}

	@Test
	public void testConcurrency() throws InterruptedException {
		// In this test we just start multiple threads to provoke an error if something goes wrong. Thus, not really a
		// deterministic test. If it goes wrong at least once, the synchronization is flawed.
		assertFalse(termDocFreqService.isInitialized());
		Thread t = new Thread(termDocFreqService);
		Thread t1 = new Thread(termDocFreqService);
		Thread t2 = new Thread(termDocFreqService);
		Thread t3 = new Thread(termDocFreqService);
		Thread t4 = new Thread(termDocFreqService);
		t.start();
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		termDocFreqService.awaitInitialization();
		assertTrue(termDocFreqService.isInitialized());
		assertEquals(1, termDocFreqService.getDocumentFrequencyForTerm(NodeIDPrefixConstants.TERM + 1063));
		assertEquals(170, termDocFreqService.getDocumentFrequencyForTerm(NodeIDPrefixConstants.TERM + 103));
		assertEquals(589, termDocFreqService.getDocumentFrequencyForTerm(NodeIDPrefixConstants.AGGREGATE_TERM + 0));
	}

	@Test
	public void testGetValueWithoutPrioInitialization() {
		assertFalse(termDocFreqService.isInitialized());
		assertEquals(1, termDocFreqService.getDocumentFrequencyForTerm(NodeIDPrefixConstants.TERM + 1063));
		assertTrue(termDocFreqService.isInitialized());
		assertEquals(170, termDocFreqService.getDocumentFrequencyForTerm(NodeIDPrefixConstants.TERM + 103));
		assertEquals(589, termDocFreqService.getDocumentFrequencyForTerm(NodeIDPrefixConstants.AGGREGATE_TERM + 0));
	}
}
