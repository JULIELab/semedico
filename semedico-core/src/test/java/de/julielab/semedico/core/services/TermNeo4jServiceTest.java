package de.julielab.semedico.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.neo4j.plugins.datarepresentation.PushTermsToSetCommand;
import de.julielab.semedico.core.TermLabels;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.interfaces.IFacetTerm;
import de.julielab.semedico.core.concepts.interfaces.IFacetTermRelation;
import de.julielab.semedico.core.concepts.interfaces.IFacetTermRelation.Type;
import de.julielab.semedico.core.concepts.interfaces.IPath;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetLabels;
import de.julielab.semedico.core.facetterms.FacetTerm;
import de.julielab.semedico.core.services.TermNeo4jService.TermCacheLoader;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseService;
import de.julielab.semedico.core.services.interfaces.ITermService;

public class TermNeo4jServiceTest {

	private static Registry registry;

	private static AsyncCacheLoader<String, IConcept>.LoadingWorkerReference loadingWorkerReference;

	private static ITermService termService;

	@BeforeClass
	public static void setup() {
		registry = TestUtils.createTestRegistry();

		// We get the service to get the loading worker reference to be able to
		// join on the loading worker; otherwise, we will run into concurrency
		// issues because when tests finish, the whole JVM is terminate even
		// when other threads are running.
		TermCacheLoader termCacheLoader = registry.getService(TermCacheLoader.class);
		loadingWorkerReference = termCacheLoader.getLoadingWorkerReference();
		termService = registry.getService(ITermService.class);

		org.junit.Assume.assumeTrue(TestUtils.isAddressReachable(TestUtils.neo4jTestEndpoint));

	}
	
	@AfterClass
	public static void shutdown() {
		registry.shutdown();
	}

	@Test
	public void testGetTerms() {
		// We only fetch a small fraction of terms for this test to save time.
		int amount = 100;
		Iterator<IConcept> terms = termService.getTerms(amount);
		int i = 0;
		while (terms.hasNext()) {
			IFacetTerm next = (IFacetTerm) terms.next();
			assertNotNull(next);
			assertFalse(StringUtils.isBlank(next.getId()));
			assertTrue(next.getSourceIds().size() > 0);
			assertFalse(StringUtils.isBlank(next.getPreferredName()));
			assertTrue(next.getFacets() != null && next.getFacets().size() > 0);
			i++;
		}
		assertEquals("Number of terms", Integer.valueOf(amount), Integer.valueOf(i));

	}
	
	@Test
	public void testGetAggregateTerm() {
		IFacetTerm term = (IFacetTerm) termService.getTerm(NodeIDPrefixConstants.AGGREGATE_TERM + 1340);
		assertNotNull(term);
		assertEquals("cell", term.getPreferredName());
		Set<String> facetIds = new HashSet<>();
		for (Facet facet : term.getFacets())
			facetIds.add(facet.getId());
		assertEquals(2, facetIds.size());
		assertEquals(1, term.getDescriptions().size());
	}


	@Test
	public void testGetTermsInSuggestionQueue() {
		ITermDatabaseService termDBService = registry.getService(ITermDatabaseService.class);

		// Empty the queue before starting the actual test.
		Iterator<IConcept> emptyQueueIt = termService.getTermsInSuggestionQueue();
		while (emptyQueueIt.hasNext()) {
			@SuppressWarnings("unused")
			IConcept next = emptyQueueIt.next();
			// just use up the iterator to empty the queue for the case that another test let terms in the queue
		}

		PushTermsToSetCommand cmd = new PushTermsToSetCommand(
				TermLabels.GeneralLabel.PENDING_FOR_SUGGESTIONS.toString());
		cmd.eligibleTermDefinition = cmd.new TermSelectionDefinition();
		cmd.eligibleTermDefinition.facetLabel = FacetLabels.General.USE_FOR_SUGGESTIONS.name();
		// For the test, we restrict ourselves to a few terms because it would take too long, otherwise
		int amount = 100;
		termDBService.pushTermsToSet(cmd, amount);

		Iterator<IConcept> termIt = termService.getTermsInSuggestionQueue();
		int termCount = 0;
		while (termIt.hasNext()) {
			IConcept term = termIt.next();
			assertNotNull(term);
			termCount++;
		}
		// We just know that in our test-database there are 1795 terms in
		// suggestion-facets.
		assertEquals(Integer.valueOf(amount), Integer.valueOf(termCount));
	}

	@Test
	public void testGetPathFromRoot() {
		// Only get a few terms to save time
		Iterator<IConcept> terms = termService.getTerms(100);
		while (terms.hasNext()) {
			IConcept term = terms.next();
			IPath pathFromRoot = termService.getShortestPathFromAnyRoot(term);
			assertNotNull(pathFromRoot);
			// We expect at least one node on a path, i.e. the root node itself.
			assertTrue(pathFromRoot.length() > 0);
		}
		loadingWorkerReference.interruptAndJoin();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadChildrenOfTerm() throws Exception {
		Field outgoingRelsField = Concept.class.getDeclaredField("outgoingRelationships");
		outgoingRelsField.setAccessible(true);

		// Facet term "Gene Rearrangement, B-Lymphocyte"
		IFacetTerm facetTerm = new FacetTerm(NodeIDPrefixConstants.TERM + 176);
		Map<Type, List<IFacetTermRelation>> outgoingRelationships = (Map<IFacetTermRelation.Type, List<IFacetTermRelation>>) outgoingRelsField
				.get(facetTerm);
		assertNull(outgoingRelationships.get(IFacetTermRelation.Type.IS_BROADER_THAN));
		termService.loadChildrenOfTerm((Concept) facetTerm);
		outgoingRelationships = (Map<IFacetTermRelation.Type, List<IFacetTermRelation>>) outgoingRelsField
				.get(facetTerm);
		assertEquals(Integer.valueOf(2),
				Integer.valueOf(outgoingRelationships.get(IFacetTermRelation.Type.IS_BROADER_THAN).size()));
		// WARNING here, the facet ID for the relation has to be adapted when the facets in the test DB change
		assertEquals(
				Integer.valueOf(2),
				Integer.valueOf(outgoingRelationships.get(
						IFacetTermRelation.Type.IS_BROADER_THAN.name() + "_" + NodeIDPrefixConstants.FACET + 2).size()));
	}

	/**
	 * This test is completely analogous to {@link Neo4jServiceTest#testGetShortestPathInFacet()} 
	 */
	@Test
	public void testGetShortestPathInFacet() {
		IFacetService facetService = registry.getService(IFacetService.class);
		// term Immunity, Natural, originalId D007113
		IConcept term = termService.getTerm(NodeIDPrefixConstants.TERM + 81);
		// facet Cellular Processes
		Facet facetCP = facetService.getFacetById(NodeIDPrefixConstants.FACET + 1);
		// facet Immune Processes
		Facet facetIP = facetService.getFacetById(NodeIDPrefixConstants.FACET + 2);
		// negativ example: facet Investigative Techniques, the term isn't there at all
		Facet facetIT = facetService.getFacetById(NodeIDPrefixConstants.FACET + 4);
		IPath path;
		path = termService.getShortestRootPathInFacet(term, facetCP);
		assertEquals(termService.getTerm(NodeIDPrefixConstants.TERM + 62), path.getNodeAt(0));
		assertEquals(term, path.getNodeAt(1));

		path = termService.getShortestRootPathInFacet(term, facetIP);
		assertEquals(termService.getTerm(NodeIDPrefixConstants.TERM + 162), path.getNodeAt(0));
		assertEquals(term, path.getNodeAt(1));

		// There should be the empty path because the requested term is not included in the requested facet.
		path = termService.getShortestRootPathInFacet(term, facetIT);
		assertEquals(0, path.length());
	}

	
}
