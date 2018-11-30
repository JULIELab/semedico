/** 
 * IExternalLinkService.java
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
 * Creation date: 21.10.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core.services.interfaces;

import java.io.IOException;
import java.util.Collection;

import de.julielab.semedico.core.ExternalLink;
import de.julielab.semedico.core.entities.documents.SemedicoDocument;

public interface IExternalLinkService {

	/**
	 * For logos of fulltext offerers.
	 * @param hits
	 * @throws IOException
	 */
	public void markFullTexts(Collection<SemedicoDocument> hits) throws IOException;
	public Collection<ExternalLink> fetchExternalLinks(String pmid) throws IOException;
}
