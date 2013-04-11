/**
 * QueryAnalysisCommand.java
 *
 * Copyright (c) 2013, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 06.04.2013
 **/

/**
 * 
 */
package de.julielab.semedico.search.components;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;

/**
 * @author faessler
 * 
 */
public class SemedicoSearchCommand {
	public Multimap<String, IFacetTerm> semedicoQuery;
	public int documentId;
	public FacetGroup<UIFacet> facetsToCount;
	public SearchNodeSearchCommand nodeCmd;

	public SemedicoSearchCommand() {
		documentId = Integer.MIN_VALUE;
	}
	
	public void addFacetToCount(UIFacet uiFacet) {
		if (null == facetsToCount)
			// We would rather use a plain list but this results in API incompatibility.
			facetsToCount = new FacetGroup<UIFacet>("facetsToCount", -1, false);
		facetsToCount.add(uiFacet);
	}
}
