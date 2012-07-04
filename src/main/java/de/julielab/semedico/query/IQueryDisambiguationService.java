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

package de.julielab.semedico.query;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;

import com.aliasi.chunk.Chunker;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.taxonomy.IFacetTerm;

public interface IQueryDisambiguationService {

	public Multimap<String, IFacetTerm> disambiguateQuery(String query, Pair<String,String> termIdAndFacetId);
	public Collection<IFacetTerm> mapQueryTerm(String queryTerm) throws IOException;
}
