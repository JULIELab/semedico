package de.julielab.semedico.core.facets;

import java.util.List;

import de.julielab.semedico.core.entities.state.AbstractUserInterfaceState;
import de.julielab.semedico.core.entities.state.SearchState;

public interface IFacetDeterminer {
	List<UIFacet> determineFacets(SearchState searchState, AbstractUserInterfaceState uiState);
	void setFacetGroup(UIFacetGroup facetGroup);
	Class<?> getMarker();
}
