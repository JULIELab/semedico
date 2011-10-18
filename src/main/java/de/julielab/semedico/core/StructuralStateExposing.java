/**
 * StructuralStateExposing.java
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
 * Creation date: 18.10.2011
 **/

/**
 * 
 */
package de.julielab.semedico.core;

/**
 * @author faessler
 *
 */
public interface StructuralStateExposing {
	public boolean isHierarchical();
	public boolean isFlat();
	// Can possibly be removed.
	public Facet.Source getSource();
	public Facet.SourceType getStructureState();
}

