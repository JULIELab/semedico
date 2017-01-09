package de.julielab.semedico.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.facetterms.Event;
import de.julielab.semedico.core.services.TermNeo4jService.EventCacheLoader;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;

public class EventCacheLoaderTest {

	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger(EventCacheLoaderTest.class);
	private static EventCacheLoader cacheLoader;
	private static Registry registry;
	private static ITermService termService;

	@BeforeClass
	public static void setup() {
		org.junit.Assume.assumeTrue(TestUtils.isAddressReachable(TestUtils.neo4jTestEndpoint));
		registry = TestUtils.createTestRegistry();
		termService = registry.getService(ITermService.class);
		IFacetService facetService = registry.getService(IFacetService.class);
		cacheLoader =
				new TermNeo4jService.EventCacheLoader(LoggerFactory.getLogger(TermNeo4jService.EventCacheLoader.class),
						termService, facetService);
	}

	@AfterClass
	public static void shutDown() {
		registry.shutdown();
	}

	@Test
	public void testLoadBinaryEvent() throws Exception {
		// ISL1-localization-Mef2c
		String binEvent =
				"jrex:" + NodeIDPrefixConstants.TERM
						+ 1839
						+ "-"
						+ NodeIDPrefixConstants.TERM
						+ 1855
						+ "-"
						+ NodeIDPrefixConstants.TERM
						+ 1854;
		Event event = cacheLoader.load(binEvent);
		assertNotNull(event);
		assertEquals(2, event.getArguments().size());
		assertEquals(NodeIDPrefixConstants.TERM + 1855, event.getArguments().get(0).getId());
		assertEquals(NodeIDPrefixConstants.TERM + 1854, event.getArguments().get(1).getId());
		assertEquals(NodeIDPrefixConstants.TERM + 1839, event.getEventTerm().getId());
	}

}
