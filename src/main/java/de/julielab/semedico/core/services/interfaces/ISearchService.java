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
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import de.julielab.elastic.query.SortCriterium;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.UserQuery;
import de.julielab.semedico.core.search.components.data.ArticleSearchResult;
import de.julielab.semedico.core.search.components.data.DocumentSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SentenceSearchResult;
import de.julielab.semedico.core.services.SearchService.SearchOption;

/**
 * @author faessler
 * 
 */
public interface ISearchService
{
	Future<ArticleSearchResult> doArticleSearch(
		String documentId,
		String indexType,
		Supplier<ParseTree> highlightingQuery);

	Future<SemedicoSearchResult> doDocumentPagingSearch(
		Supplier<ParseTree> query,
		int startPosition,
		SearchState searchState);

	Future<SemedicoSearchResult> doFacetNavigationSearch(
		UIFacet uiFacet,
		Supplier<ParseTree> query,
		UserInterfaceState uiState,
		SearchState searchState);

	Future<SemedicoSearchResult> doFacetNavigationSearch(
		Collection<UIFacet> uiFacets,
		Supplier<ParseTree> query,
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

	Future<SemedicoSearchResult> doTermSelectSearch(
		Supplier<ParseTree> semedicoQuery,
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
		Supplier<ParseTree> query,
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

	Future<DocumentSearchResult> doDocumentSearch(Supplier<ParseTree> parseTree, Collection<String> searchFields,
			SearchState searchState, UserInterfaceState uiState);

	Future<SentenceSearchResult> doSentenceSearch(ParseTree query, SortCriterium sortCriterium,
			EnumSet<SearchOption> searchOptions);
}
