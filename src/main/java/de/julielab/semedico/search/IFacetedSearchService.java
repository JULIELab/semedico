package de.julielab.semedico.search;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.FacettedSearchResult;
import de.julielab.semedico.core.SortCriterium;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;

public interface IFacetedSearchService {

	public FacettedSearchResult search(String solrQuery,
			int maxNumberOfHighlightedSnippets, SortCriterium sortCriterium,
			boolean reviewsFiltered) throws IOException;

	public Collection<DocumentHit> constructDocumentPage(int start);

	/**
	 * @param displayedTermIds
	 * @param labels 
	 * @return
	 */
	public void queryAndStoreHierarchichalFacetCounts(Multimap<FacetConfiguration, IFacetTerm> displayedTermIds, FacetHit facetHit);

	/**
	 * @param facets
	 * @return
	 */
	public void queryAndStoreFlatFacetCounts(List<FacetConfiguration> facets, FacetHit facetHit);

	/**
	 * @param allDisplayedTerms
	 */
	public void queryAndStoreFacetCountsInSelectedFacetGroup(
			Map<FacetConfiguration, Collection<IFacetTerm>> allDisplayedTerms, FacetHit facetHit);

}
