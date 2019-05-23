/**
 * Path.java
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
 * Creation date: 29.07.2011
 **/

/**
 * 
 */
package de.julielab.semedico.core.concepts.interfaces;

import de.julielab.semedico.core.concepts.Concept;


/**
 * An interface for a path of nodes in a graph.
 * <p>
 * A path is an ordered list of nodes which are immediately connected in the
 * graph.
 * </p>
 * 
 * @author faessler
 * 
 */
public interface IPath extends Iterable<Concept> {
	/**
	 * Appends the node <code>node</code> to this path.
	 * <p>
	 * Implementations should check whether the new node is related to the node
	 * it is appended to.
	 * </p>
	 * 
	 * @param node
	 *            The new node to append to this path.
	 * @throws UnsupportedOperationException If the underlying implementation is immutable.
	 */
	public void appendNode(Concept node);

	/**
	 * Removes the end node from this path and returns it.
	 * 
	 * @return The removed (formerly) last node of this path.
	 * @throws UnsupportedOperationException If the underlying implementation is immutable.
	 */
	public Concept removeLastNode();

	/**
	 * Returns the end node of this path.
	 * 
	 * @return
	 */
	public Concept getLastNode();

	/**
	 * Checks whether the node <code>node</code> is on this path.
	 * 
	 * @param node
	 *            The node to check.
	 * @return True if <code>node</code> is on this path, false otherwise.
	 */
	public boolean containsNode(Concept node);

	/**
	 * Reverses the order of nodes. Traversing this path then starts at the
	 * former end node and ends at the former starting node.
	 * @throws IllegalAccessException If the underlying implementation is immutable.
	 */
	public void reverse() throws IllegalAccessException;

	/**
	 * The length of a path is here defined as being the number of nodes on the
	 * path.
	 * 
	 * @return The number of nodes on this path.
	 */
	public int length();

	/**
	 * Removes all nodes from this path.
	 * @throws IllegalAccessException If the underlying implementation is immutable.
	 */
	public void clear() throws IllegalAccessException;

	public boolean isEmpty();

	/**
	 * @param pathItemIndex
	 * @return
	 */
	public Concept getNodeAt(int pathItemIndex);

	/**
	 * @param i
	 * @param j
	 * @return
	 */
	public IPath subPath(int i, int j);
	
	/**
	 * 
	 * @return A copy of this path.
	 */
	public IPath copyPath();
}
