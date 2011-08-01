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
 * Creation date: 27.05.2011
 **/

package de.julielab.semedico.core.Taxonomy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * An abstract class for a node of a (multi)-hierarchy.
 * <p>
 * A <code>IMultiHierarchyNode</code> knows its parent(s) as well as its
 * children. Algorithms on nodes e.g. for obtaining all ancestors of a node are
 * given by {@link MultiHierarchy}.
 * </p>
 * <p>
 * <code>IMultiHierarchyNode</code> objects are used in Semedico for the
 * FacetTerms as well as their facet count objects (Label).
 * </p>
 * <p>
 * The building operations like {@link #addParent(IFacetTerm)} and
 * {@link #addChild(IFacetTerm)} are not focused on efficiency. It is
 * expected that these operations are used in a single building process and then
 * are needed any longer.
 * </p>
 * 
 * @see MultiHierarchy
 * @author faessler
 */
public abstract class MultiHierarchyNode implements IFacetTerm {

	/**
	 * This node's ID.
	 */
	protected String id;

	protected String name;

	// TODO how can we express that the parents and the children must be of the
	// same class as this node? That is, if we have an FacetTerm, there should
	// only be FacetTerms as children and parents, not labels...
	/**
	 * All parents of this node.
	 */
	protected List<IFacetTerm> parents;

	/**
	 * All children of this node.
	 */
	protected List<IFacetTerm> children;

	/**
	 * Creates a <code>IMultiHierarchyNode</code> with identifier <code>id</code>
	 * .
	 * 
	 * @param id
	 *            The name of the node to create.
	 */
	public MultiHierarchyNode(String id, String name) {
		this.id = id;
		this.name = name;
		parents = new ArrayList<IFacetTerm>();
		children = new ArrayList<IFacetTerm>();
	}

	/**
	 * Return this node's identifier.
	 * 
	 * @return The ID of this node.
	 */
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	/**
	 * Adds <code>parentNode</code> to the parents of this node.
	 * 
	 * @param parent
	 *            The new parent node of this node.
	 */
	public void addParent(IFacetTerm parent) {
		//has to be public for the use of services
		if (parents.contains(parent))
			return;

		// The contract is that when a new node is no parent of this node, than
		// this node is not yet a child of the parent node. So no security
		// checks needed here.
		parents.add(parent);
		parent.addChild(this);
	}

	/**
	 * Returns the first parent of this node.
	 * 
	 * @return The first parent.
	 */
	public IFacetTerm getFirstParent() {
		if (parents.size() > 0)
			return parents.get(0);
		return null;
	}

	/**
	 * Returns the parent node with index i.
	 * 
	 * @param i
	 *            The index of the parent node to return.
	 * @return The ith parent of this node.
	 */
	public IFacetTerm getParent(int i) {
		return parents.get(i);
	}
	
	public Collection<IFacetTerm> getAllParents() {
		return parents;
	}

	/**
	 * Returns the number of parents for this node.
	 * 
	 * @return The number of parents.
	 */
	public int getNumberOfParents() {
		return parents.size();
	}

	/**
	 * Returns <code>true</code> if this node has at least one parent node.
	 * 
	 * @return true if this node has at least one parent.
	 */
	public boolean hasParent() {
		return parents.size() > 0;
	}

	/**
	 * Returns true if this node contains <code>node</code> as a parent.
	 * 
	 * @param node
	 *            The potential parent node to be tested.
	 * @return true if <code>node</code> is a parent of this node, false
	 *         otherwise.
	 */
	public boolean hasParent(IFacetTerm node) {
		return parents.contains(node);
	}

	/**
	 * Adds <code>childNode</code> to the children of this node.
	 * 
	 * @param child
	 *            New new child node for this node.
	 */
	public void addChild(IFacetTerm child) {
		if (children.contains(child))
			return;

		// The contract is that when a new node is no child of this node, than
		// this node is not yet a parent of the child node. So no security
		// checks needed here.
		children.add(child);
		child.addParent(this);
	}

	/**
	 * Returns the first child of this node.
	 * 
	 * @return The first child.
	 */
	public IFacetTerm getFirstChild() {
		if (children.size() > 0)
			return children.get(0);
		return null;
	}

	/**
	 * Returns the child node with index i.
	 * 
	 * @param i
	 *            The index of the child node to return.
	 * @return The ith child of this node.
	 */
	public IFacetTerm getChild(int i) {
		return children.get(i);
	}

	/**
	 * Returns true if this node contains <code>node</code> as a child.
	 * 
	 * @param node
	 *            The potential child node to be tested.
	 * @return true if <code>node</code> is a child of this node, false
	 *         otherwise.
	 */
	public boolean hasChild(IFacetTerm node) {
		return children.contains(node);
	}

	/**
	 * Returns <code>true</code> if this node has at least one child node.
	 * 
	 * @return true if this node has at least one child.
	 */
	public boolean hasChildren() {
		return children.size() > 0;
	}

	/**
	 * Returns the number of children for this node.
	 * 
	 * @return The number of children.
	 */
	public int getNumberOfChildren() {
		return children.size();
	}
	
	public Iterator<IFacetTerm> childIterator() {
		return children.iterator();
	}
	
}
