package de.julielab.semedico.core.facets;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import de.julielab.semedico.core.AbstractUserInterfaceState;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.services.interfaces.IBioPortalOntologyRecommender;

@Deprecated
public class BioPortalFacetsFromQueryDeterminer implements IFacetDeterminer {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface BioPortalFacetsFromQueryDetermination {
		//
	}
	
	private UIFacetGroup facetGroup;
	private IBioPortalOntologyRecommender ontologyRecommender;

	public BioPortalFacetsFromQueryDeterminer(IBioPortalOntologyRecommender ontologyRecommender) {
		this.ontologyRecommender = ontologyRecommender;
	}

	@Override
	public List<UIFacet> determineFacets(SearchState searchState, AbstractUserInterfaceState uiState) {
//		String userQueryString = searchState.getUserQueryString();
		String userQueryString ="";
		return ontologyRecommender.recommendOntologies(userQueryString, facetGroup);
	}

	@Override
	public void setFacetGroup(UIFacetGroup facetGroup) {
		this.facetGroup = facetGroup;

	}
	
	@Override
	public Class<?> getMarker(){
		return BioPortalFacetsFromQueryDetermination.class;
	}

}
