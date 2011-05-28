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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Algorithms for use with <code>MultiHierarchyNode</code>.
 * 
 * @see MultiHierarchyNode
 * @author faessler
 */
public class MultiHierarchy {

	/**
	 * The roots of this <code>MultiHierarchy</code>, in an unordered fashion.
	 */
	protected Set<IMultiHierarchyNode> roots;

	/**
	 * A map making all nodes in the hierarchy available by their unique
	 * identifier.
	 */
	protected Map<String, IMultiHierarchyNode> idNodeMap;

	/**
	 * Used to cache paths from a hierarchy root to particular nodes.
	 */
	protected Map<IMultiHierarchyNode, List<IMultiHierarchyNode>> rootPathMap;

	/**
	 * Constructs a new, empty <code>MultiHierarchy</code>.
	 */
	public MultiHierarchy() {
		roots = new HashSet<IMultiHierarchyNode>();
		idNodeMap = new HashMap<String, IMultiHierarchyNode>();
		rootPathMap = new HashMap<IMultiHierarchyNode, List<IMultiHierarchyNode>>();
	}

	/**
	 * Adds the <code>MultiHierarchyNode</code> node to this hierarchy.
	 * 
	 * @param node
	 *            The node to add to this hierarchy.
	 * @throws IllegalStateException
	 *             If there already exists a node with the same ID like
	 *             <code>node</code> in this hierarchy.
	 */
	public void addNode(IMultiHierarchyNode node) throws IllegalStateException {
		// TODO Dafuer sorgen, dass in der DB die Eintraege eindeutig sind und
		// das hier dann wieder einkommentieren.
		// if (idNodeMap.get(node.getId()) != null) {
		// throw new IllegalStateException("A node with ID " + node.getId()
		// + " already exists in this " + getClass().getName());
		// }
		idNodeMap.put(node.getId(), node);
	}

	/**
	 * Returns the roots of this <code>MultiHierarchy</code> in an unordered
	 * fashion.
	 * 
	 * @return The roots of this <code>MultiHierarchy</code>.
	 */
	public Set<IMultiHierarchyNode> getRoots() {
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
	public IMultiHierarchyNode getNode(String id) {
		return idNodeMap.get(id);
	}

	/**
	 * Returns an ordered list containing all nodes on the leftmost path from a
	 * root to <code>node</code>, including the root and the node itself.
	 * 
	 * @param node
	 *            The node of which the root path should be returned.
	 * @return The leftmost path from a root to <code>node</code.
	 */
	public synchronized List<IMultiHierarchyNode> getPathFromRoot(
			IMultiHierarchyNode node) {
		List<IMultiHierarchyNode> path = rootPathMap.get(node);

		if (path != null)
			return path;

		if (!node.hasParent())
			return Collections.emptyList();

		path = new ArrayList<IMultiHierarchyNode>();
		IMultiHierarchyNode parentNode = node;
		path.add(parentNode);
		while (parentNode.hasParent()) {
			parentNode = node.getFirstParent();
			path.add(parentNode);
		}
		Collections.reverse(path);
		rootPathMap.put(node, path);
		return path;
	}

}
