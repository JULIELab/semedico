package de.julielab.semedico.core;

import java.util.List;
import java.util.Map;

import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.search.IFacetedSearchService;

public class SearchSessionState {

	private final SearchState searchState;
	private final UserInterfaceState uiState;

	public SearchSessionState(
			Map<Facet, FacetConfiguration> facetConfigurations,
			List<FacetGroup<FacetConfiguration>> facetConfigurationGroups, FacetHit facetHit,
			ITermService termService, IFacetedSearchService searchService) {
		super();
		this.searchState = new SearchState();
		this.uiState = new UserInterfaceState(termService, searchService, facetConfigurations,
				facetConfigurationGroups, facetHit);
	}

	/**
	 * 
	 */
	public void reset() {
		getUiState().reset();
	}

	/**
	 * @return the searchState
	 */
	public SearchState getSearchState() {
		return searchState;
	}

	/**
	 * @return the uiState
	 */
	public UserInterfaceState getUiState() {
		return uiState;
	}

}
