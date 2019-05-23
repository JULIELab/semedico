package de.julielab.semedico.services;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import de.julielab.semedico.core.services.SortCriterium;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.search.components.data.SemedicoSearchResult;
import de.julielab.semedico.core.services.interfaces.ISearchService;

/**
 * A helper interface that most of all mirrors {@link ISearchService} but offers the methods without dependencies to
 * state objects. Those are added by the implementation of this interface. Implementations will mostly call
 * <tt>ISearchService</tt> and get the session state objects.
 * 
 * @author faessler
 * 
 */
public interface IStatefulSearchService {
	Future<SemedicoSearchResult> doArticleSearch(String docId, String indexType, ParseTree highlightingQuery);

	Future<SemedicoSearchResult> doDocumentPagingSearch(ParseTree query, int startPosition);

	Future<SemedicoSearchResult> doFacetNavigationSearch(UIFacet uiFacet, ParseTree query);

	Future<SemedicoSearchResult> doFacetNavigationSearch(Collection<UIFacet> uiFacets, ParseTree query);

	Future<SemedicoSearchResult> doTabSelectSearch(String solrQuery);

	Future<SemedicoSearchResult> doTermSelectSearch(ParseTree semedicoQuery, String userQuery);

	Future<SemedicoSearchResult> doNewDocumentSearch(List<QueryToken> userQuery);
	
	Future<SemedicoSearchResult> doDocumentSearchWebservice(
		List<QueryToken> userQuery,
		SortCriterium sortcriterium,
		int startPosition, int subsetsize);
}
