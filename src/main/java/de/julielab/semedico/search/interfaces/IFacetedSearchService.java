package de.julielab.semedico.search.interfaces;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.FacetedSearchResult;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.SortCriterium;
import de.julielab.semedico.core.taxonomy.IFacetTerm;

public interface IFacetedSearchService {

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
			Pair<String, String> termAndFacetId);

	/**
	 * For subsequent searches where we already have analyzed the initial user
	 * query and now refresh the search for newly selected terms, discarded
	 * terms etc.
	 * 
	 * @param disambiguatedQuery
	 * @return
	 */
	public FacetedSearchResult search(
			Multimap<String, IFacetTerm> disambiguatedQuery);

	public Collection<DocumentHit> constructDocumentPage(int start);

	/**
	 * @param displayedTermIds
	 * @param labels
	 * @return
	 */
	public void queryAndStoreHierarchichalFacetCounts(String solrQueryString,
			Multimap<FacetConfiguration, IFacetTerm> displayedTermIds,
			LabelStore facetHit);

	/**
	 * @param facets
	 * @return
	 */
	public void queryAndStoreFlatFacetCounts(String solrQueryString,
			List<FacetConfiguration> facets, LabelStore facetHit);

	/**
	 * @param allDisplayedTerms
	 */
	public void queryAndStoreFacetCountsInSelectedFacetGroup(
			String solrQueryString,
			Map<FacetConfiguration, Collection<IFacetTerm>> allDisplayedTerms,
			LabelStore facetHit);

	/**
	 * @param originalQueryString
	 * @param searchState
	 * @return
	 */
	public Collection<String> getPmidsForSearch(String originalQueryString,
			SearchState searchState);

}
