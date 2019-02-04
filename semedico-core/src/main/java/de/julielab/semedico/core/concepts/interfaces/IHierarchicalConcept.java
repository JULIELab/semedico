
package de.julielab.semedico.core.concepts.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.julielab.semedico.core.entities.ConceptRelationKey;
import de.julielab.semedico.core.concepts.DescribableConcept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.IConceptService;

/**
 * 
 * @see IConcept
 * 
 * @author faessler
 */
public interface IHierarchicalConcept extends IConcept, DescribableConcept {

	/**
	 * Return this term's identifier.
	 * 
	 * @return The ID of this term.
	 */
	 String getId();

	 String getPreferredName();

	/**
	 * Returns the first parent of this node.
	 * 
	 * @return The first parent.
	 */
	 IConcept getFirstParent();

	/**
	 * Returns the parent node with index i.
	 * 
	 * @param i
	 *            The index of the parent node to return.
	 * @return The ith parent of this node.
	 */
	 IConcept getParent(int i);

	/**
	 * Returns the number of parents for this node. TODO I think this cannot be used since the caching stuff; good that
	 * it isn't required until now ;-) The problem is that the parents would have to be loaded first. We only do this for children currently.
	 * 
	 * @return The number of parents.
	 */
	 int getNumberOfParents();

	/**
	 * Returns <code>true</code> if this node has at least one parent node.
	 * 
	 * @return true if this node has at least one parent.
	 */
	 boolean hasParent();

	/**
	 * Returns true if this node contains <code>node</code> as a parent.
	 * 
	 * @param node
	 *            The potential parent node to be tested.
	 * @return true if <code>node</code> is a parent of this node, false otherwise.
	 */
	 boolean hasParent(IConcept node);

	/**
	 * Returns the first child of this node.
	 * 
	 * @return The first child.
	 */
	 IConcept getFirstChild();

	/**
	 * Returns all children of this node as a <code>Collection</code>.
	 * 
	 * @return The children of this node.
	 */
	 Collection<IConcept> getAllChildren();

	/**
	 * Returns the child node with index i.
	 * 
	 * @param i
	 *            The index of the child node to return.
	 * @return The ith child of this node.
	 */
	 IConcept getChild(int i);

	/**
	 * Returns true if this node contains <code>node</code> as a child.
	 * 
	 * @param node
	 *            The potential child node to be tested.
	 * @return true if <code>node</code> is a child of this node, false otherwise.
	 */
	 boolean hasChild(IConcept node);

	/**
	 * Returns the number of children for this node.
	 * 
	 * @return The number of children.
	 */
	 int getNumberOfChildren();

	 boolean hasChildrenInFacet(String facetId);

	 void addFacet(Facet facet);

	/**
	 * @return
	 */
	 boolean hasChildren();

	/**
	 * @return
	 */
	 List<Facet> getFacets();

	/**
	 * Returns true for all facets in which this term is included.
	 * 
	 * @param facet
	 *            The facet to check whether this term is contained in.
	 * @return True if this term is included in <code>facet</code>, false otherwise.
	 */
	 boolean isContainedInFacet(Facet facet);

	 void setWritingVariants(List<String> writingVariants);

	 List<String> getWritingVariants();

	 void setSourceIds(List<String> sourceIds);

	 void setOriginalId(String originalId);

	 void setPreferredName(String preferredName);

	 void setSynonyms(List<String> synonyms);

	 void setFacets(List<Facet> facets);

	 void setIncomingRelationships(Map<String, List<IConceptRelation>> incomingRelationships);

	 void setOutgoingRelationships(Map<String, List<IConceptRelation>> outgoingRelationships);

	 IConceptRelation getRelationShipWithKey(ConceptRelationKey key);

	 List<String> getSourceIds();

	 String getOriginalId();

	 Collection<String> getAllChildIds();

	 void setConceptService(IConceptService termService);

	/**
	 * Sets the ID of the term, if it does not already have one. Setting an ID to a term that already has an ID is
	 * illegal and will cause an exception to be thrown.
	 * 
	 * @param id
	 * @throws IllegalAccessError
	 *             If the term already has an ID.
	 */
	 void setId(String id);

	void addOutgoingRelationship(IConceptRelation outgoingRelationship);

	void addIncomingRelationship(IConceptRelation incomingRelationship);

	void addChildrenFacet(String facetId);


}
