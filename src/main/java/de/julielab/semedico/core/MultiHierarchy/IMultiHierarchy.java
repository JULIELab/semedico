/**
 * IMultiHierarchy.java
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
 * Creation date: 30.05.2011
 **/

package de.julielab.semedico.core.MultiHierarchy;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Interface for common hierarchy operations.
 * 
 * @author faessler
 */
public interface IMultiHierarchy<T extends MultiHierarchyNode> {
	/**
	 * Returns the <code>MultiHierarchyNode</code> with identifier
	 * <code>id</code>
	 * 
	 * @param id
	 *            The identifier of the node to return.
	 * @return The node with identifier <code>id</code> or <code>null</code> if
	 *         no such node exists.
	 */
	public T getNode(String id);

	/**
	 * Returns all nodes contained in this multi hierarchy.
	 * 
	 * @return All nodes of the hierarchy.
	 */
	public Collection<T> getNodes();

	/**
	 * Returns an ordered list containing all nodes on the leftmost path from a
	 * root to <code>node</code>, including the root and the node itself.
	 * 
	 * @param node
	 *            The node of which the root path should be returned.
	 * @return The leftmost path from a root to <code>node</code.
	 */
	public List<T> getPathFromRoot(T node);
	
	public Set<T> getRoots();
	
	public boolean isAncestorOf(T candidate, T term);
	
	public void addParent(T child, T parent);
}
