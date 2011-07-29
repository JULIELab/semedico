/** 
 * ITermOccurrenceFilterService.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: landefeld
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: 28.07.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core.services;

import java.util.Collection;

import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.MultiHierarchy.IMultiHierarchyNode;

public interface ITermOccurrenceFilterService {

	public Collection<String> filterTermOccurrences(IMultiHierarchyNode term, Collection<String> termOccurrences);
}
