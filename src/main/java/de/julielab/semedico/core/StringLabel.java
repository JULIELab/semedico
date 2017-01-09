/**
 * StringLabel.java
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
 * Creation date: 18.08.2011
 **/

/**
 * 
 */
package de.julielab.semedico.core;

import de.julielab.semedico.core.facets.Facet;

/**
 * @author faessler
 *
 */
public class StringLabel extends Label {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2611839392832174339L;
	private String name;

	public StringLabel(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see de.julielab.semedico.core.Label#hasChildHitsInFacet(de.julielab.semedico.core.Facet)
	 */
	@Override
	public boolean hasChildHitsInFacet(Facet facet) {
		return false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getId() {
		return name;
	}

	@Override
	public boolean isTermLabel() {
		return false;
	}

	@Override
	public boolean isStringLabel() {
		return true;
	}

	@Override
	public boolean isMessageLabel() {
		return false;
	}

}

