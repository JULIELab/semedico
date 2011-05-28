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

package de.julielab.semedico.core.MultiHierarchy;

/**
 * An interface for<code>MultiHierarchyNode</code> objects.
 * 
 * @see MultiHierarchyNode
 * 
 * @author faessler
 */
public interface IMultiHierarchyNode {

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

	/**
	 * Adds <code>parentNode</code> to the parents of this node.
	 * 
	 * @param parentNode
	 *            The new parent node of this node.
	 */
	public void addParent(MultiHierarchyNode parentNode);

	/**
	 * Returns the first parent of this node.
	 * 
	 * @return The first parent.
	 */
	public MultiHierarchyNode getFirstParent();

	/**
	 * Returns the parent node with index i.
	 * 
	 * @param i
	 *            The index of the parent node to return.
	 * @return The ith parent of this node.
	 */
	public MultiHierarchyNode getParent(int i);

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
	public boolean hasParent(MultiHierarchyNode node);

	/**
	 * Adds <code>childNode</code> to the children of this node.
	 * 
	 * @param childNode
	 *            New new child node for this node.
	 */
	public void addChild(MultiHierarchyNode childNode);

	/**
	 * Returns the first child of this node.
	 * 
	 * @return The first child.
	 */
	public MultiHierarchyNode getFirstChild();

	/**
	 * Returns the child node with index i.
	 * 
	 * @param i
	 *            The index of the child node to return.
	 * @return The ith child of this node.
	 */
	public MultiHierarchyNode getChild(int i);

	/**
	 * Returns true if this node contains <code>node</code> as a child.
	 * 
	 * @param node
	 *            The potential child node to be tested.
	 * @return true if <code>node</code> is a child of this node, false
	 *         otherwise.
	 */
	public boolean hasChild(MultiHierarchyNode node);

	/**
	 * Returns the number of children for this node.
	 * 
	 * @return The number of children.
	 */
	public int getNumberOfChildren();

}
