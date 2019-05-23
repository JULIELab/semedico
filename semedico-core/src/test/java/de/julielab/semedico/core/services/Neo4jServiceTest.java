package de.julielab.semedico.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.julielab.neo4j.plugins.ConceptManager;
import de.julielab.neo4j.plugins.constants.semedico.FacetConstants;
import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.neo4j.plugins.constants.semedico.ConceptConstants;
import de.julielab.neo4j.plugins.datarepresentation.PushTermsToSetCommand;
import de.julielab.semedico.core.TermLabels;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.interfaces.IFacetTermRelation;
import de.julielab.semedico.core.facets.FacetLabels;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseService;

/**
 * Important! This is rather an integration test than a unit test. As such, it
 * depends on the actual contents of the Neo4j database given. The test expects
 * exact IDs for concepts that should exist in the respective test database. As
 * such, this database has to be stable up to a certain point (or the tests have
 * to be relaxed). The test database is created by the script
 * 'prepareSemedicoTestDatabase.sh' in the semedico-resources-management
 * project. Even if this script does not seem to create an up-to-date database,
 * it should just be left this way as long as the tests are of use. Otherwise, a
 * lot of tests in this class wont't work anymore.
 * 
 * @author faessler
 * 
 */
public class Neo4jServiceTest {

	private static final Logger log = LoggerFactory.getLogger(Neo4jServiceTest.class);

	private static ITermDatabaseService neo4jService;

	public static ITermDatabaseService createNeo4jService() {
		neo4jService = new Neo4jService(log, new Neo4jHttpClientService(
				LoggerFactory.getLogger(HttpClientService.class), TestUtils.neo4jTestUser,
				TestUtils.neo4jTestPassword), TestUtils.neo4jTestEndpoint);
		return neo4jService;

	}

	@BeforeClass
	public static void setup() {
		org.junit.Assume.assumeTrue(TestUtils.isAddressReachable(TestUtils.neo4jTestEndpoint));

		createNeo4jService();
	}

	@Test
	public void testGetFacetIdsWithGeneralLabel() {
		List<String> suggestionFacetIds = neo4jService
				.getFacetIdsWithGeneralLabel(FacetLabels.General.USE_FOR_SUGGESTIONS);
		assertNotNull(suggestionFacetIds);
		assertTrue(suggestionFacetIds.size() > 0);
		for (String facetId : suggestionFacetIds)
			assertTrue(facetId.startsWith(NodeIDPrefixConstants.FACET));
	}

	@Test
	public void testGetFacetRootIds() {
		JSONArray facetRoots = neo4jService.getFacetRootIDs(NodeIDPrefixConstants.FACET + 4);
		// Facet nr. 6 - "Investigative Techniques" in our test data - has 81
		// roots in the original
		// Semedico data.
		assertEquals("Number of facet roots", 81, facetRoots.length());
	}

	@Test
	public void testGetFacets() {
		JSONArray facetsByFacetGroup = neo4jService.getFacets(false);
		assertTrue(facetsByFacetGroup.length() > 0);
		JSONObject facetGroup = facetsByFacetGroup.getJSONObject(0);
		JSONArray facets = facetGroup.getJSONArray("facets");
		assertTrue(facets.length() > 0);
		// Just take any facet to check if it has values where we expect them to
		// be. That they're actually correct is a task for the Neo4jServer
		// extension 'FacetManager'.
		JSONObject facet = facets.getJSONObject(0);
		assertNotNull(facet.get(FacetConstants.PROP_ID));
		assertNotNull(facet.get(FacetConstants.PROP_NAME));
		assertNotNull(facet.get(FacetConstants.PROP_SOURCE_TYPE));
		assertNotNull(facet.get(FacetConstants.PROP_SOURCE_NAME));
		assertNotNull(facet.get(FacetConstants.PROP_POSITION));
	}

	@Test
	public void testGetPathFromRoot() {
		JSONArray pathFromRoot = neo4jService.getShortestPathFromAnyRoot("C026408",
				ConceptConstants.PROP_SRC_IDS);
		assertNotNull(pathFromRoot);
		// We should get a single path.
		assertEquals("Length of path", 3, pathFromRoot.length());

		// This term - Cell Differentiation - is a root itself - of Cellular
		// Processes.
		pathFromRoot = neo4jService.getShortestPathFromAnyRoot("D002454",
				ConceptConstants.PROP_SRC_IDS);
		assertNotNull(pathFromRoot);
		// We should get a single path.
		assertEquals("Length of path", 1, pathFromRoot.length());
	}

	@Test
	public void testGetPathsFromRoots() {
		JSONArray pathsFromRoots = neo4jService.getAllPathsFromAnyRoots("C026408",
				ConceptConstants.PROP_SRC_IDS, false);
		assertNotNull(pathsFromRoots);
		assertEquals("Length of path", 3, pathsFromRoots.getJSONArray(0).length());
	}

	@Test
	public void testGetPathsFromRootsMultipleTerms() {
		// this should return paths of the length 1, 2 and 3 - however, we don't
		// know the order because it's
		// non-determined
		JSONArray pathsFromRoots = neo4jService.getPathsFromRoots(
				Lists.newArrayList("leukocyte", "C026408"), ConceptConstants.PROP_SRC_IDS);

		assertNotNull(pathsFromRoots);
		// The data contains three columns with one path each
		assertEquals("Length of path", 2, pathsFromRoots.length());

		// We know which path lengths we should come across. So we successfully
		// remove already found path lengths. If
		// everything went OK, we found everything.
		Set<Integer> pathLengths = Sets.newHashSet(1, 3);
		for (int i = 0; i < pathsFromRoots.length(); i++) {
			JSONArray path = pathsFromRoots.getJSONArray(i);
			Integer pathlength = path.length();
			assertTrue(pathLengths.remove(pathlength));
		}
		log.debug("Not-pulled path lengths: " + pathLengths);
		assertTrue(pathLengths.isEmpty());
	}

	/**
	 * This test is completely analogous to
	 * {@link TermNeo4jServiceTest#testGetShortestPathInFacet()}
	 */
	@Test
	public void testGetShortestPathInFacet() {
		// term Immunity, Natural, originalId D007113
		String termId = NodeIDPrefixConstants.TERM + 81;
		// facet Cellular Processes
		String facetIdCP = NodeIDPrefixConstants.FACET + 1;
		// facet Immune Processes
		String facetIdIP = NodeIDPrefixConstants.FACET + 2;
		// negativ example: facet Investigative Techniques, the term isn't there
		// at all
		String facetIdIT = NodeIDPrefixConstants.FACET + 4;
		JSONArray path;
		path = neo4jService.getShortestRootPathInFacet(termId, facetIdCP);
		assertEquals(NodeIDPrefixConstants.TERM + 62, path.getString(0));
		assertEquals(termId, path.getString(1));

		path = neo4jService.getShortestRootPathInFacet(termId, facetIdIP);
		assertEquals(NodeIDPrefixConstants.TERM + 162, path.getString(0));
		assertEquals(termId, path.getString(1));

		// There should be the empty path because the requested term is not
		// included in the requested facet.
		path = neo4jService.getShortestRootPathInFacet(termId, facetIdIT);
		assertEquals(0, path.length());
	}

	@Test
	public void testGetTermsById() {
		// Just get the very first term, whichever it may be.
		String id0 = NodeIDPrefixConstants.TERM + 0;
		String id93 = NodeIDPrefixConstants.TERM + 93;
		JSONArray resultRows = neo4jService.getTerm(id0);
		assertNotNull(resultRows);
		assertEquals("Number of returned rows", 1, resultRows.length());
		JSONArray columns = resultRows.getJSONObject(0).getJSONArray(Neo4jService.ROW);
		assertEquals("Number of columns", 2, columns.length());
		JSONObject jsonTerm = columns.getJSONObject(0);
		String jsonTermId = jsonTerm.getString(ConceptConstants.PROP_ID);
		assertEquals(id0, jsonTermId);

		resultRows = neo4jService.getTerms(Lists.newArrayList(id0, id93));
		assertNotNull(resultRows);
		assertEquals("Number of returned rows", 2, resultRows.length());
		columns = resultRows.getJSONObject(0).getJSONArray(Neo4jService.ROW);
		assertEquals("Number of columns", 2, columns.length());
		jsonTerm = columns.getJSONObject(0);
		jsonTermId = jsonTerm.getString(ConceptConstants.PROP_ID);
		assertTrue(id0.equals(jsonTermId) || id93.equals(jsonTermId));
		columns = resultRows.getJSONObject(1).getJSONArray(Neo4jService.ROW);
		assertEquals("Number of columns", 2, columns.length());
		jsonTerm = columns.getJSONObject(0);
		jsonTermId = jsonTerm.getString(ConceptConstants.PROP_ID);
		assertTrue(id0.equals(jsonTermId) || id93.equals(jsonTermId));
	}

	@Test
	public void testGetTerms() {
		JSONArray terms = neo4jService.getTerms(-1);
		int i;
		for (i = 0; i < terms.length(); i++) {
			// from the array of row objects, get the ith row and from that get
			// the columns, of which there should be
			// one holding the term.
			JSONObject term = terms.getJSONObject(i).getJSONArray(Neo4jService.ROW)
					.getJSONObject(0);
			// termId
			assertFalse(StringUtils.isBlank(term.getString(ConceptConstants.PROP_ID)));
			// source ids
			assertTrue(term.getJSONArray(ConceptConstants.PROP_SRC_IDS).length() > 0);
			// preferred name
			assertFalse(StringUtils.isBlank(term.getString(ConceptConstants.PROP_ID)));
		}
		assertTrue("There are terms in the DB", i > 0);
	}

	@Test
	public void testGetTermChildren() {
		// Terms "Antigen Presentation" and "Immune Tolerance"
		JSONObject termChildrenRows = neo4jService.getTermChildren(
				Lists.newArrayList("tid192", "tid158"), TermLabels.GeneralLabel.TERM.name());
		// Two elements, one for each term we queried its children for.
		assertEquals(Integer.valueOf(2), Integer.valueOf(termChildrenRows.length()));
		JSONObject childrenObject;
		JSONObject reltypeMapping;
		JSONArray children;
		childrenObject = termChildrenRows.getJSONObject("tid192");
		reltypeMapping = childrenObject.getJSONObject(ConceptManager.RET_KEY_RELTYPES);
		children = childrenObject.getJSONArray(ConceptManager.RET_KEY_CHILDREN);
		assertEquals(Integer.valueOf(0), Integer.valueOf(reltypeMapping.length()));
		assertEquals(Integer.valueOf(0), Integer.valueOf(children.length()));

		childrenObject = termChildrenRows.getJSONObject("tid158");
		reltypeMapping = childrenObject.getJSONObject(ConceptManager.RET_KEY_RELTYPES);
		children = childrenObject.getJSONArray(ConceptManager.RET_KEY_CHILDREN);
		assertEquals(Integer.valueOf(5), Integer.valueOf(reltypeMapping.length()));
		assertEquals(Integer.valueOf(5), Integer.valueOf(children.length()));
	}

	@Test
	public void testGetTermPath() {
		// Terms "Gene Rearrangement, B-Lymphocyte" and
		// "Immunoglobulin Class Switching"
		String term1Id = NodeIDPrefixConstants.TERM + 176;
		String term2Id = NodeIDPrefixConstants.TERM + 188;
		JSONArray termPath = neo4jService.getTermPath(term1Id, term2Id,
				IFacetTermRelation.Type.IS_BROADER_THAN);
		assertNotNull(termPath);
		// Should be of length three, just looked it up in the Neo4j server
		// graph visualization
		assertEquals("Length of path is wrong", 3, termPath.length());

		termPath = neo4jService.getTermPath(term2Id, term1Id,
				IFacetTermRelation.Type.IS_BROADER_THAN);
		assertNull(termPath);
	}

	@Test
	public void testPopTermsFromSet() {
		PushTermsToSetCommand cmd = new PushTermsToSetCommand(
				TermLabels.GeneralLabel.PENDING_FOR_SUGGESTIONS.toString());
		cmd.eligibleTermDefinition = cmd.new TermSelectionDefinition();
		cmd.eligibleTermDefinition.facetLabel = FacetLabels.General.USE_FOR_SUGGESTIONS.name();
		// neo4jService.pushAllTermsToSet(TermLabels.General.PENDING_FOR_SUGGESTIONS.toString(),
		// NodeConstants.PROP_GENERAL_LABELS,
		// FacetLabels.General.USE_FOR_SUGGESTIONS.name(), "", "");
		neo4jService.pushTermsToSet(cmd, 0);
		JSONArray popNodesFromSet = neo4jService.popTermsFromSet(
				TermLabels.GeneralLabel.PENDING_FOR_SUGGESTIONS.toString(), 10);
		assertEquals(10, popNodesFromSet.length());
	}

	@Test
	public void testPushAllTermsToSetTest() {
		String countTermsWithLabelQuery = "MATCH (n:"
				+ TermLabels.GeneralLabel.PENDING_FOR_SUGGESTIONS + ") RETURN COUNT(*)";
		Neo4jService neo4jServiceImpl = (Neo4jService) neo4jService;
		PushTermsToSetCommand cmd = new PushTermsToSetCommand(
				TermLabels.GeneralLabel.PENDING_FOR_SUGGESTIONS.toString());
		cmd.eligibleTermDefinition = cmd.new TermSelectionDefinition();
		cmd.eligibleTermDefinition.facetLabel = FacetLabels.General.USE_FOR_SUGGESTIONS.name();
		neo4jService.pushTermsToSet(cmd, 0);
		int numberOfTermsInSuggestionQueueBefore = (new JSONObject(
				neo4jServiceImpl.sendCypherQuery(countTermsWithLabelQuery)))
				.getJSONArray(Neo4jService.DATA).getJSONArray(0).getInt(0);
		neo4jService
				.popTermsFromSet(TermLabels.GeneralLabel.PENDING_FOR_SUGGESTIONS.toString(), 10);
		int numberOfTermsInSuggestionQueueAfter = (new JSONObject(
				neo4jServiceImpl.sendCypherQuery(countTermsWithLabelQuery)))
				.getJSONArray(Neo4jService.DATA).getJSONArray(0).getInt(0);
		assertEquals("Number of terms in suggestion queue",
				numberOfTermsInSuggestionQueueBefore - 10, numberOfTermsInSuggestionQueueAfter);
	}

	@Test
	public void testTermPathExists() {
		// Terms "Gene Rearrangement, B-Lymphocyte" and
		// "Immunoglobulin Class Switching"
		String term1Id = NodeIDPrefixConstants.TERM + 176;
		String term2Id = NodeIDPrefixConstants.TERM + 188;
		boolean termPathExists = neo4jService.termPathExists(term1Id, term2Id,
				IFacetTermRelation.Type.IS_BROADER_THAN);
		assertTrue("Term path does not exist", termPathExists);

		termPathExists = neo4jService.termPathExists(term2Id, term1Id,
				IFacetTermRelation.Type.IS_BROADER_THAN);
		assertFalse(termPathExists);
	}

	@Test
	public void testGetNumTerms() {
		int numTerms = neo4jService.getNumTerms();
		assertTrue("There are no terms in the DB", numTerms > 0);
	}

	@Test
	public void testGetFacetRootTerms() {
		// Facets "Investigative Techniques", "Immune Processes",
		// "Immunoglobulins and Antibodies" and
		// "Genes and Proteins"
		List<String> facetIds = Lists.newArrayList("fid5", "fid2", "fid4", "fid3");
		JSONObject facetRoots = neo4jService.getFacetRootTerms(facetIds, null, 0);
		Set<String> oneRootOfEachFacet = new HashSet<>();
		int numRoots = 0;
		JSONArray facetIdNames = facetRoots.names();
		assertEquals("Wrong amount of columns;", 4, facetIdNames.length());
		for (int k = 0; k < facetIdNames.length(); k++) {
			String facetId = facetIdNames.getString(k);
			JSONArray rootNodes = facetRoots.getJSONArray(facetId);
			assertTrue("Result contains non-queried facets (" + facetId + ")",
					facetId.equals("fid3") || facetId.equals("fid5") || facetId.equals("fid2")
							|| facetId.equals("fid4"));
			for (int j = 0; j < rootNodes.length(); j++) {
				numRoots++;
				JSONObject rootNode = rootNodes.getJSONObject(j);
				String termId = rootNode.getString(ConceptConstants.PROP_ID);
				oneRootOfEachFacet.add(termId);
			}
		}
		assertEquals("Amount of root terms is not right:", 1217, numRoots);
		assertTrue("Root for fid5 not found", oneRootOfEachFacet.contains("tid1843"));
		assertTrue("Root for fid4 not found", oneRootOfEachFacet.contains("tid1631"));
		assertTrue("Root for fid2 not found", oneRootOfEachFacet.contains("tid175"));
		assertTrue("Root for fid3 not found", oneRootOfEachFacet.contains("tid993"));
	}

	@Test
	public void testGetTermsByLabel() {
		JSONArray eventTermIDs = neo4jService.getTermIdsByLabel(TermLabels.GeneralLabel.EVENT_TERM
				.name());
		// There should be a few event type terms, but we won't fix ourselves to
		// a particular number since this
		// could change in the future and would break the test.
		assertTrue(eventTermIDs.length() > 5);
	}
}
