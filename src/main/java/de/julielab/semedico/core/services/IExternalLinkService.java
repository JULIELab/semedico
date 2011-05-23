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

package de.julielab.semedico.core.services;

import java.io.IOException;
import java.util.Collection;

import de.julielab.semedico.core.ExternalLink;
import de.julielab.semedico.core.SemedicoDocument;

public interface IExternalLinkService {

	public void markFullTexts(Collection<SemedicoDocument> hits) throws IOException;
	public Collection<ExternalLink> fetchExternalLinks(Integer pmid) throws IOException;
}
