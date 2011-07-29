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
package de.julielab.semedico.core.MultiHierarchy;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author faessler
 * 
 */
public class Path extends ArrayList<IMultiHierarchyNode> implements
		IPath {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.MultiHierarchy.IPath#appendNode()
	 */
	@Override
	public void appendNode(IMultiHierarchyNode node) {
		IMultiHierarchyNode lastNode = getLastNode();
		if (lastNode.hasChild(node) || node.hasChild(lastNode)) 
			add(node);
		
		throw new IllegalStateException(
				String.format(
						"The nodes %s and %s are not immediately connected in the graph.",
						lastNode.getName(), node.getName()));
	}

	/*
	 * (non-Javadoc)â
	 * 
	 * @see de.julielab.semedico.core.MultiHierarchy.IPath#removeLastNode()
	 */
	@Override
	public IMultiHierarchyNode removeLastNode() {
		return remove(size() - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.MultiHierarchy.IPath#getLastNode()
	 */
	@Override
	public IMultiHierarchyNode getLastNode() {
		return get(size() - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.MultiHierarchy.IPath#length()
	 */
	@Override
	public int length() {
		return size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.MultiHierarchy.IPath#reverse()
	 */
	@Override
	public void reverse() {
		Collections.reverse(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.MultiHierarchy.IPath#containsNode(de.julielab
	 * .semedico.core.MultiHierarchy.MultiHierarchyNode)
	 */
	@Override
	public boolean containsNode(IMultiHierarchyNode node) {
		return contains(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.MultiHierarchy.IPath#getNodeAt(int)
	 */
	@Override
	public IMultiHierarchyNode getNodeAt(int pathItemIndex) {
		return get(pathItemIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.MultiHierarchy.IPath#subPath(int, int)
	 */
	@Override
	public IPath subPath(int i, int j) {
		IPath ret = new Path();
		for (int k = i; i <= j; i++)
			ret.appendNode(get(k));
		return ret;
	}
}
