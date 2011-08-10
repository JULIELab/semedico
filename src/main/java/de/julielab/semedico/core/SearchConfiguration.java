package de.julielab.semedico.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.ITermService;

public class SearchConfiguration {

	// The most important information about session state: These are the query
	// terms which determine the currently retrieved documents in the first
	// place.
	private Multimap<String, IFacetTerm> queryTerms;
	// Since a term can occur in multiple facets, this map stores the
	// information from which facet a particular term had been chosen by the
	// user.
	private Map<IFacetTerm, Facet> queryTermFacetMap;
	// Determines how the found documents are to be ordered for display.
	private SortCriterium sortCriterium;
	// Determines whether reviews articles should be shown.
	private boolean reviewsFiltered;
	// This map allows us to retrieve the facetConfiguration associated with a
	// particular facet.
	private Map<Facet, FacetConfiguration> facetConfigurations;
	private Multimap<String, String> spellingCorrections;
	private Multimap<String, IFacetTerm> spellingCorrectedQueryTerms;
	// The existing facet groups (BioMed, Immunology, ...). These belong to the
	// state of a sessions because they can carry information about facet order
	// and such things.
	private final List<FacetGroup> facetGroups;
	// Set to true during the rendering phase after a query was put into the
	// input field and a search had been triggered (opposed to searches by
	// clicking on terms).
	private boolean newSearch;
	private SolrQuery query;
	private int selectedFacetGroupIndex;
	private FacetGroup selectedFacetGroup;
	private FacetHit facetHit;
	private final ITermService termService;
	
	public SearchConfiguration(SortCriterium sortCriterium,
			boolean reviewsFiltered, Multimap<String, IFacetTerm> queryTerms,
			HashMap<IFacetTerm, Facet> queryTermFacetMap,
			Map<Facet, FacetConfiguration> facetConfigurations,
			List<FacetGroup> facetGroups, FacetHit facetHit, ITermService termService) {
		super();
		this.sortCriterium = sortCriterium;
		this.reviewsFiltered = reviewsFiltered;
		this.queryTerms = queryTerms;
		this.queryTermFacetMap = queryTermFacetMap;
		this.facetConfigurations = facetConfigurations;
		this.facetGroups = facetGroups;
		this.termService = termService;
		this.newSearch = true;
		this.setFacetHit(facetHit);
	}

	public SortCriterium getSortCriterium() {
		return sortCriterium;
	}

	public void setSortCriterium(SortCriterium sortCriterium) {
		this.sortCriterium = sortCriterium;
	}

	public boolean isReviewsFiltered() {
		return reviewsFiltered;
	}

	public void setReviewsFiltered(boolean reviewsFiltered) {
		this.reviewsFiltered = reviewsFiltered;
	}

	public Multimap<String, IFacetTerm> getQueryTerms() {
		return queryTerms;
	}

	public void setQueryTerms(Multimap<String, IFacetTerm> queryTerms) {
		this.queryTerms = queryTerms;
	}

	public Map<Facet, FacetConfiguration> getFacetConfigurations() {
		return facetConfigurations;
	}

	public void setFacetConfigurations(
			Map<Facet, FacetConfiguration> facetConfigurations) {
		this.facetConfigurations = facetConfigurations;
	}

	public Multimap<String, String> getSpellingCorrections() {
		return spellingCorrections;
	}

	public void setSpellingCorrections(
			Multimap<String, String> spellingCorrections) {
		this.spellingCorrections = spellingCorrections;
	}

	public Multimap<String, IFacetTerm> getSpellingCorrectedQueryTerms() {
		return spellingCorrectedQueryTerms;
	}

	public void setSpellingCorrectedQueryTerms(
			Multimap<String, IFacetTerm> spellingCorrectedQueryTerms) {
		this.spellingCorrectedQueryTerms = spellingCorrectedQueryTerms;
	}

	public Map<IFacetTerm, Facet> getQueryTermFacetMap() {
		return queryTermFacetMap;
	}

	public void setQueryTermFacetMap(Map<IFacetTerm, Facet> queryTermFacetMap) {
		this.queryTermFacetMap = queryTermFacetMap;
	}

	/**
	 * @return the newSearch
	 */
	public boolean isNewSearch() {
		return newSearch;
	}

	/**
	 * @param newSearch
	 *            the newSearch to set
	 */
	public void setNewSearch(boolean newSearch) {
		this.newSearch = newSearch;
	}

	/**
	 * @return the facetGroups
	 */
	public List<FacetGroup> getFacetGroups() {
		return facetGroups;
	}

	public FacetGroup getFacetGroup(int index) {
		return facetGroups.get(index);
	}

	/**
	 * @param query
	 */
	public void setSolrQuery(SolrQuery query) {
		this.query = query;

	}

	/**
	 * @return the query
	 */
	public SolrQuery getSolrQuery() {
		return query;
	}

	/**
	 * @return the selectedFacetGroup
	 */
	public FacetGroup getSelectedFacetGroup() {
		return selectedFacetGroup;
	}

	/**
	 * @param selectedFacetGroup the selectedFacetGroup to set
	 */
	public void setSelectedFacetGroup(FacetGroup selectedFacetGroup) {
		this.selectedFacetGroup = selectedFacetGroup;
		for (int i = 0; i < facetGroups.size(); i++) {
			if(facetGroups.get(i) == selectedFacetGroup)
				selectedFacetGroupIndex = i;
		}
	}

	/**
	 * @return the selectedFacetGroupIndex
	 */
	public int getSelectedFacetGroupIndex() {
		return selectedFacetGroupIndex;
	}

	/**
	 * @param selectedFacetGroupIndex the selectedFacetGroupIndex to set
	 */
	public void setSelectedFacetGroupIndex(int selectedFacetGroupIndex) {
		this.selectedFacetGroupIndex = selectedFacetGroupIndex;
		this.selectedFacetGroup = facetGroups.get(selectedFacetGroupIndex);
	}

	/**
	 * 
	 */
	public void reset() {
		for (FacetConfiguration configuration : facetConfigurations.values())
			configuration.reset();
		selectedFacetGroupIndex = 0;
		selectedFacetGroup = facetGroups.get(0);
	}

	public void updateLabels() {
		List<String> allIds = getDisplayedTermIds();
		getFacetHit().updateLabels(allIds);
	}
	
	private List<String> getDisplayedTermIds() {
		List<String> displayedTermIds = new ArrayList<String>();
		for (Facet facet : getSelectedFacetGroup()) {
			getDisplayedTermIdsForFacet(displayedTermIds, facet);
		}
		return displayedTermIds;
	}

	private void getDisplayedTermIdsForFacet(List<String> displayedTermIds,
			Facet facet) {
		Map<Facet, FacetConfiguration> facetConfigurations = getFacetConfigurations();
		FacetConfiguration facetConfiguration = facetConfigurations.get(facet);
		if (facetConfiguration.isDrilledDown()) {
			IFacetTerm lastPathTerm = facetConfiguration.getLastPathElement();
			IFacetTerm term = termService.getNode(lastPathTerm.getId());
			Iterator<IFacetTerm> childIt = term.childIterator();
			while (childIt.hasNext())
				displayedTermIds.add(childIt.next().getId());

		} else {
			Iterator<IFacetTerm> rootIt = termService.getFacetRoots(
					facetConfiguration.getFacet()).iterator();
			while (rootIt.hasNext())
				displayedTermIds.add(rootIt.next().getId());
		}
	}

	/**
	 * @return the facetHit
	 */
	public FacetHit getFacetHit() {
		return facetHit;
	}

	/**
	 * @param facetHit the facetHit to set
	 */
	public void setFacetHit(FacetHit facetHit) {
		this.facetHit = facetHit;
	}
	
}
