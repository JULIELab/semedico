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

package de.julielab.semedico.core.Taxonomy;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

/**
 * Algorithms for use with <code>MultiHierarchyNode</code>.
 * 
 * @see MultiHierarchyNode
 * @author faessler
 */
abstract public class Taxonomy implements
		ITaxonomy {

	private final Logger logger;

	/**
	 * The roots of this <code>MultiHierarchy</code>, in an unordered fashion.
	 */
	protected Set<IFacetTerm> roots;

	/**
	 * A map making all nodes in the hierarchy available by their unique
	 * identifier.
	 */
	protected Map<String, IFacetTerm> idNodeMap;

	/**
	 * Used to cache paths from a hierarchy root to particular nodes.
	 */
	protected Map<IFacetTerm, IPath> rootPathMap;

	/**
	 * Constructs a new, empty <code>MultiHierarchy</code>.
	 */
	public Taxonomy(Logger logger) {
		this.logger = logger;
		roots = new HashSet<IFacetTerm>();
		idNodeMap = new HashMap<String, IFacetTerm>();
		rootPathMap = new HashMap<IFacetTerm, IPath>();
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
	public void addNode(IFacetTerm node) throws IllegalStateException {
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
	public void addParent(IFacetTerm child, IFacetTerm parent) {
		child.addParent(parent);
		roots.remove(child);
	}

	/**
	 * Returns the roots of this <code>MultiHierarchy</code> in an unordered
	 * fashion.
	 * 
	 * @return The roots of this <code>MultiHierarchy</code>.
	 */
	public Set<IFacetTerm> getRoots() {
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
	public IFacetTerm getNode(String id) {
		IFacetTerm term = idNodeMap.get(id);
		if (term == null)
			logger.warn("Term with ID '{}' is unknown.", id);
		return term;
	}

	public Collection<IFacetTerm> getNodes() {
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
	public synchronized IPath getPathFromRoot(IFacetTerm node) {
		IPath path = rootPathMap.get(node);

		if (path != null) {
			return path;
		}
		try {
			path = new Path();
			path.appendNode(node);
			IFacetTerm parentNode = node;
			while (parentNode.hasParent() && path.length() <= idNodeMap.size()) {
				// The cast is no issue because the parents of a node are always of
				// the same type as the node (see addParent()).
				if (parentNode.equals(parentNode.getFirstParent()))
						throw new IllegalStateException("Node " + node.getId() + " references itself as a parent.");
				parentNode = (IFacetTerm) parentNode.getFirstParent();
				path.appendNode(parentNode);
			}
			path.reverse();
			
			rootPathMap.put(node, new ImmutablePathWrapper(path));

			return path;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean isAncestorOf(IFacetTerm candidate, IFacetTerm term) {
		return getPathFromRoot(term).containsNode(candidate);
	}
}
