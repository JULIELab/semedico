/**
 * IBTermService.java
 *
 * Copyright (c) 2012, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 04.07.2012
 **/

/**
 * 
 */
package de.julielab.semedico.bterms.interfaces;

import java.util.List;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.exceptions.TooFewSearchNodesException;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;

/**
 * @author faessler
 *
 */
public interface IBTermService {
	public List<Label> determineBTermLabelList(List<Multimap<String, IFacetTerm>> searchNodes) throws TooFewSearchNodesException;
}

