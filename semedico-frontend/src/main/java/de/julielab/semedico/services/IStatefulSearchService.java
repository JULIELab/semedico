package de.julielab.semedico.services;

import de.julielab.elastic.query.SortCriterium;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.search.results.SemedicoESSearchResult;
import de.julielab.semedico.core.search.services.ISearchService;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

/**
 * A helper interface that most of all mirrors {@link ISearchService} but offers the methods without dependencies to
 * state objects. Those are added by the implementation of this interface. Implementations will mostly call
 * <tt>ISearchService</tt> and get the session state objects.
 *
 * @author faessler
 * @deprecated the statefulness of the search service makes no sense any more since the new search service does not expect state objects
 */

public interface IStatefulSearchService {
    Future<SemedicoESSearchResult> doArticleSearch(String docId, String indexType, ParseTree highlightingQuery);

    Future<SemedicoESSearchResult> doDocumentPagingSearch(ParseTree query, int startPosition);

    Future<SemedicoESSearchResult> doFacetNavigationSearch(UIFacet uiFacet, ParseTree query);

    Future<SemedicoESSearchResult> doFacetNavigationSearch(Collection<UIFacet> uiFacets, ParseTree query);

    Future<SemedicoESSearchResult> doTabSelectSearch(String solrQuery);

    Future<SemedicoESSearchResult> doTermSelectSearch(ParseTree semedicoQuery, String userQuery);

    Future<SemedicoESSearchResult> doNewDocumentSearch(List<QueryToken> userQuery);

    Future<SemedicoESSearchResult> doDocumentSearchWebservice(
            List<QueryToken> userQuery,
            SortCriterium sortcriterium,
            int startPosition, int subsetsize);
}
