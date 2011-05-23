package de.julielab.stemnet.core;

import java.util.Map;

import com.google.common.collect.Multimap;

public class SearchConfiguration {

	private SortCriterium sortCriterium;
	private boolean reviewsFiltered;
	private Multimap<String, FacetTerm> queryTerms;
	private Map<Facet, FacetConfiguration> facetConfigurations;
	private Multimap<String, String> spellingCorrections;
	private Multimap<String, FacetTerm> spellingCorrectedQueryTerms;
	
	public SearchConfiguration(SortCriterium sortCriterium,
			boolean reviewsFiltered, Multimap<String, FacetTerm> queryTerms,
			Map<Facet, FacetConfiguration> facetConfigurations) {
		super();
		this.sortCriterium = sortCriterium;
		this.reviewsFiltered = reviewsFiltered;
		this.queryTerms = queryTerms;
		this.facetConfigurations = facetConfigurations;
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
	
	public Multimap<String, FacetTerm> getQueryTerms() {
		return queryTerms;
	}
	
	public void setQueryTerms(Multimap<String, FacetTerm> queryTerms) {
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
	
	public Multimap<String, FacetTerm> getSpellingCorrectedQueryTerms() {
		return spellingCorrectedQueryTerms;
	}
	
	public void setSpellingCorrectedQueryTerms(
			Multimap<String, FacetTerm> spellingCorrectedQueryTerms) {
		this.spellingCorrectedQueryTerms = spellingCorrectedQueryTerms;
	}
}
