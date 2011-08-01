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
package de.julielab.semedico.core.Taxonomy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author faessler
 * 
 */
public class Path extends ArrayList<IFacetTerm> implements IPath {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Path() {
		super();
	}
	
	public Path(List<IFacetTerm> list) {
		super(list);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.MultiHierarchy.IPath#appendNode()
	 */
	@Override
	public void appendNode(IFacetTerm node) {
		IFacetTerm lastNode = getLastNode();
		if (lastNode != null && lastNode.hasChild(node)
				|| node.hasChild(lastNode))
			add(node);
		else if (lastNode != null)
			throw new IllegalStateException(
					String.format(
							"The nodes %s and %s are not immediately connected in the graph.",
							lastNode.getName(), node.getName()));
		else
			// This path is currently empty, we don't have to check any
			// relationships.
			add(node);
	}

	/*
	 * (non-Javadoc)â
	 * 
	 * @see de.julielab.semedico.core.MultiHierarchy.IPath#removeLastNode()
	 */
	@Override
	public IFacetTerm removeLastNode() {
		if (size() > 0)
			return remove(size() - 1);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.MultiHierarchy.IPath#getLastNode()
	 */
	@Override
	public IFacetTerm getLastNode() {
		if (size() > 0)
			return get(size() - 1);
		return null;
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
	public boolean containsNode(IFacetTerm node) {
		return contains(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.MultiHierarchy.IPath#getNodeAt(int)
	 */
	@Override
	public IFacetTerm getNodeAt(int pathItemIndex) {
		return get(pathItemIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.MultiHierarchy.IPath#subPath(int, int)
	 */
	@Override
	public IPath subPath(int i, int j) {
		return new Path(subList(i, j));
	}
}
