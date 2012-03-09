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

import java.util.List;
import java.util.Map;

public interface IKwicService {

	String getHighlightedTitle(Map<String, List<String>> docHighlights);

	String getHighlightedAbstract(Map<String, List<String>> docHighlights,
			int pmid);

	String[] getAbstractHighlights(Map<String, List<String>> docHighlights);
}
