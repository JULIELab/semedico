/**
 * LabelMultiHierarchy.java
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
 * Creation date: 27.05.2011
 **/

package de.julielab.semedico.core.MultiHierarchy;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Facet;

/**
 * A <code>MultiHierarchy</code> with some additional algorithms useful when
 * dealing with term count labels.
 * 
 * @author faessler
 */
public class LabelMultiHierarchy extends MultiHierarchy {
	
	protected Multimap<Facet, MultiHierarchyNode> facetRoots;
	
	private void copyHierarchy(MultiHierarchy termHierarchy) {
		
		
	}

}
