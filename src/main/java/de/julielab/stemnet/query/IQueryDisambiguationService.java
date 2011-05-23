/** 
 * IQueryDisambiguationService.java
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

package de.julielab.stemnet.query;

import java.io.IOException;
import java.util.Collection;

import com.google.common.collect.Multimap;

import de.julielab.stemnet.core.FacetTerm;

public interface IQueryDisambiguationService {

	public Multimap<String, FacetTerm> disambiguateQuery(String query, String jsonTerms) throws IOException;
	public Collection<FacetTerm> mapQueryTerm(String queryTerm) throws IOException;
	
}
