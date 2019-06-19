package de.julielab.semedico.services;

import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.search.components.data.SemedicoSearchResult;
import de.julielab.semedico.core.services.SortCriterium;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.state.SemedicoSessionState;
import org.apache.tapestry5.services.ApplicationStateManager;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

public class StatefulSearchService implements IStatefulSearchService {

	private ISearchService searchService;
	private ApplicationStateManager asm;

	public StatefulSearchService(ApplicationStateManager asm, ISearchService searchService) {
		this.asm = asm;
		this.searchService = searchService;
	}

	@Override
	public Future<SemedicoSearchResult> doArticleSearch(String documentId, String indexType,
			ParseTree highlightingQuery) {
		return searchService.doArticleSearch(documentId, indexType, highlightingQuery);
	}

	@Override
	public Future<SemedicoSearchResult> doDocumentPagingSearch(ParseTree query, int startPosition) {
		return searchService.doDocumentPagingSearch(query, startPosition, getSS());
	}

	@Override
	public Future<SemedicoSearchResult> doFacetNavigationSearch(Collection<UIFacet> uiFacets, ParseTree query) {
		return searchService.doFacetNavigationSearch(uiFacets, query, getUIS(), getSS());
	}

	@Override
	public Future<SemedicoSearchResult> doFacetNavigationSearch(UIFacet uiFacet, ParseTree query) {
		return searchService.doFacetNavigationSearch(uiFacet, query, getUIS(), getSS());
	}

	@Override
	public Future<SemedicoSearchResult> doNewDocumentSearch(List<QueryToken> userQuery) {
		return searchService.doNewDocumentSearch(userQuery, getSS(), getUIS());
	}
	
	@Override
	public Future<SemedicoSearchResult> doDocumentSearchWebservice(
		List<QueryToken> userQuery,
		SortCriterium sortcriterium,
		int startPosition, int subsetsize) {
		return searchService.doDocumentSearchWebservice(
			userQuery,
			sortcriterium,
			startPosition, subsetsize,
			Collections.<String> emptySet(),
			getSS(),
			getUIS());
	}

	@Override
	public Future<SemedicoSearchResult> doTabSelectSearch(String solrQuery) {
		return searchService.doTabSelectSearch(solrQuery, getSS(), getUIS());
	}

	@Override
	public Future<SemedicoSearchResult> doTermSelectSearch(ParseTree semedicoQuery, String userQuery) {
		return searchService.doTermSelectSearch(semedicoQuery, getSS(), getUIS());
	}

	private SearchState getSS() {
		return asm.get(SemedicoSessionState.class).getDocumentRetrievalSearchState();
	}

	private UserInterfaceState getUIS() {
		SemedicoSessionState sessionState = asm.get(SemedicoSessionState.class);
		return sessionState.getDocumentRetrievalUiState();
	}

}
