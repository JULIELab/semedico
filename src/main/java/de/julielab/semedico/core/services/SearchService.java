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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ParallelExecutor;

import com.google.common.collect.Lists;

import de.julielab.elastic.query.SortCriterium;
import de.julielab.elastic.query.components.ISearchComponent;
import de.julielab.elastic.query.components.data.FieldTermsCommand;
import de.julielab.elastic.query.components.data.FieldTermsCommand.OrderType;
import de.julielab.elastic.query.components.data.SearchServerCommand;
import de.julielab.elastic.query.components.data.aggregation.AggregationCommand;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.ArticleQuery;
import de.julielab.semedico.core.query.DocumentQuery;
import de.julielab.semedico.core.query.FieldTermsQuery;
import de.julielab.semedico.core.query.ISemedicoQuery;
import de.julielab.semedico.core.query.ParseTreeQueryBase;
import de.julielab.semedico.core.query.UserQuery;
import de.julielab.semedico.core.query.WrappingQuery;
import de.julielab.semedico.core.query.translation.SearchTask;
import de.julielab.semedico.core.search.annotations.ArticleChain;
import de.julielab.semedico.core.search.annotations.DocumentChain;
import de.julielab.semedico.core.search.annotations.DocumentPagingChain;
import de.julielab.semedico.core.search.annotations.FacetCountChain;
import de.julielab.semedico.core.search.annotations.FacetIndexTermsChain;
import de.julielab.semedico.core.search.annotations.FieldTermsChain;
import de.julielab.semedico.core.search.annotations.StatementSearchChain;
import de.julielab.semedico.core.search.annotations.SuggestionsChain;
import de.julielab.semedico.core.search.annotations.TermSelectChain;
import de.julielab.semedico.core.search.components.QueryAnalysisCommand;
import de.julielab.semedico.core.search.components.data.ArticleSearchResult;
import de.julielab.semedico.core.search.components.data.DocumentSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCommand;
import de.julielab.semedico.core.search.components.data.SemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SentenceSearchResult;
import de.julielab.semedico.core.search.components.data.StatementSearchResult;
import de.julielab.semedico.core.search.components.data.SuggestionsSearchCommand;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.ISearchService;

/**
 * @author faessler
 * 
 */
public class SearchService implements ISearchService {

	public enum SearchOption {
		/**
		 * Create a query that does not return any stored fields or
		 * aggregations, just count the number of hits as quickly as possible.
		 * Corresponds to {@link #NO_FIELDS}, {@link #NO_AGGREGATIONS} and {@link #NO_HIGHLIGHTING}.
		 */
		HIT_COUNT,
		/**
		 * Build the full query with all requested fields, aggregations and
		 * everything else.
		 */
		FULL,
		/**
		 * Build the query but do not issue it. Useful for {@link WrappingQuery}
		 * to get the desired query (e.g. {@link FieldTermsQuery}).
		 */
		RETURN_SERVER_QUERY,
		/**
		 * Do not return stored fields from a query.
		 */
		NO_FIELDS,
		/**
		 * Do not create aggregations for a query.
		 */
		NO_AGGREGATIONS,
		NO_HIGHLIGHTING
	}

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
	private ISearchComponent statementSearch;

	public SearchService(ParallelExecutor executor,
			@Symbol(SemedicoSymbolConstants.BIOMED_PUBLICATIONS_INDEX_NAME) String documentsIndexName,
			@DocumentChain ISearchComponent documentSearchChain,
			@DocumentPagingChain ISearchComponent documentPagingChain,
			@TermSelectChain ISearchComponent termSelectChain, @FacetCountChain ISearchComponent facetCountChain,
			@ArticleChain ISearchComponent highlightedArticleChain,
			@FacetIndexTermsChain ISearchComponent facetIndexTermsChain,
			@FieldTermsChain ISearchComponent fieldTermsChain, @SuggestionsChain ISearchComponent suggestionChain,
			@StatementSearchChain ISearchComponent statementSearch) {
		this.executor = executor;
		this.documentsIndexName = documentsIndexName;
		this.documentSearchChain = documentSearchChain;
		this.documentPagingChain = documentPagingChain;
		this.termSelectChain = termSelectChain;
		this.facetCountChain = facetCountChain;
		this.highlightedArticleChain = highlightedArticleChain;
		this.facetIndexTermsChain = facetIndexTermsChain;
		this.fieldTermsChain = fieldTermsChain;
		this.suggestionChain = suggestionChain;
		this.statementSearch = statementSearch;
	}

	private <S extends ISemedicoQuery, T extends SemedicoSearchResult> Future<T> executeSearchChain(
			final ISearchComponent chain, final SemedicoSearchCarrier<S, T> carrier) {
		return executor.invoke(new Invokable<T>() {
			@Override
			public T invoke() {
				chain.process(carrier);
				carrier.setElapsedTime();
				return carrier.result;
			}
		});
	}

	@Override
	public Future<ArticleSearchResult> doArticleSearch(String documentId, String indexType,
			Supplier<ParseTree> highlightingQuery) {
		SemedicoSearchCarrier<ArticleQuery, ArticleSearchResult> carrier = new SemedicoSearchCarrier<>(
				ArticleChain.class.getSimpleName());
		carrier.query = new ArticleQuery(documentId);
		carrier.query.setIndex(documentsIndexName);
		carrier.query.setIndexTypes(Arrays.asList(indexType));
		if (highlightingQuery != null)
			carrier.query.setQuery(highlightingQuery.get());

		return executeSearchChain(highlightedArticleChain, carrier);
	}

	@Override
	public Future<SemedicoSearchResult> doDocumentPagingSearch(Supplier<ParseTree> query, int startPosition,
			SearchState searchState) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(DocumentPagingChain.class.getSimpleName());
		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.semedicoQuery = query.get();
		searchCmd.index = documentsIndexName;
		searchCmd.task = SearchTask.DOCUMENTS;
		carrier.searchCmd = searchCmd;
		SearchServerCommand solrCmd = new SearchServerCommand();
		// solrCmd.serverQuery = solrQuery;
		solrCmd.start = startPosition;
		carrier.addSearchServerCommand(solrCmd);
		carrier.searchState = searchState;

		return executeSearchChain(documentPagingChain, carrier);

		// documentPagingChain.process(carrier);
		//
		// carrier.setElapsedTime();
		// SemedicoSearchResult searchResult = carrier.searchResult;
		// return searchResult;
	}

	@Override
	public Future<SemedicoSearchResult> doFacetNavigationSearch(Collection<UIFacet> uiFacets, Supplier<ParseTree> query,
			UserInterfaceState uiState, SearchState searchState) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(FacetCountChain.class.getSimpleName());
		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.task = SearchTask.DOCUMENTS;
		searchCmd.semedicoQuery = query.get();
		for (UIFacet uiFacet : uiFacets) {
			searchCmd.addFacetToCount(uiFacet);
		}
		carrier.searchCmd = searchCmd;
		carrier.uiState = uiState;
		carrier.searchState = searchState;

		SearchServerCommand solrCmd = new SearchServerCommand();
		solrCmd.index = documentsIndexName;
		// solrCmd.serverQuery = solrQuery;
		carrier.addSearchServerCommand(solrCmd);

		return executeSearchChain(facetCountChain, carrier);

		// facetCountChain.process(carrier);
		//
		// carrier.setElapsedTime();
		// SemedicoSearchResult searchResult = new
		// SemedicoSearchResult(carrier.searchCmd.semedicoQuery);
		//
		// return searchResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.ISearchService#
	 * doFacetNavigationSearch(de.julielab.semedico.core.UIFacet)
	 */
	@Override
	public Future<SemedicoSearchResult> doFacetNavigationSearch(UIFacet uiFacet, Supplier<ParseTree> query,
			UserInterfaceState uiState, SearchState searchState) {
		return doFacetNavigationSearch(Lists.newArrayList(uiFacet), query, uiState, searchState);
	}

	@Override
	public Future<DocumentSearchResult> doDocumentSearch(Supplier<ParseTree> parseTree, Collection<String> searchFields,
			SearchState searchState, UserInterfaceState uiState) {
		DocumentQuery query = new DocumentQuery(parseTree.get(), new HashSet<>(searchFields));
		query.setIndex(documentsIndexName);

		SemedicoSearchCarrier<DocumentQuery, DocumentSearchResult> carrier = new SemedicoSearchCarrier<>(
				DocumentChain.class.getSimpleName());
		carrier.query = query;
		carrier.query.setIndex(documentsIndexName);
		carrier.query.setIndexTypes(Arrays.asList(IIndexInformationService.Indexes.DocumentTypes.medline));
		carrier.searchState = searchState;
		carrier.uiState = uiState;

		Future<DocumentSearchResult> executeSearchChain = executeSearchChain(documentSearchChain, carrier);

		return executeSearchChain;
	}

	@Override
	public Future<SemedicoSearchResult> doNewDocumentSearch(UserQuery userQuery, Collection<String> searchFields,
			SearchState searchState, UserInterfaceState uiState) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(DocumentChain.class.getSimpleName());
		QueryAnalysisCommand queryCmd = new QueryAnalysisCommand();
		queryCmd.userQuery = userQuery;
		// queryCmd.selectedTermId = termId;
		// if (facetId != null)
		// queryCmd.facetIdForSelectedTerm = facetId;
		// queryCmd.eventQueries = eventQueries;
		carrier.queryAnalysisCmd = queryCmd;
		carrier.searchState = searchState;
		carrier.uiState = uiState;

		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.task = SearchTask.DOCUMENTS;
		searchCmd.index = documentsIndexName;
		searchCmd.searchFieldFilter = searchFields;
		carrier.searchCmd = searchCmd;

		return executeSearchChain(documentSearchChain, carrier);

		// documentSearchChain.process(carrier);
		//
		// carrier.setElapsedTime();
		// SemedicoSearchResult searchResult = carrier.searchResult;
		//
		// return searchResult;
	}

	@Override
	public Future<SemedicoSearchResult> doNewDocumentSearch(UserQuery userQuery, SearchState searchState,
			UserInterfaceState uiState) {
		return doNewDocumentSearch(userQuery, Collections.<String>emptySet(), searchState, uiState);
	}

	@Override
	public Future<SemedicoSearchResult> doDocumentSearchWebservice(UserQuery userQuery, SortCriterium sortcriterium,
			int startPosition, int subsetsize, Collection<String> searchFields, SearchState searchState,
			UserInterfaceState uiState) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(DocumentChain.class.getSimpleName());
		QueryAnalysisCommand queryCmd = new QueryAnalysisCommand();
		queryCmd.userQuery = userQuery;
		// queryCmd.selectedTermId = termId;
		// if (facetId != null)
		// queryCmd.facetIdForSelectedTerm = facetId;
		// queryCmd.eventQueries = eventQueries;
		carrier.queryAnalysisCmd = queryCmd;
		carrier.searchState = searchState;
		carrier.uiState = uiState;

		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.searchFieldFilter = searchFields;
		searchCmd.index = documentsIndexName;
		searchCmd.task = SearchTask.DOCUMENTS;
		carrier.searchCmd = searchCmd;

		SearchServerCommand serverCmd = new SearchServerCommand();
		serverCmd.start = startPosition;
		serverCmd.rows = subsetsize;
		carrier.addSearchServerCommand(serverCmd);

		return executeSearchChain(documentSearchChain, carrier);

		// documentSearchChain.process(carrier);
		//
		// carrier.setElapsedTime();
		// SemedicoSearchResult searchResult = carrier.searchResult;
		//
		// return searchResult;
	}

	@Override
	public Future<SemedicoSearchResult> doDocumentSearchWebservice(UserQuery userQuery, SortCriterium sortcriterium,
			int startPosition, SearchState searchState, UserInterfaceState uiState) {
		return doDocumentSearchWebservice(userQuery, sortcriterium, startPosition, 10, Collections.<String>emptySet(),
				searchState, uiState);
	}

	@Override
	public Future<SemedicoSearchResult> doRelatedArticleSearch(String relatedDocumentId) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(ArticleChain.class.getSimpleName());
		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.task = SearchTask.DOCUMENTS;
		searchCmd.documentId = relatedDocumentId;
		searchCmd.index = documentsIndexName;
		carrier.searchCmd = searchCmd;
		SearchServerCommand serverCmd = new SearchServerCommand();
		serverCmd.addField(IIndexInformationService.TITLE);
		serverCmd.addField(IIndexInformationService.ABSTRACT);
		serverCmd.addField(IIndexInformationService.PUBLICATION_TYPES);
		serverCmd.addField(IIndexInformationService.PUBMED_ID);
		serverCmd.addField(IIndexInformationService.TITLE);
		serverCmd.addField(IIndexInformationService.DATE);
		carrier.addSearchServerCommand(serverCmd);

		return executeSearchChain(highlightedArticleChain, carrier);

		// highlightedArticleChain.process(carrier);
		//
		// carrier.setElapsedTime();
		// SemedicoSearchResult searchResult = carrier.searchResult;
		// return searchResult;
	}

	/**
	 * Retrieves ALL index terms for <tt>facets</tt>.
	 */
	@Override
	public Future<SemedicoSearchResult> doRetrieveFacetIndexTerms(List<Facet> facets) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(FacetIndexTermsChain.class.getSimpleName());
		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.facetsToGetAllIndexTerms = facets;
		searchCmd.index = documentsIndexName;
		carrier.searchCmd = searchCmd;

		return executeSearchChain(facetIndexTermsChain, carrier);

		// facetIndexTermsChain.process(carrier);
		//
		// carrier.setElapsedTime();
		// SemedicoSearchResult searchResult = carrier.searchResult;
		//
		// return searchResult;
	}

	@Override
	public Future<SemedicoSearchResult> doSuggestionSearch(String fragment, List<Facet> facets) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(SuggestionsChain.class.getSimpleName());
		SuggestionsSearchCommand suggCmd = new SuggestionsSearchCommand();
		suggCmd.fragment = fragment;
		suggCmd.facets = facets;
		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.suggCmd = suggCmd;
		carrier.searchCmd = searchCmd;

		return executeSearchChain(suggestionChain, carrier);

		// suggestionChain.process(carrier);
		//
		// carrier.setElapsedTime();
		// SemedicoSearchResult searchResult = carrier.searchResult;
		//
		// return searchResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.ISearchService#
	 * doTabSelectSearch()
	 */
	@Override
	public Future<SemedicoSearchResult> doTabSelectSearch(String solrQuery, SearchState searchState,
			UserInterfaceState uiState) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(FacetCountChain.class.getSimpleName());
		SearchServerCommand solrCmd = new SearchServerCommand();
		solrCmd.index = documentsIndexName;
		// solrCmd.serverQuery = solrQuery;
		carrier.addSearchServerCommand(solrCmd);
		carrier.searchState = searchState;
		carrier.uiState = uiState;

		return executeSearchChain(facetCountChain, carrier);

		// facetCountChain.process(carrier);
		//
		// carrier.setElapsedTime();
		// SemedicoSearchResult searchResult = new
		// SemedicoSearchResult(carrier.searchCmd.semedicoQuery);
		//
		// return searchResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.ISearchService#
	 * doTermSelectSearch(com.google.common.collect.Multimap)
	 */
	@Override
	public Future<SemedicoSearchResult> doTermSelectSearch(Supplier<ParseTree> semedicoQuery, SearchState searchState,
			UserInterfaceState uiState) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(TermSelectChain.class.getSimpleName());
		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.task = SearchTask.DOCUMENTS;
		searchCmd.index = documentsIndexName;
		searchCmd.semedicoQuery = semedicoQuery.get();
		carrier.searchCmd = searchCmd;
		carrier.searchState = searchState;
		carrier.uiState = uiState;

		// TODO the queryAnalysisCmd can be omitted completely since the
		// semedicoQuery is used, can't it??
		QueryAnalysisCommand queryAnalysisCmd = new QueryAnalysisCommand();
		// queryCmd.userQuery = userQuery;
		carrier.queryAnalysisCmd = queryAnalysisCmd;

		return executeSearchChain(termSelectChain, carrier);

		// termSelectChain.process(carrier);
		//
		// carrier.setElapsedTime();
		// SemedicoSearchResult searchResult = carrier.searchResult;
		//
		// return searchResult;
	}

	@Override
	public Future<SemedicoSearchResult> doRetrieveFieldTermsByDocScore(Supplier<ParseTree> query, String fieldName,
			int size) {
		SemedicoSearchCarrier carrier = new SemedicoSearchCarrier(FieldTermsChain.class.getSimpleName());
		carrier.searchCmd = new SemedicoSearchCommand();
		carrier.searchCmd.index = documentsIndexName;
		carrier.searchCmd.semedicoQuery = query.get();
		carrier.searchCmd.fieldTermsCmd = new FieldTermsCommand();
		carrier.searchCmd.fieldTermsCmd.field = fieldName;
		carrier.searchCmd.fieldTermsCmd.size = size;

		carrier.searchCmd.fieldTermsCmd.orderTypes = new OrderType[] { FieldTermsCommand.OrderType.DOC_SCORE,
				FieldTermsCommand.OrderType.COUNT };

		carrier.searchCmd.fieldTermsCmd.sortOrders = new AggregationCommand.OrderCommand.SortOrder[] {
				AggregationCommand.OrderCommand.SortOrder.DESCENDING,
				AggregationCommand.OrderCommand.SortOrder.DESCENDING };

		return executeSearchChain(fieldTermsChain, carrier);

		// fieldTermsChain.process(carrier);
		//
		// carrier.setElapsedTime();
		// SemedicoSearchResult searchResult = carrier.searchResult;
		//
		// return searchResult;
	}

	public Future<StatementSearchResult> doStatementSearch(ParseTree query, SortCriterium sortCriterium,
			EnumSet<SearchOption> searchOptions) {
		SemedicoSearchCarrier<ParseTreeQueryBase, StatementSearchResult> carrier = new SemedicoSearchCarrier<>(
				"Statements");
		carrier.query = new ParseTreeQueryBase(SearchTask.STATEMENTS);
		carrier.query.setQuery(query);
		carrier.query.setIndex(documentsIndexName);
		carrier.query.setIndexTypes(Arrays.asList(IIndexInformationService.Indexes.DocumentTypes.medline));
		carrier.query.setSearchOptions(searchOptions);
		return executeSearchChain(statementSearch, carrier);
	}

	@Override
	public Future<SentenceSearchResult> doSentenceSearch(ParseTree query, SortCriterium sortCriterium,
			EnumSet<SearchOption> searchOptions) {
		SemedicoSearchCarrier<ParseTreeQueryBase, SentenceSearchResult> carrier = new SemedicoSearchCarrier<>(
				"Sentences");
		carrier.query = new ParseTreeQueryBase(SearchTask.SENTENCES);
		carrier.query.setQuery(query);
		carrier.query.setIndex(documentsIndexName);
		carrier.query.setIndexTypes(Arrays.asList(IIndexInformationService.Indexes.DocumentTypes.medline));
		carrier.query.setSearchOptions(searchOptions);

		return executeSearchChain(statementSearch, carrier);
	}

}