/**
 * SearchService.java
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
package de.julielab.semedico.core.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ParallelExecutor;

import com.google.common.collect.Lists;

import de.julielab.semedico.core.services.SortCriterium;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.FieldTermsCommand;
import de.julielab.scicopia.core.elasticsearch.legacy.FieldTermsCommand.OrderType;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchServerCommand;
import de.julielab.scicopia.core.elasticsearch.legacy.AggregationCommand;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.query.translation.SearchTask;
import de.julielab.semedico.core.search.annotations.ArticleChain;
import de.julielab.semedico.core.search.annotations.DocumentChain;
import de.julielab.semedico.core.search.annotations.DocumentPagingChain;
import de.julielab.semedico.core.search.annotations.FacetCountChain;
import de.julielab.semedico.core.search.annotations.FacetIndexTermsChain;
import de.julielab.semedico.core.search.annotations.FieldTermsChain;
import de.julielab.semedico.core.search.annotations.SuggestionsChain;
import de.julielab.semedico.core.search.annotations.TermSelectChain;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCommand;
import de.julielab.semedico.core.search.components.data.SemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SuggestionsSearchCommand;
import de.julielab.semedico.core.services.interfaces.ISearchService;

/**
 * @author faessler
 * 
 */
public class SearchService implements ISearchService {
	private final ISearchComponent documentSearchChain;
	private final ISearchComponent facetCountChain;
	private final ISearchComponent termSelectChain;
	private final ISearchComponent highlightedArticleChain;
	private final ISearchComponent documentPagingChain;
	private ISearchComponent facetIndexTermsChain;
	private ISearchComponent suggestionChain;
	private ISearchComponent fieldTermsChain;
	private ParallelExecutor executor;
	private String documentsIndexName;

	public SearchService(
			ParallelExecutor executor,
			@Symbol(SemedicoSymbolConstants.DOCUMENTS_INDEX_NAME) String documentsIndexName,
			@DocumentChain ISearchComponent documentSearchChain,
			@DocumentPagingChain ISearchComponent documentPagingChain,
			@TermSelectChain ISearchComponent termSelectChain,
			@FacetCountChain ISearchComponent facetCountChain,
			@ArticleChain ISearchComponent highlightedArticleChain,
			@FacetIndexTermsChain ISearchComponent facetIndexTermsChain,
			@FieldTermsChain ISearchComponent fieldTermsChain,
			@SuggestionsChain ISearchComponent suggestionChain) {
		this.executor					= executor;
		this.documentsIndexName = documentsIndexName;
		this.documentSearchChain		= documentSearchChain;
		this.documentPagingChain		= documentPagingChain;
		this.termSelectChain			= termSelectChain;
		this.facetCountChain			= facetCountChain;
		this.highlightedArticleChain	= highlightedArticleChain;
		this.facetIndexTermsChain		= facetIndexTermsChain;
		this.fieldTermsChain			= fieldTermsChain;
		this.suggestionChain			= suggestionChain;
	}

	private Future<SemedicoSearchResult> executeSearchChain(
			final ISearchComponent chain,
			final SemedicoSearchCarrier carrier) {
		return executor.invoke(() -> {
				chain.process(carrier);
				carrier.setElapsedTime();
				return carrier.getResult();	
		});
	}

	@Override
	public Future<SemedicoSearchResult> doArticleSearch(
		String documentId,
		String indexType,
		ParseTree highlightingQuery) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(ArticleChain.class.getSimpleName());
		SemedicoSearchCommand searchCmd	= new SemedicoSearchCommand();
		searchCmd.setDocumentId(documentId);
		searchCmd.setIndex(documentsIndexName);
		
		if (null != indexType) {
			searchCmd.setIndexTypes(Arrays.asList(indexType));
		}
		carrier.setSearchCommand(searchCmd);
		searchCmd.setSemedicoQuery(highlightingQuery);
		searchCmd.setTask(SearchTask.GET_ARTICLE);

		return executeSearchChain(highlightedArticleChain, carrier);
	}

	@Override
	public Future<SemedicoSearchResult> doDocumentPagingSearch(ParseTree query, int startPosition,
			SearchState searchState) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(DocumentPagingChain.class.getSimpleName());
		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.setSemedicoQuery(query);
		searchCmd.setIndex(documentsIndexName);
		searchCmd.setTask(SearchTask.DOCUMENTS);
		carrier.setSearchCommand(searchCmd);
		SearchServerCommand elasticCmd = new SearchServerCommand();
		elasticCmd.start = startPosition;
		carrier.addSearchServerCommand(elasticCmd);
		carrier.setSearchState(searchState);

		return executeSearchChain(documentPagingChain, carrier);
	}

	@Override
	public Future<SemedicoSearchResult> doFacetNavigationSearch(
			Collection<UIFacet> uiFacets,
			ParseTree query,
			UserInterfaceState uiState,
			SearchState searchState) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(FacetCountChain.class.getSimpleName());
		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.setTask(SearchTask.DOCUMENTS);
		searchCmd.setSemedicoQuery(query);
		for (UIFacet uiFacet : uiFacets) {
			searchCmd.addFacetToCount(uiFacet);
		}
		carrier.setSearchCommand(searchCmd);
		carrier.setUiState(uiState);
		carrier.setSearchState(searchState);

		SearchServerCommand elasticCmd = new SearchServerCommand();
		elasticCmd.index = documentsIndexName;
		carrier.addSearchServerCommand(elasticCmd);

		return executeSearchChain(facetCountChain, carrier);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.ISearchService#
	 * doFacetNavigationSearch(de.julielab.semedico.core.UIFacet)
	 */
	@Override
	public Future<SemedicoSearchResult> doFacetNavigationSearch(
			UIFacet uiFacet,
			ParseTree query,
			UserInterfaceState uiState,
			SearchState searchState) {
		return doFacetNavigationSearch(
				Lists.newArrayList(uiFacet),
				query,
				uiState,
				searchState);
	}

	@Override
	public Future<SemedicoSearchResult> doNewDocumentSearch(
			List<QueryToken> userQuery,
			Collection<String> searchFields,
			SearchState searchState,
			UserInterfaceState uiState) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(DocumentChain.class.getSimpleName());

		carrier.setUserQuery(userQuery);
		carrier.setSearchState(searchState);
		carrier.setUiState(uiState);

		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.setTask(SearchTask.DOCUMENTS);
		searchCmd.setIndex(documentsIndexName);
		searchCmd.setSearchFieldFilter(searchFields);
		carrier.setSearchCommand(searchCmd);

		return executeSearchChain(documentSearchChain, carrier);
	}
	
	@Override
	public Future<SemedicoSearchResult> doNewDocumentSearch(List<QueryToken> userQuery, SearchState searchState,
			UserInterfaceState uiState) {
		return doNewDocumentSearch(userQuery, Collections.<String> emptySet(), searchState, uiState);
	}

	@Override
	public Future<SemedicoSearchResult> doDocumentSearchWebservice(
			List<QueryToken> userQuery,
			SortCriterium sortcriterium,
			int startPosition, int subsetsize,
			Collection<String> searchFields,
			SearchState searchState,
			UserInterfaceState uiState) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(DocumentChain.class.getSimpleName());

		carrier.setUserQuery(userQuery);
		carrier.setSearchState(searchState);
		carrier.setUiState(uiState);

		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.setSearchFieldFilter(searchFields);
		searchCmd.setIndex(documentsIndexName);
		searchCmd.setTask(SearchTask.DOCUMENTS);
		carrier.setSearchCommand(searchCmd);
		
		SearchServerCommand serverCmd = new SearchServerCommand();
		serverCmd.start = startPosition;
		serverCmd.rows = subsetsize;
		carrier.addSearchServerCommand(serverCmd);

		return executeSearchChain(documentSearchChain, carrier);
	}

	@Override
	public Future<SemedicoSearchResult> doDocumentSearchWebservice(
		List<QueryToken> userQuery,
		SortCriterium sortcriterium,
		int startPosition,
		SearchState searchState,
		UserInterfaceState uiState) {
		return doDocumentSearchWebservice(
			userQuery,
			sortcriterium,
			startPosition, 10,
			Collections.<String> emptySet(),
			searchState,
			uiState);
	}

	/**
	 * Retrieves ALL index terms for <tt>facets</tt>.
	 */
	@Override
	public Future<SemedicoSearchResult> doRetrieveFacetIndexTerms(List<Facet> facets) {
		SemedicoSearchCarrier carrier				= new SemedicoSearchCarrier(FacetIndexTermsChain.class.getSimpleName());
		SemedicoSearchCommand searchCmd		= new SemedicoSearchCommand();
		searchCmd.setFacetsToGetAllIndexTerms(facets);
		searchCmd.setIndex(documentsIndexName);
		carrier.setSearchCommand(searchCmd);

		return executeSearchChain(facetIndexTermsChain, carrier);
	}

	@Override
	public Future<SemedicoSearchResult> doSuggestionSearch(String fragment, List<Facet> facets) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(SuggestionsChain.class.getSimpleName());
		SuggestionsSearchCommand suggCmd = new SuggestionsSearchCommand();
		suggCmd.fragment = fragment;
		suggCmd.facets = facets;
		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.setSuggestionsCommand(suggCmd);
		carrier.setSearchCommand(searchCmd);

		return executeSearchChain(suggestionChain, carrier);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.ISearchService#
	 * doTabSelectSearch()
	 */
	@Override
	public Future<SemedicoSearchResult> doTabSelectSearch(
		String elasticQuery,
		SearchState searchState,
		UserInterfaceState uiState) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(FacetCountChain.class.getSimpleName());
		SearchServerCommand elasticCmd = new SearchServerCommand();
		elasticCmd.index = documentsIndexName;
		carrier.addSearchServerCommand(elasticCmd);
		carrier.setSearchState(searchState);
		carrier.setUiState(uiState);

		return executeSearchChain(facetCountChain, carrier);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.ISearchService#
	 * doTermSelectSearch(com.google.common.collect.Multimap)
	 */
	@Override
	public Future<SemedicoSearchResult> doTermSelectSearch(
		ParseTree semedicoQuery,
		SearchState searchState,
		UserInterfaceState uiState) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(TermSelectChain.class.getSimpleName());
		SemedicoSearchCommand searchCmd	= new SemedicoSearchCommand();
		searchCmd.setTask(SearchTask.DOCUMENTS);
		searchCmd.setIndex(documentsIndexName);
		searchCmd.setSemedicoQuery(semedicoQuery);
		carrier.setSearchCommand(searchCmd);
		carrier.setSearchState(searchState);
		carrier.setUiState(uiState);

		return executeSearchChain(termSelectChain, carrier);
	}

	@Override
	public Future<SemedicoSearchResult> doRetrieveFieldTermsByDocScore(
		ParseTree query,
		String fieldName,
		int size) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(FieldTermsChain.class.getSimpleName());
		
		FieldTermsCommand fieldTermsCmd = new FieldTermsCommand();
		fieldTermsCmd.field = fieldName;
		fieldTermsCmd.size = size;
		
		fieldTermsCmd.orderTypes = new OrderType[] {
			FieldTermsCommand.OrderType.DOC_SCORE,
			FieldTermsCommand.OrderType.COUNT
		};
		
		fieldTermsCmd.sortOrders
			= new AggregationCommand.OrderCommand.SortOrder[] {
			AggregationCommand.OrderCommand.SortOrder.DESCENDING,
			AggregationCommand.OrderCommand.SortOrder.DESCENDING
		};
		
		SemedicoSearchCommand searchCommand = new SemedicoSearchCommand();
		searchCommand.setIndex(documentsIndexName);
		searchCommand.setSemedicoQuery(query);
		searchCommand.setFieldTermsCommand(fieldTermsCmd);
		carrier.setSearchCommand(searchCommand);

		return executeSearchChain(fieldTermsChain, carrier);
	}
	
}