package de.julielab.semedico.core.services.interfaces;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.interfaces.IPath;
import de.julielab.semedico.core.facets.Facet;

public interface ITermService {

	/**
	 * @param facet
	 * @return
	 */
	public List<Concept> getFacetRoots(Facet facet);

	/**
	 * Returns the <code>MultiHierarchyNode</code> with identifier
	 * <code>id</code>
	 * 
	 * @param id
	 *            The identifier of the node to return.
	 * @return The node with identifier <code>id</code> or <code>null</code> if
	 *         no such node exists.
	 */
	public IConcept getTerm(String id);

	IConcept getTermIfCached(String id);

	/**
	 * Returns <code>true</code> iff the implementation has a node with ID
	 * <code>id</code>.
	 * 
	 * @param id
	 *            Node ID to check for an existing node with that ID.
	 * @return <code>true</code> if there is a node with this ID in the
	 *         taxonomy, false otherwise.
	 */
	public boolean hasTerm(String id);

	/**
	 * <p>
	 * WARNING: May be slow, test this before use.
	 * </p>
	 * <p>
	 * Returns all nodes contained in this multi hierarchy.
	 * </p>
	 * 
	 * @return All nodes of the hierarchy.
	 */
	public Iterator<IConcept> getTerms();

	public Iterator<IConcept> getTerms(int limit);

	public long pushAllTermsIntoSuggestionQueue();

	public long pushAllTermsIntoDictionaryQueue();

	/**
	 * Pushes all terms - without any restrictions - into the set
	 * <code>queueName</code>.
	 * 
	 * @param queueName
	 * @return
	 */
	public long pushAllTermsIntoQueue(String queueName);

	public Iterator<IConcept> getTermsInSuggestionQueue();

	public Iterator<IConcept> getTermsInQueryDictionaryQueue();

	public Iterator<IConcept> getTermsInQueue(String queueName);

	// public Iterator<IFacetTerm> getTermsWithLabel(TermLabels.General label);

	/**
	 * Returns a shortest (not necessarily unique!) path from any facet root to
	 * <code>node</code>, including the root and <code>node</code>.
	 * 
	 * @param node
	 *            The node of which the root path should be returned.
	 * @return A shortest path from any facet, including the respective facet
	 *         root and <code>node</code> itself.
	 */
	public IPath getShortestPathFromAnyRoot(IConcept node);

	/**
	 * Returns a shortest (not necessarily unique!) path from a root of
	 * <code>facet</code> to <code>node</code>, including the root and
	 * <code>node</code>.
	 * 
	 * @param node
	 * @return
	 */
	public IPath getShortestRootPathInFacet(IConcept node, Facet facet);

	/**
	 * Returns all paths from any root of <code>facet</code> to
	 * <code>term</code>. All paths include the respective root and
	 * <code>term</code>.
	 * 
	 * @param term
	 * @param facet
	 * @return
	 */
	public Collection<IPath> getAllPathsFromRootsInFacet(IConcept term, Facet facet);

	// TODO perhaps the arguments should be of type Concept rather than IConcept
	// to reflect that IConcepts are meant as
	// entities without relationships. If this paradigm still holds later, this
	// should be changed.
	public boolean isAncestorOf(IConcept candidate, IConcept term);

	public int getNumTerms();

	public void loadChildrenOfTerm(Concept facetTerm);

	int getNumLoadedRoots(String facetId);

	/**
	 * Assures that at least the list of term IDs given for each facet in
	 * <tt>requestRootIds</tt> is loaded for this facet, if the respective term
	 * is indeed a root term for the facet. Please be aware of the facet that
	 * the return value are the newly loaded roots of each facet, not all roots
	 * loaded until the time of the call.
	 * 
	 * @param requestRootIds
	 * @return The newly loaded roots of each facet, not all roots loaded until
	 *         the time of the call.
	 */
	Map<String, List<Concept>> assureFacetRootsLoaded(Map<Facet, List<String>> requestRootIds);

	void loadChildrenOfTerm(Concept facetTerm, String termLabel);

	public boolean isTermID(String termId);

	List<IConcept> getTermsByLabel(String label, boolean sort);

	IConcept getTermSynchronously(String id);
}
