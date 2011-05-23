/** 
 * ITermFileReaderService.java
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
 * Creation date: 23.05.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.stemnet.core.services;

import java.util.List;

import de.julielab.stemnet.core.TermFileEntry;

public interface ITermFileReaderService {

	public abstract boolean hasMoreElements();

	public abstract TermFileEntry nextElement();

	public abstract List<TermFileEntry> sortTopDown(List<TermFileEntry> terms);

	public abstract void resolveRelationships(List<TermFileEntry> terms);

	public void setFacetService(IFacetService facetService);
	public IFacetService getFacetService();
}