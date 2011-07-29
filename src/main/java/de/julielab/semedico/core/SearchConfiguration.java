package de.julielab.semedico.core;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.MultiHierarchy.IMultiHierarchyNode;

public class SearchConfiguration {

	private SortCriterium sortCriterium;
	private boolean reviewsFiltered;
	private Multimap<String, IMultiHierarchyNode> queryTerms;
	private Map<IMultiHierarchyNode, Facet> queryTermFacetMap;
	private Map<Facet, FacetConfiguration> facetConfigurations;
	private Multimap<String, String> spellingCorrections;
	private Multimap<String, IMultiHierarchyNode> spellingCorrectedQueryTerms;
	
	public SearchConfiguration(SortCriterium sortCriterium,
			boolean reviewsFiltered, Multimap<String, IMultiHierarchyNode> queryTerms,
			HashMap<IMultiHierarchyNode, Facet> queryTermFacetMap, Map<Facet, FacetConfiguration> facetConfigurations) {
		super();
		this.sortCriterium = sortCriterium;
		this.reviewsFiltered = reviewsFiltered;
		this.queryTerms = queryTerms;
		this.queryTermFacetMap = queryTermFacetMap;
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
	
	public Multimap<String, IMultiHierarchyNode> getQueryTerms() {
		return queryTerms;
	}
	
	public void setQueryTerms(Multimap<String, IMultiHierarchyNode> queryTerms) {
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
	
	public Multimap<String, IMultiHierarchyNode> getSpellingCorrectedQueryTerms() {
		return spellingCorrectedQueryTerms;
	}
	
	public void setSpellingCorrectedQueryTerms(
			Multimap<String, IMultiHierarchyNode> spellingCorrectedQueryTerms) {
		this.spellingCorrectedQueryTerms = spellingCorrectedQueryTerms;
	}

	public Map<IMultiHierarchyNode, Facet> getQueryTermFacetMap() {
		return queryTermFacetMap;
	}
	
	public void setQueryTermFacetMap(Map<IMultiHierarchyNode, Facet> queryTermFacetMap) {
		this.queryTermFacetMap = queryTermFacetMap;
	}
}
