package de.julielab.semedico.core.services;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.interfaces.IPath;
import de.julielab.semedico.core.facetterms.FacetTerm;
import de.julielab.semedico.core.services.TermNeo4jService.ShortestRootPathCacheLoader;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseService;
import de.julielab.semedico.core.services.interfaces.ITermService;

public class RootPathCacheLoaderTest {
	private static ShortestRootPathCacheLoader cacheLoader;
	private static String id1;
	private static String id2;
	private static String id3;

	@BeforeClass
	public static void setup() {
		org.junit.Assume.assumeTrue(TestUtils
				.isAddressReachable(TestUtils.neo4jTestEndpoint));
		
		ITermDatabaseService neo4jService = Neo4jServiceTest.createNeo4jService();
		ITermService termService = prepareTermService();
		cacheLoader = new TermNeo4jService.ShortestRootPathCacheLoader(
				LoggerFactory.getLogger(RootPathCacheLoaderTest.class),
				neo4jService, termService);

	}

	private static ITermService prepareTermService() {
		ITermService termService = EasyMock.createMock(ITermService.class);
		String tid = NodeIDPrefixConstants.TERM;
		id1 = tid + 176;
		id2 = tid + 177;
		id3 = tid + 188;
		EasyMock.expect(termService.getTerm(id1)).andReturn(new FacetTerm(id1, termService));
		EasyMock.expect(termService.getTerm(id2)).andReturn(new FacetTerm(id2, termService));
		EasyMock.expect(termService.getTerm(id3)).andReturn(new FacetTerm(id3, termService));
		EasyMock.replay(termService);
		return termService;
	}

	@Test
	public void testLoad() {
		// Term with preferred name "Immunoglobulin Class Switching"
		IPath path = cacheLoader.load(NodeIDPrefixConstants.TERM + "188");
		assertEquals("Path length", 3, path.length());
		assertEquals(id1, path.getNodeAt(0).getId());
		assertEquals(id2, path.getNodeAt(1).getId());
		assertEquals(id3, path.getNodeAt(2).getId());
	}
}
