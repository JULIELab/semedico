/**
 * IMultiHierarchyNode.java
 *
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 28.05.2011
 **/

package de.julielab.semedico.core.concepts.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.julielab.semedico.core.TermRelationKey;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.ITermService;

/**
 * 
 * @see IConcept
 * 
 * @author faessler
 */
public interface IFacetTerm extends IConcept {

	/**
	 * Return this term's identifier.
	 * 
	 * @return The ID of this term.
	 */
	public String getId();

	/**
	 * @see TermConstants#PROP_PREF_NAME
	 */
	public String getPreferredName();

	/**
	 * Returns the first parent of this node.
	 * 
	 * @return The first parent.
	 */
	public IConcept getFirstParent();

	/**
	 * Returns the parent node with index i.
	 * 
	 * @param i
	 *            The index of the parent node to return.
	 * @return The ith parent of this node.
	 */
	public IConcept getParent(int i);

	/**
	 * Returns the number of parents for this node. TODO I think this cannot be used since the caching stuff; good that
	 * it isn't required until now ;-) The problem is that the parents would have to be loaded first. We only do this for children currently.
	 * 
	 * @return The number of parents.
	 */
	public int getNumberOfParents();

	/**
	 * Returns <code>true</code> if this node has at least one parent node.
	 * 
	 * @return true if this node has at least one parent.
	 */
	public boolean hasParent();

	/**
	 * Returns true if this node contains <code>node</code> as a parent.
	 * 
	 * @param node
	 *            The potential parent node to be tested.
	 * @return true if <code>node</code> is a parent of this node, false otherwise.
	 */
	public boolean hasParent(IConcept node);

	/**
	 * Returns the first child of this node.
	 * 
	 * @return The first child.
	 */
	public IConcept getFirstChild();

	/**
	 * Returns all children of this node as a <code>Collection</code>.
	 * 
	 * @return The children of this node.
	 */
	public Collection<IConcept> getAllChildren();

	/**
	 * Returns the child node with index i.
	 * 
	 * @param i
	 *            The index of the child node to return.
	 * @return The ith child of this node.
	 */
	public IConcept getChild(int i);

	/**
	 * Returns true if this node contains <code>node</code> as a child.
	 * 
	 * @param node
	 *            The potential child node to be tested.
	 * @return true if <code>node</code> is a child of this node, false otherwise.
	 */
	public boolean hasChild(IConcept node);

	/**
	 * Returns the number of children for this node.
	 * 
	 * @return The number of children.
	 */
	public int getNumberOfChildren();

	public boolean hasChildrenInFacet(String facetId);

	public void addFacet(Facet facet);

	/**
	 * @param size
	 */
	@Deprecated
	public void setFacetIndex(int size);

	/**
	 * @return
	 */
	public boolean hasChildren();

	/**
	 * @return
	 */
	public List<Facet> getFacets();

	/**
	 * Returns true for all facets in which this term is included.
	 * 
	 * @param facet
	 *            The facet to check whether this term is contained in.
	 * @return True if this term is included in <code>facet</code>, false otherwise.
	 */
	public boolean isContainedInFacet(Facet facet);

	public void setWritingVariants(List<String> writingVariants);

	/**
	 * @see TermConstants#PROP_WRITING_VARIANTS
	 */
	public List<String> getWritingVariants();

	public void setSourceIds(List<String> sourceIds);

	public void setOriginalId(String originalId);

	public void setPreferredName(String preferredName);

	public void setSynonyms(List<String> synonyms);

	public void setFacets(List<Facet> facets);

	public void setIncomingRelationships(Map<String, List<IFacetTermRelation>> incomingRelationships);

	public void setOutgoingRelationships(Map<String, List<IFacetTermRelation>> outgoingRelationships);

	public IFacetTermRelation getRelationShipWithKey(TermRelationKey key);

	public List<String> getSourceIds();

	public String getOriginalId();

	public Collection<String> getAllChildIds();

	public void setTermService(ITermService termService);

	/**
	 * Sets the ID of the term, if it does not already have one. Setting an ID to a term that already has an ID is
	 * illegal and will cause an exception to be thrown.
	 * 
	 * @param id
	 * @throws IllegalAccessError
	 *             If the term already has an ID.
	 */
	public void setId(String id);

	void addOutgoingRelationship(IFacetTermRelation outgoingRelationship);

	void addIncomingRelationship(IFacetTermRelation incomingRelationship);

	void addChildrenFacet(String facetId);
}
