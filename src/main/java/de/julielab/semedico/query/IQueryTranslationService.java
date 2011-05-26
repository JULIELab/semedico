/** 
 * IQueryTranslationService.java
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
 **/

package de.julielab.semedico.query;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.FacetTerm;

public interface IQueryTranslationService {


	/**
	 * Transforms the disambiguated user query into a corresponding query string which can be sent to the used search server, e.g. Solr.
	 * @param queryTerms Disambiguated user query (i.e. user entered query terms mapped to facet terms).
	 * @return A search string which can be understood by the used search server.
	 */
	public String createQueryFromTerms(Multimap<String, FacetTerm> queryTerms);

	public String createKwicQueryFromTerms(Multimap<String, FacetTerm> queryTerms);
	
	public String createKwicQueryForTerm(FacetTerm term, List<String> phrases) throws IOException;

}
