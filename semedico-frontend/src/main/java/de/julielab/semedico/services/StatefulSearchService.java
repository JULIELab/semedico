package de.julielab.semedico.services;

import de.julielab.elastic.query.SortCriterium;
import de.julielab.semedico.core.entities.state.SearchState;
import de.julielab.semedico.core.entities.state.UserInterfaceState;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;
import de.julielab.semedico.core.search.services.ISearchService;
import de.julielab.semedico.state.SemedicoSessionState;
import org.apache.tapestry5.services.ApplicationStateManager;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @deprecated the statefulness of the search service makes no sense any more since the new search service does not expect state objects
 */
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
//		return searchService.doArticleSearch(documentId, indexType, highlightingQuery);
		return null;
	}

	@Override
	public Future<SemedicoSearchResult> doDocumentPagingSearch(ParseTree query, int startPosition) {
//		return searchService.doDocumentPagingSearch(query, startPosition, getSS());
		return null;
	}

	@Override
	public Future<SemedicoSearchResult> doFacetNavigationSearch(Collection<UIFacet> uiFacets, ParseTree query) {
//		return searchService.doFacetNavigationSearch(uiFacets, query, getUIS(), getSS());
		return null;
	}

	@Override
	public Future<SemedicoSearchResult> doFacetNavigationSearch(UIFacet uiFacet, ParseTree query) {
//		return searchService.doFacetNavigationSearch(uiFacet, query, getUIS(), getSS());
		return null;
	}

	@Override
	public Future<SemedicoSearchResult> doNewDocumentSearch(List<QueryToken> userQuery) {
//		return searchService.doNewDocumentSearch(userQuery, getSS(), getUIS());
		return null;
	}

	@Override
	public Future<SemedicoSearchResult> doDocumentSearchWebservice(List<QueryToken> userQuery, SortCriterium sortcriterium, int startPosition, int subsetsize) {
		return null;
	}


	@Override
	public Future<SemedicoSearchResult> doTabSelectSearch(String solrQuery) {
//		return searchService.doTabSelectSearch(solrQuery, getSS(), getUIS());
		return null;
	}

	@Override
	public Future<SemedicoSearchResult> doTermSelectSearch(ParseTree semedicoQuery, String userQuery) {
//		return searchService.doTermSelectSearch(semedicoQuery, getSS(), getUIS());
		return null;
	}

	private SearchState getSS() {
		return asm.get(SemedicoSessionState.class).getDocumentRetrievalSearchState();
	}

	private UserInterfaceState getUIS() {
		SemedicoSessionState sessionState = asm.get(SemedicoSessionState.class);
		return sessionState.getDocumentRetrievalUiState();
	}

}
