/**
 * MultiHierarchy.java
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

package de.julielab.semedico.core.MultiHierarchy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

/**
 * Algorithms for use with <code>MultiHierarchyNode</code>.
 * 
 * @see MultiHierarchyNode
 * @author faessler
 */
abstract public class MultiHierarchy<T extends MultiHierarchyNode> implements
		IMultiHierarchy<T> {

	/**
	 * The roots of this <code>MultiHierarchy</code>, in an unordered fashion.
	 */
	protected Set<T> roots;

	/**
	 * A map making all nodes in the hierarchy available by their unique
	 * identifier.
	 */
	protected Map<String, T> idNodeMap;

	/**
	 * Used to cache paths from a hierarchy root to particular nodes.
	 */
	protected Map<T, IPath<T>> rootPathMap;

	/**
	 * Constructs a new, empty <code>MultiHierarchy</code>.
	 */
	public MultiHierarchy() {
		roots = new HashSet<T>();
		idNodeMap = new HashMap<String, T>();
		rootPathMap = new HashMap<T, IPath<T>>();
	}

	/**
	 * Adds the <code>MultiHierarchyNode</code> node to this hierarchy.
	 * <p>
	 * The new node is initially unconnected with any other node.<br/>
	 * To connect the node with other, already existing nodes in the hierarchy,
	 * use {@link #addParent(MultiHierarchyNode, MultiHierarchyNode)}.
	 * </p>
	 * 
	 * @param node
	 *            The node to add to this hierarchy.
	 * @throws IllegalStateException
	 *             If there already exists a node with the same ID like
	 *             <code>node</code> in this hierarchy.
	 */
	public void addNode(T node) throws IllegalStateException {
		// TODO Dafuer sorgen, dass in der DB die Eintraege eindeutig sind und
		// das hier dann wieder einkommentieren.
		// if (idNodeMap.get(node.getId()) != null) {
		// throw new IllegalStateException("A node with ID " + node.getId()
		// + " already exists in this " + getClass().getName());
		// }
		idNodeMap.put(node.getId(), node);
		roots.add(node);
	}

	/**
	 * Adds the node <code>parent</code> as a parent to the node
	 * <code>child</code>.
	 * 
	 * @param child
	 *            The node to get <code>parent</code> as a parent node.
	 * @param parent
	 */
	public void addParent(T child, T parent) {
		child.addParent(parent);
		roots.remove(child);
	}

	/**
	 * Returns the roots of this <code>MultiHierarchy</code> in an unordered
	 * fashion.
	 * 
	 * @return The roots of this <code>MultiHierarchy</code>.
	 */
	public Set<T> getRoots() {
		return roots;
	}

	/**
	 * Returns the <code>MultiHierarchyNode</code> with identifier
	 * <code>id</code>
	 * 
	 * @param id
	 *            The identifier of the node to return.
	 * @return The node with identifier <code>id</code> or <code>null</code> if
	 *         no such node exists.
	 */
	public T getNode(String id) {
		return idNodeMap.get(id);
	}

	public Collection<T> getNodes() {
		return idNodeMap.values();
	}

	public boolean hasNode(String id) {
		return idNodeMap.get(id) != null;
	}

	/**
	 * Returns an ordered list containing all nodes on the leftmost path from a
	 * root to <code>node</code>, including the root and the node itself.
	 * 
	 * @param node
	 *            The node of which the root path should be returned.
	 * @return The leftmost path from a root to <code>node</code.
	 */
	@SuppressWarnings("unchecked")
	public synchronized IPath<T> getPathFromRoot(T node) {
		IPath<T> path = rootPathMap.get(node);

		// Of source, path.size() should never be zero. But there have been
		// issues with hot/auto-deploying when the paths would be cleared but
		// not set to null.
		if (path != null && path.length() != 0) {
			return path;
		}
		path = new Path<T>();
		path.appendNode(node);
		T parentNode = node;
		while (parentNode.hasParent() && path.length() <= idNodeMap.size()) {
			// The cast is no issue because the parents of a node are always of
			// the same type as the node (see addParent()).
			if (parentNode.getFirstParent().getId().equals(parentNode.getId()))
					throw new IllegalStateException("Node " + node.getId() + " references itself as a parent.");
			parentNode = (T) parentNode.getFirstParent();
			path.appendNode(parentNode);
		}
		path.reverse();
		
		rootPathMap.put(node, path);
		for (T pathNode : path)
			System.out.println(pathNode.getName());
		return path;
	}
	
	public boolean isAncestorOf(T candidate, T term) {
		return getPathFromRoot(term).containsNode(candidate);
	}
}
