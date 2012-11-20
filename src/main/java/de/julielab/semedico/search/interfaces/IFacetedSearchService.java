package de.julielab.semedico.search.interfaces;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.FacetedSearchResult;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.util.TripleStream;

public interface IFacetedSearchService {

	public int DO_FACET = 1;
	
	/**
	 * For new searches where the user input is analyzed from scratch or the
	 * user selected a suggested term.
	 * 
	 * @param userQueryString
	 * @param termAndFacetId
	 * @return
	 * @throws IOException
	 */
	public FacetedSearchResult search(String userQueryString,
			Pair<String, String> termAndFacetId, int flags);

	/**
	 * For subsequent searches where we already have analyzed the initial user
	 * query and now refresh the search for newly selected terms, discarded
	 * terms etc.
	 * 
	 * @param disambiguatedQuery
	 * @return
	 */
	public FacetedSearchResult search(
			Multimap<String, IFacetTerm> disambiguatedQuery, int flags);

	/**
	 * @param displayedTermIds
	 * @param labels
	 * @return
	 */
	public void queryAndStoreHierarchichalFacetCounts(String solrQueryString,
			Multimap<UIFacet, IFacetTerm> displayedTermIds,
			LabelStore facetHit);

	/**
	 * @param facets
	 * @return
	 */
	public void queryAndStoreFlatFacetCounts(String solrQueryString,
			List<UIFacet> facets, LabelStore facetHit);

	/**
	 * @param allDisplayedTerms
	 */
	public void queryAndStoreFacetCountsInSelectedFacetGroup(
			String solrQueryString,
			Map<UIFacet, Collection<IFacetTerm>> allDisplayedTerms,
			LabelStore facetHit);

	/**
	 * @param originalQueryString
	 * @param searchState
	 * @return
	 */
	public Collection<String> getPmidsForSearch(String originalQueryString,
			SearchState searchState);
	
	/**
	 * @param solrQueryString
	 * @param start
	 * @param maxHighlightSnippets
	 * @return
	 */
	public List<DocumentHit> constructDocumentPage(String solrQueryString, int start,
			int maxHighlightSnippets);

	/**
	 * @param searchNodes
	 * @param bTerm
	 * @param targetSNIndex
	 * @return
	 */
	public FacetedSearchResult searchBTermSearchNode(
			List<Multimap<String, IFacetTerm>> searchNodes, IFacetTerm bTerm,
			int targetSNIndex);
	
	public long getNumDocs();

	/**
	 * @param searchNodes
	 * @param targetSNIndex
	 * @param fields
	 * @return
	 */
	TripleStream<String, Integer, Integer> getSearchNodeTermsInField(
			List<Multimap<String, IFacetTerm>> searchNodes, int targetSNIndex,
			String[] fields);

}
