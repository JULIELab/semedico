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

/**
 * Interface for common hierarchy operations.
 * @author faessler
 */
public interface IMultiHierarchy {
	/**
	 * Returns the <code>MultiHierarchyNode</code> with identifier
	 * <code>id</code>
	 * 
	 * @param id
	 *            The identifier of the node to return.
	 * @return The node with identifier <code>id</code> or <code>null</code> if
	 *         no such node exists.
	 */
	public MultiHierarchyNode getNode(String id);
}
