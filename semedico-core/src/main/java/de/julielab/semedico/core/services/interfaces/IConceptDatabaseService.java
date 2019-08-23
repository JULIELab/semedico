package de.julielab.semedico.core.services.interfaces;

import com.google.common.collect.Multimap;
import de.julielab.neo4j.plugins.datarepresentation.PushConceptsToSetCommand;
import de.julielab.neo4j.plugins.datarepresentation.constants.ConceptConstants;
import de.julielab.semedico.commons.concepts.FacetLabels;
import de.julielab.semedico.core.concepts.ConceptDescription;
import de.julielab.semedico.core.concepts.interfaces.IConceptRelation.Type;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.util.ConceptLoadingException;
import org.apache.tapestry5.json.JSONObject;
import org.neo4j.driver.v1.StatementResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface IConceptDatabaseService {

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
     * @return
     */
    Stream<FacetGroup<Facet>> getFacetGroups(boolean getHollowfacets);

    /**
     * Returns up to <tt>limit</tt> terms from the database. If <tt>limit</tt> is negative, all terms are returned.
     *
     * @param limit
     * @return
     */
    Stream<ConceptDescription> getConcepts(int limit);

    Optional<ConceptDescription> getConcept(String id);

    List<String> getFacetIdsWithGeneralLabel(FacetLabels.General label);

    Stream<ConceptDescription> getConcepts(Iterable<String> ids);

    boolean termPathExists(String sourceId, String targetId, Type... types);

    Optional<ConceptDescription> getConceptBySourceId(String id);

    Optional<ConceptDescription> getConceptBySourceId(String source, String id);

    Stream<ConceptDescription> getConceptsBySourceId(Iterable<String> ids);

    Stream<ConceptDescription> getConceptsBySourceId(Iterable<String> sources, Iterable<String> ids);

    /**
     * Returns the IDs of the concepts of a shortest path between the concepts with IDs <tt>sourceId</tt> and <tt>targetId</tt>.
     * The path might be restricted to a set of allowed relationship types <tt>types</tt>.
     * Note that <tt>sourceId</tt> and <tt>targetId</tt> must not be equal.
     *
     * @param sourceId ID of the start node.
     * @param targetId ID of the target node.
     * @param types    The allowed relation types.
     * @return An array of concept IDs lying on the shortest path.
     */
    String[] getConceptPath(String sourceId, String targetId, Type... types);

    /**
     * <p>
     * This method is a convenience call to {@link #getShortestPathFromAnyRoot(String, String)} with the
     * default concept ID type {@link ConceptConstants#PROP_ID}.
     * </p>
     *
     * @param conceptId ID of the term to get the root path for.
     * @return The root path in the form <tt>["id1", "id2", "id3", "id4"]</tt>.
     * @see #getShortestPathFromAnyRoot(String, String)
     */
    String[] getShortestPathFromAnyRoot(String conceptId);

    /**
     * <p>
     * Gets the shortest path from a facet root concept to the concept with ID <tt>conceptId</tt>, restricted to consist of
     * relationship types specified by <tt>types</tt>. Internally, uses {@link #getPathsFromRoots(Collection, String)} to
     * get all paths and then return the shortest.
     * </p>
     * <p>
     * The return format is a single array containing the term IDs on the path, e.g.<br />
     * <tt>["id1", "id2", "id3", "id4"]</tt>
     * </p>
     *
     * @param conceptId ID of the term to get the root path for.
     * @param idType    The term ID type that should be queried for node retrieval (see respective constants in
     *                  {@link ConceptConstants}).
     * @return The shortest root-term path for the desired term.
     * @see #getPathsFromRoots(Collection, String)
     */
    String[] getShortestPathFromAnyRoot(String conceptId, String idType);

    /**
     * <p>
     * Determines all paths from any facet root term to the term with ID <tt>termId</tt>.
     * </p>
     * <p>
     * The traversal is restricted to go to the term with ID <tt>termId</tt> from facet root terms, i.e. terms matching
     * <tt>anynode-[HAS_ROOT]->root-[types]->termId</tt>. All relationships from root to the searched term are
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
     * @param termId ID of the term to get root paths for. The type of the ID (Semedico ID, facetSource ID, ...) must fit the
     *               index given by <tt>indexName</tt>.
     * @param idType The term ID type that should be queried for node retrieval (see respective constants in
     *               {@link ConceptConstants}).
     * @return All paths from a facet root to the term with ID <tt>termId</tt>, sorted ascending by path length.
     */
    String[][] getAllPathsFromAnyRoots(String termId, String idType, boolean sortByLength);

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
    String[][] getPathsFromRoots(Collection<String> termIds, String idType);

    /**
     * <p>
     * Calls {@link #getAllPathsFromAnyRoots(String, String, boolean)} with the default term index type
     * {@link ConceptConstants#PROP_ID}.
     * </p>
     *
     * @param conceptId The Semedico term ID to get root paths for.
     * @return All paths from a facet root to the term with ID <tt>conceptId</tt>, sorted ascending by path length.
     * @see #getAllPathsFromAnyRoots(String, String, boolean)
     */
    String[][] getAllPathsFromAnyRoots(String conceptId, boolean sortByLength);

    /**
     * <p>
     * Returns the taxonomic term roots of the facet with ID <tt>facetId</tt>. The returned format is
     * <tt>["id1", "id2", "id3", "id4"]</tt>.
     * </p>
     *
     * @param facetId The facet ID of the facet to get the root terms for.
     * @return The taxonomic root terms of the facet identified with <tt>facetId</tt>.
     */
    String[] getFacetRootIDs(String facetId);


    /**
     * <p>
     * Retrieves a number <tt>amount</tt> of concept IDs from the database with the retrieval label <tt>label</tt>.
     * </p>
     *
     * @param label  The label that is used to define the set of concepts.
     * @param amount The number of concept IDs to take out of the set and return.
     * @return The IDs of the concepts removed from the set.
     */
    List<ConceptDescription> popTermsFromSet(String label, int amount) throws ConceptLoadingException;

    int getNumConcepts();

    /**
     * Pushes <tt>amount</tt> terms to the set specified by <tt>cmd</tt>. If <tt>amount</tt> is equal or less than zero,
     * all terms will be pushed into the set.
     *
     * @param cmd
     * @param amount
     * @return
     */
    long pushTermsToSet(PushConceptsToSetCommand cmd, int amount);

    /**
     * @param termIds
     * @param label   The label against which to resolve the IDs (e.g. TERM or AGGREGATE).
     * @return
     */
    JSONObject getTermChildren(Iterable<? extends String> termIds, String label);

    String[] getShortestRootPathInFacet(String termId, String facetId);

    String[][] getPathsFromRootsInFacet(Collection<String> termIds, String idType, boolean sortByLength, String facetId);

    int getNumFacets();

    Multimap<String, ConceptDescription> getFacetRootConcepts(Iterable<? extends String> facetIds, Map<String, List<String>> requestedRootIds,
                                                              int maxRoots) throws ConceptLoadingException;

    String[] getFacetRootIDs(Iterable<? extends String> facetIds);

    String[] getTermIdsByLabel(String label);

    StatementResult sendCypherQueryViaBolt(String query, Map<String, Object> parameters);

}
