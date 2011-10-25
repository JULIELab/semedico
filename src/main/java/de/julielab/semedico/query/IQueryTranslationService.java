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
import de.julielab.semedico.core.Taxonomy.IFacetTerm;

public interface IQueryTranslationService {

	public String createKwicQueryForTerm(IFacetTerm term, List<String> phrases)
			throws IOException;

	/**
	 * Transforms the disambiguated user query into a corresponding query string
	 * which can be sent to the used search server, e.g. Solr.
	 * <p>
	 * For terms which have been associated with particular parts of the user
	 * input query by {@link QueryDisambiguationService}, a search expression of
	 * the term's ID in all index fields in which this term is to be searched is
	 * created.
	 * <p>
	 * <p>
	 * The user input string <code>rawQuery</code> is necessary to access the
	 * boolean query structure entered by the user.
	 * </p>
	 * 
	 * @param queryTerms
	 *            Disambiguated user query (i.e. user entered query terms mapped
	 *            to facet terms).
	 * @param rawQuery
	 *            User Query without modifications.
	 * @return A search string which can be understood by the used search
	 *         server.
	 */
	public String createQueryFromTerms(Multimap<String, IFacetTerm> queryTerms,
			String rawQuery);

	public String createKwicQueryFromTerms(
			Multimap<String, IFacetTerm> queryTerms);

	/**
	 * @param phraseSlop
	 */
	void setPhraseSlop(int phraseSlop);

}
