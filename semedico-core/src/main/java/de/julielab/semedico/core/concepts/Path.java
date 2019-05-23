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
package de.julielab.semedico.core.concepts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.julielab.semedico.core.concepts.interfaces.IPath;

/**
 * @author faessler
 * 
 */
public class Path extends ArrayList<Concept> implements IPath {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6629150357013535498L;
	
	public static final IPath EMPTY_PATH = new ImmutablePathWrapper(new Path());
	
	public Path() {
		super();
	}

	public Path(List<Concept> list) {
		super(list);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.MultiHierarchy.IPath#appendNode()
	 */
	@Override
	public void appendNode(Concept node) {
		add(node);
	}

	/*
	 * (non-Javadoc)�
	 * 
	 * @see de.julielab.semedico.core.MultiHierarchy.IPath#removeLastNode()
	 */
	@Override
	public Concept removeLastNode() {
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
	public Concept getLastNode() {
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
	public boolean containsNode(Concept node) {
		return contains(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.MultiHierarchy.IPath#getNodeAt(int)
	 */
	@Override
	public Concept getNodeAt(int pathItemIndex) {
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

	@Override
	public IPath copyPath() {
		return new Path(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < size(); i++) {
			sb.append(get(i).getId());
			if (i < size() - 1)
				sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}
}
