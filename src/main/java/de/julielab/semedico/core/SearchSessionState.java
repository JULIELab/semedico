package de.julielab.semedico.core;

import java.util.List;
import java.util.Map;

import de.julielab.semedico.core.services.ITermService;

public class SearchSessionState {

	private final SearchState searchState;
	private final UserInterfaceState uiState;

	public SearchSessionState(
			Map<Facet, FacetConfiguration> facetConfigurations,
			List<FacetGroup> facetGroups, FacetHit facetHit,
			ITermService termService) {
		super();
		this.searchState = new SearchState();
		this.uiState = new UserInterfaceState(termService, facetConfigurations,
				facetGroups, facetHit);
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
