/** 
 * ILabelCacheService.java
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
 * Creation date: 18.12.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.search;

import java.util.Collection;

import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.Label;

public interface ILabelCacheService {

//	public Label getCachedLabel();
	public Label getCachedLabel(FacetTerm term);
	public void releaseLabels(Collection<Label> labels);
}
