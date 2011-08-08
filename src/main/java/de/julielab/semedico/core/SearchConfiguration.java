package de.julielab.semedico.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Taxonomy.IFacetTerm;

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

	public SearchConfiguration(SortCriterium sortCriterium,
			boolean reviewsFiltered, Multimap<String, IFacetTerm> queryTerms,
			HashMap<IFacetTerm, Facet> queryTermFacetMap,
			Map<Facet, FacetConfiguration> facetConfigurations,
			List<FacetGroup> facetGroups, boolean newSearch) {
		super();
		this.sortCriterium = sortCriterium;
		this.reviewsFiltered = reviewsFiltered;
		this.queryTerms = queryTerms;
		this.queryTermFacetMap = queryTermFacetMap;
		this.facetConfigurations = facetConfigurations;
		this.facetGroups = facetGroups;
		this.setNewSearch(newSearch);
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

}
