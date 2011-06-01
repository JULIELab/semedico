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

import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.MultiHierarchy.IMultiHierarchy;
import de.julielab.semedico.core.MultiHierarchy.LabelMultiHierarchy;

public interface ILabelCacheService extends IMultiHierarchy<Label> {


	public LabelMultiHierarchy getCachedHierarchy();
	
	public void releaseHierarchy(LabelMultiHierarchy hierarchy);
}
