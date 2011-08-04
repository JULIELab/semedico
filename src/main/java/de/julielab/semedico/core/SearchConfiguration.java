package de.julielab.semedico.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Taxonomy.IFacetTerm;

public class SearchConfiguration {

	private SortCriterium sortCriterium;
	private boolean reviewsFiltered;
	private Multimap<String, IFacetTerm> queryTerms;
	private Map<IFacetTerm, Facet> queryTermFacetMap;
	private Map<Facet, FacetConfiguration> facetConfigurations;
	private Multimap<String, String> spellingCorrections;
	private Multimap<String, IFacetTerm> spellingCorrectedQueryTerms;
	private final List<FacetGroup> facetGroups;
	private boolean newSearch;
	
	public SearchConfiguration(SortCriterium sortCriterium,
			boolean reviewsFiltered, Multimap<String, IFacetTerm> queryTerms,
			HashMap<IFacetTerm, Facet> queryTermFacetMap, Map<Facet, FacetConfiguration> facetConfigurations,
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
	
	public void setSpellingCorrections(Multimap<String, String> spellingCorrections) {
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
	 * @param newSearch the newSearch to set
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
