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

package de.julielab.semedico.core.search.interfaces;

import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.search.components.data.Label;

import java.util.Collection;

public interface ILabelCacheService {

	/**
	 * @param id
	 * @return
	 */
	Label getCachedLabel(String id);

	/**
	 * @param term
	 * @return
	 */
	Label getCachedLabel(Concept term);

	public void releaseLabels(Collection<? extends Label> labels);
}
