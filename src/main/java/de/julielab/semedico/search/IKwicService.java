/** 
 * IKwicService.java
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
 * Creation date: 04.03.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.search;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.SemedicoDocument;

public interface IKwicService {

	public DocumentHit createDocumentHit(SemedicoDocument document, Map<String, Map<String, List<String>>> highlighting);
	public String createHighlightedAbstract(String query, SemedicoDocument document)	throws IOException;
	public String createHighlightedTitle(String query, SemedicoDocument document)	throws IOException;
}
