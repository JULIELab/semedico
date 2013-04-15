/**
 * ISearchService.java
 *
 * Copyright (c) 2013, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 09.04.2013
 **/

/**
 * 
 */
package de.julielab.semedico.core.services.interfaces;

import java.util.List;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.search.components.SemedicoSearchResult;

/**
 * @author faessler
 * 
 */
public interface ISearchService {
	public SemedicoSearchResult doArticleSearch(int documentId, String solrQuery);

	public SemedicoSearchResult doFacetNavigationSearch(UIFacet uiFacet,
			String solrQuery);

	public SemedicoSearchResult doIndirectLinkArticleSearch(IFacetTerm selectedLinkTerm, List<Multimap<String, IFacetTerm>> searchNodes,
			int searchNodeIndex);

	public SemedicoSearchResult doIndirectLinksSearch(List<Multimap<String, IFacetTerm>> searchNodes);
	
	public SemedicoSearchResult doNewDocumentSearch(String userQuery,
			String termId, Integer facetId);
	
	public SemedicoSearchResult doSearchNodeSwitchSearch(String solrQuery, Multimap<String, IFacetTerm> semedicoQuery);
	
	public SemedicoSearchResult doTabSelectSearch(String solrQuery);

	public SemedicoSearchResult doTermSelectSearch(
			Multimap<String, IFacetTerm> semedicoQuery, String userQuery);
}
