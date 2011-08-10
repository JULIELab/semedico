package de.julielab.semedico.state;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.services.ApplicationStateCreator;

import com.google.common.collect.HashMultimap;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.SearchConfiguration;
import de.julielab.semedico.core.SortCriterium;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.IFacetService;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.search.IFacettedSearchService;
import de.julielab.semedico.search.ILabelCacheService;

/**
 * Used for dependency injection of a session based SearchConfiguration. The
 * usage of this ApplicationStateCreator is determined in AppModule.
 * 
 * @author landefeld
 * 
 */
public class FacetConfigurationsStateCreator implements
		ApplicationStateCreator<SearchConfiguration> {

	private IFacetService facetService;
	private final ILabelCacheService labelCacheService;
	private final IFacettedSearchService searchService;
	private final ITermService termService;

	public FacetConfigurationsStateCreator(IFacetService facetService, ILabelCacheService labelCacheService, IFacettedSearchService searchService, ITermService termService) {
		super();
		this.facetService = facetService;
		this.labelCacheService = labelCacheService;
		this.searchService = searchService;
		this.termService = termService;
	}

	public SearchConfiguration create() {

		Map<Facet, FacetConfiguration> configurations = new HashMap<Facet, FacetConfiguration>();
		Collection<Facet> facets = facetService.getFacets();
		for (Facet facet : facets) {
			configurations.put(facet, new FacetConfiguration(facet));
		}

		FacetHit facetHit = new FacetHit(new HashMap<String, Label>(), labelCacheService, searchService);
		SearchConfiguration searchConfiguration = new SearchConfiguration(SortCriterium.DATE_AND_RELEVANCE, false,
				HashMultimap.<String, IFacetTerm>create(), new HashMap<IFacetTerm, Facet>(), configurations, facetService.copyFacetGroups(), facetHit, termService);
		return searchConfiguration;
	}

	public IFacetService getFacetService() {
		return facetService;
	}

	public void setFacetService(IFacetService facetService) {
		this.facetService = facetService;
	}

}
