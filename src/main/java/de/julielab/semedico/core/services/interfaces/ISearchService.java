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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import de.julielab.elastic.query.SortCriterium;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.UserQuery;
import de.julielab.semedico.core.search.components.data.SemedicoSearchResult;

/**
 * @author faessler
 * 
 */
public interface ISearchService
{
	Future<SemedicoSearchResult> doArticleSearch(
		String documentId,
		String indexType,
		ParseTree highlightingQuery);

	Future<SemedicoSearchResult> doDocumentPagingSearch(
		ParseTree query,
		int startPosition,
		SearchState searchState);

//	Future<SemedicoSearchResult> doDocumentPagingSearchWebservice(
//			UserQuery query,
//			int startPosition,
//			SearchState searchState); // TODO automatisiet hinzugefügt hier noch mal prüfen
//	
////	localhost:8080/webservice?inputstring=mtoractivation&sortcriterium=RELEVANCE&subsetstart=1&subsetend=10&subsetsize=10

	Future<SemedicoSearchResult> doFacetNavigationSearch(
		UIFacet uiFacet,
		ParseTree query,
		UserInterfaceState uiState,
		SearchState searchState);

	Future<SemedicoSearchResult> doFacetNavigationSearch(
		Collection<UIFacet> uiFacets,
		ParseTree query,
		UserInterfaceState uiState,
		SearchState searchState);

	Future<SemedicoSearchResult> doRelatedArticleSearch(
		String relatedDocumentId);

	Future<SemedicoSearchResult> doRetrieveFacetIndexTerms(
		List<Facet> facets);


	Future<SemedicoSearchResult> doSuggestionSearch(
			String fragment,
			List<Facet> facets);

	Future<SemedicoSearchResult> doTabSelectSearch(
		String solrQuery,
		SearchState searchState,
		UserInterfaceState uiState);

	// SemedicoSearchResult doTermSelectSearch(
	// Multimap<String, IFacetTerm> semedicoQuery, String userQuery,
	// SearchState searchState, UserInterfaceState uiState);

	Future<SemedicoSearchResult> doTermSelectSearch(
		ParseTree semedicoQuery,
		SearchState searchState,
		UserInterfaceState uiState);

	Future<SemedicoSearchResult> doNewDocumentSearch(
		UserQuery userQuery,
		Collection<String> searchFields,
		SearchState ss,
		UserInterfaceState uis);

	Future<SemedicoSearchResult> doNewDocumentSearch(
		UserQuery userQuery,
		SearchState searchState,
		UserInterfaceState uiState);

	/**
	 * A method to get the amount of <tt>size</tt> index terms from the field <tt>fieldName</tt> document index in the order specified by <tt>sortOrder</tt>.
	 * @param fieldNames
	 * @param sortOrder
	 * @param size
	 * @return
	 */
	Future<SemedicoSearchResult> doRetrieveFieldTermsByDocScore(
		ParseTree query,
		String fieldName,
		int size);

	Future<SemedicoSearchResult> doDocumentSearchWebservice(
		UserQuery userQuery,
		SortCriterium sortcriterium,
		int startPosition,
		int subsetsize, Collection<String> searchFields,
		SearchState searchState,
		UserInterfaceState uiState);

	Future<SemedicoSearchResult> doDocumentSearchWebservice(
		UserQuery userQuery,
		SortCriterium sortcriterium,
		int startPosition,
		SearchState searchState,
		UserInterfaceState uiState);
}
