/** 
 * IDocumentCacheService.java
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

package de.julielab.semedico.core.services.interfaces;

import de.julielab.semedico.core.search.components.data.SemedicoDocument;

public interface IDocumentCacheService {
	
	public SemedicoDocument getCachedDocument(int docId);
	public void addDocument(SemedicoDocument document);
}
