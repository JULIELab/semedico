package de.julielab.semedico.core.services.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

import de.julielab.neo4j.plugins.datarepresentation.PushTermsToSetCommand;
import de.julielab.semedico.core.concepts.interfaces.IFacetTermRelation.Type;
import de.julielab.semedico.core.facets.FacetLabels;

public interface ITermDatabaseService {

	/**
	 * <p>
	 * Returns a JSONArray of facet groups. Each facet group has its own properties as well as a special property
	 * <tt>facets</tt>. This property enumerates the facets belonging to the facet group.
	 * </p>
	 * <p>
	 * A facet is then itself a JSONObject holding the properties defining the facet.
	 * </p>
	 * 
	 * @param getHollowfacets
	 * 
	 * @return
	 */
	public JSONArray getFacets(boolean getHollowfacets);

	/**
	 * Returns up to <tt>limit</tt> terms from the database. If <tt>limit</tt> is negative, all terms are returned.
	 * 
	 * @param limit
	 * @return
	 */
	public JSONArray getTerms(int limit);

	public JSONArray getTerm(String id);

	public List<String> getFacetIdsWithGeneralLabel(FacetLabels.General label);

	/**
	 * @deprecated only a fast hack. we still have no clean, comprehensive and complete way to insert terms from
	 *             multiple sources.
	 * @param termMap
	 * @param facetName
	 */
	@Deprecated
	public void addTerm(Map<String, Object> termMap, String facetName);

	public JSONArray getTerms(Iterable<String> ids);

	public boolean termPathExists(String sourceId, String targetId, Type... types);

	public JSONArray getTermPath(String sourceId, String targetId, Type... types);

	/**
	 * <p>
	 * This method is a convenience call to {@link #getShortestPathFromAnyRoot(String, String)} with the
	 * default term ID type.
	 * </p>
	 * 
	 * @param termId
	 *            ID of the term to get the root path for.
	 * @return The root path in the form <tt>["id1", "id2", "id3", "id4"]</tt>.
	 */
	public JSONArray getShortestPathFromAnyRoot(String termId);

	/**
	 * <p>
	 * Gets the shortest path from a facet root term to the term with ID <tt>termId</tt>, restricted to consist of
	 * relationship types specified by <tt>types</tt>. Internally, uses {@link #getPathsFromRoots(Collection, String)} to
	 * get all paths and then return the shortest.
	 * </p>
	 * <p>
	 * The return format is a single array containing the term IDs on the path, e.g.<br />
	 * <tt>["id1", "id2", "id3", "id4"]</tt>
	 * </p>
	 * 
	 * @param termId
	 *            ID of the term to get the root path for.
	 * @param idType
	 *            The term ID type that should be queried for node retrieval.
	 * @return The shortest root-term path for the desired term.
	 */
	public JSONArray getShortestPathFromAnyRoot(String termId, String idType);

	/**
	 * <p>
	 * Determines all paths from any facet root term to the term with ID <tt>termId</tt>.
	 * </p>
	 * <p>
	 * The traversal is restricted to go to the term with ID <tt>termId</tt> from facet root terms, i.e. terms matching
	 * <tt>anynode-[HAS_ROOT]-&gt;root-[types]-&gt;termId</tt>. All relationships from root to the searched term are
	 * restricted to be of one type in <tt>types</tt>. Thus, multiple paths may be returned. Returned paths are ordered
	 * by length, ascending. Additionally, root terms of the NO_FACET facet are excluded. The thought behind the
	 * exclusion is, that we most probably only want these root paths to show them in the frontend, but we won't show
	 * terms in the NO_FACET facet.
	 * </p>
	 * <p>
	 * The output format is one row with an array of term IDs on the respective path for each path, e.g.<br />
	 * <tt>[ [ ["id1", "id2"] ], [ ["id3", "id4", "id5"] ] ]</tt>
	 * </p>
	 * 
	 * @param termId
	 *            ID of the term to get root paths for. The type of the ID (Semedico ID, source ID, ...) must fit the
	 *            index given by <tt>indexName</tt>.
	 * @param idType
	 *            The term ID type that should be queried for node retrieval.
	 * @param sortByLength
	 * @return All paths from a facet root to the term with ID <tt>termId</tt>, sorted ascending by path length.
	 */
	public JSONArray getAllPathsFromAnyRoots(String termId, String idType, boolean sortByLength);

	/**
	 * <p>
	 * Returns the same data as {@link #getAllPathsFromAnyRoots(String, String, boolean)} but for a <tt>Collection</tt>
	 * of term IDs. I.e. the main difference between the two methods is that this method returns multiple columns.
	 * </p>
	 * 
	 * @param termIds
	 * @param idType
	 * @return
	 */
	public JSONArray getPathsFromRoots(Collection<String> termIds, String idType);

	/**
	 * <p>
	 * Calls {@link #getAllPathsFromAnyRoots(String, String, boolean)} with the default term index type.
	 * </p>
	 * 
	 * @param termId
	 *            The Semedico term ID to get root paths for.
	 * @return All paths from a facet root to the term with ID <tt>termId</tt>, sorted ascending by path length.
	 */
	public JSONArray getAllPathsFromAnyRoots(String termId, boolean sortByLength);

	/**
	 * <p>
	 * Returns the taxonomic term roots of the facet with ID <tt>facetId</tt>. The returned format is
	 * <tt>["id1", "id2", "id3", "id4"]</tt>.
	 * </p>
	 * 
	 * @param facetId
	 *            The facet ID of the facet to get the root terms for.
	 * @return The taxonomic root terms of the facet identified with <tt>facetId</tt>.
	 */
	public JSONArray getFacetRootIDs(String facetId);

	public JSONArray popTermsFromSet(String label, int amount);

	public int getNumTerms();

	/**
	 * Pushes <tt>amount</tt> terms to the set specified by <tt>cmd</tt>. If <tt>amount</tt> is equal or less than zero,
	 * all terms will be pushed into the set.
	 * 
	 * @param cmd
	 * @param amount
	 * @return
	 */
	public long pushTermsToSet(PushTermsToSetCommand cmd, int amount);

	/**
	 * 
	 * @param termIds
	 * @param label The label against which to resolve the IDs (e.g. TERM or AGGREGATE).
	 * @return
	 */
	public JSONObject getTermChildren(Iterable<? extends String> termIds, String label);

	JSONArray getShortestRootPathInFacet(String termId, String facetId);

	JSONArray getPathsFromRootsInFacet(Collection<String> termIds, String idType, boolean sortByLength, String facetId);

	int getNumFacets();

	JSONObject getFacetRootTerms(Iterable<? extends String> facetIds, Map<String, List<String>> requestedRootIds,
			int maxRoots);

	JSONArray getFacetRootIDs(Iterable<? extends String> facetIds);

	public JSONArray getTermIdsByLabel(String label);

}
