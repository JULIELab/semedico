/**
 * ImmutablePath.java
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
 * Creation date: 27.10.2011
 **/

/**
 * 
 */
package de.julielab.semedico.core.concepts;

import de.julielab.semedico.core.concepts.interfaces.IPath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author faessler
 * 
 */
public class ImmutablePathWrapper implements IPath {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String IMMUTABLE_MSG = "This path is immutable.";

	private IPath path;

	public ImmutablePathWrapper(IPath path) {
		this.path = path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Concept> iterator() {
		return path.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.Taxonomy.IPath#appendNode(de.julielab.semedico
	 * .core.Taxonomy.IFacetTerm)
	 */
	@Override
	public void appendNode(Concept node) {
		throw new UnsupportedOperationException(IMMUTABLE_MSG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.Taxonomy.IPath#removeLastNode()
	 */
	@Override
	public Concept removeLastNode() {
		throw new UnsupportedOperationException(IMMUTABLE_MSG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.Taxonomy.IPath#getLastNode()
	 */
	@Override
	public Concept getLastNode() {
		return path.getLastNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.Taxonomy.IPath#containsNode(de.julielab.semedico
	 * .core.Taxonomy.IFacetTerm)
	 */
	@Override
	public boolean containsNode(Concept node) {
		return path.containsNode(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.Taxonomy.IPath#reverse()
	 */
	@Override
	public void reverse() throws IllegalAccessException {
		throw new IllegalAccessException(IMMUTABLE_MSG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.Taxonomy.IPath#length()
	 */
	@Override
	public int length() {
		return path.length();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.Taxonomy.IPath#clear()
	 */
	@Override
	public void clear() throws IllegalAccessException {
		throw new IllegalAccessException(IMMUTABLE_MSG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.Taxonomy.IPath#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return path.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.Taxonomy.IPath#getNodeAt(int)
	 */
	@Override
	public Concept getNodeAt(int pathItemIndex) {
		return path.getNodeAt(pathItemIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.Taxonomy.IPath#subPath(int, int)
	 */
	@Override
	public IPath subPath(int i, int j) {
		return path.subPath(i, j);
	}

	/**
	 * Copies the underlying <code>Path</code> object and returns it. Please
	 * note that the returned object is <em>not</em> an instance of
	 * <code>ImmutablePathWrapper</code> but of <code>Path</code> path itself.
	 * 
	 * @return A (mutable) copy of the <code>Path</code> object underlying this
	 *         <code>ImmutablePathWrapper</code>.
	 * @see de.julielab.semedico.core.taxonomy.interfaces.Taxonomy.IPath#copyPath()
	 */
	@Override
	public IPath copyPath() {
		return path.copyPath();
	}

	@Override
	public String toString() {
		List<String> termIds = new ArrayList<>();
		for (IConcept term : this)
			termIds.add(term.getId());
		return termIds.toString();
	}
	
	

}
