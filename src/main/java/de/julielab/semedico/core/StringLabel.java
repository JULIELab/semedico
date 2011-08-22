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

/**
 * @author faessler
 *
 */
public class StringLabel extends Label {

	public StringLabel(String name) {
		super(name, name);
	}
	
	/* (non-Javadoc)
	 * @see de.julielab.semedico.core.Label#hasChildHits()
	 */
	@Override
	public boolean hasChildHits() {
		return false;
	}

}

