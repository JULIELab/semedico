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

package de.julielab.semedico.core.Taxonomy;

import java.util.Collection;
import java.util.Iterator;

import de.julielab.semedico.core.Facet;

/**
 * An interface for<code>IMultiHierarchyNode</code> objects.
 * 
 * @see IFacetTerm
 * 
 * @author faessler
 */
public interface IFacetTerm {

	/**
	 * Return this node's identifier.
	 * 
	 * @return The ID of this node.
	 */
	public String getId();

	/**
	 * Return this node's name. In Semedico, this name is the term name, e.g.
	 * "Hematopoiesis", "Cell Aggregation" etc.
	 * 
	 * @return The name of this node.
	 */
	public String getName();
	
	public void addParent(IFacetTerm parent);

	/**
	 * Returns the first parent of this node.
	 * 
	 * @return The first parent.
	 */
	public IFacetTerm getFirstParent();

	/**
	 * Returns all parents of this node as a <code>Collection</code>.
	 * 
	 * @return The parents of this node.
	 */
	public Collection<IFacetTerm> getAllParents();
	
	/**
	 * Returns the parent node with index i.
	 * 
	 * @param i
	 *            The index of the parent node to return.
	 * @return The ith parent of this node.
	 */
	public IFacetTerm getParent(int i);

	/**
	 * Returns the number of parents for this node.
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
	 * @return true if <code>node</code> is a parent of this node, false
	 *         otherwise.
	 */
	public boolean hasParent(IFacetTerm node);

	/**
	 * Returns the first child of this node.
	 * 
	 * @return The first child.
	 */
	public IFacetTerm getFirstChild();

	/**
	 * Returns all children of this node as a <code>Collection</code>.
	 * 
	 * @return The children of this node.
	 */
	public Collection<IFacetTerm> getAllChildren();
	
	/**
	 * Returns the child node with index i.
	 * 
	 * @param i
	 *            The index of the child node to return.
	 * @return The ith child of this node.
	 */
	public IFacetTerm getChild(int i);

	/**
	 * Returns true if this node contains <code>node</code> as a child.
	 * 
	 * @param node
	 *            The potential child node to be tested.
	 * @return true if <code>node</code> is a child of this node, false
	 *         otherwise.
	 */
	public boolean hasChild(IFacetTerm node);

	/**
	 * Returns the number of children for this node.
	 * 
	 * @return The number of children.
	 */
	public int getNumberOfChildren();

	public Iterator<IFacetTerm> childIterator();
	
	public void addFacet(Facet facet);
	
	/**
	 * To be replaced by searchFieldNames of the Facet class.
	 * @param indexNames
	 */
	@Deprecated
	public void setIndexNames(Collection<String> indexNames);
	
	public void setDescription(String description);
	
	public void setShortDescription(String shortDescription);
	@Deprecated
	public void setKwicQuery(String kwicQuery);
	@Deprecated
	public String getKwicQuery();
	
	public Facet getFirstFacet();

	/**
	 * To be replaced by searchFieldNames of the Facet class.
	 * 
	 * @return
	 */
	@Deprecated
	public Collection<String> getIndexNames();

	/**
	 * @return
	 */
	public String getSynonyms();

	/**
	 * @return
	 */
	public String getDescription();

	/**
	 * @return
	 */
	@Deprecated
	public int getDatabaseId();

	/**
	 * @param size
	 */
	@Deprecated
	public void setFacetIndex(int size);

	/**
	 * @param multiHierarchyNode
	 */
	public void addChild(IFacetTerm multiHierarchyNode);

	/**
	 * @return
	 */
	public boolean hasChildren();

	/**
	 * @return
	 */
	public Collection<Facet> getFacets();

	/**
	 * Returns true for all facets in which this term is included.
	 * 
	 * @param facet The facet to check whether this term is contained in.
	 * @return True if this term is included in <code>facet</code>, false otherwise.
	 */
	public boolean isContainedInFacet(Facet facet);
	
}
