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

import java_cup.runtime.Symbol;

import com.aliasi.chunk.Chunker;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.query.QueryDisambiguationService.TermAndPositionWrapper;

public interface IQueryDisambiguationService {

	public Multimap<String, TermAndPositionWrapper> disambiguateQuery(String query, String jsonTerms) throws IOException;
	public Collection<IFacetTerm> mapQueryTerm(String queryTerm) throws IOException;
	public Chunker getChunker();
	public void setChunker(Chunker dictionaryChunker);
	public int getMaxAmbigueTerms() ;
	public void setMaxAmbigueTerms(int maxAmbigueTerms);
	public double getMinMatchingScore();
	public void setMinMatchingScore(double minMatchingScore);
}
