package de.julielab.semedico.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.services.ApplicationStateCreator;
import org.slf4j.Logger;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.SearchSessionState;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.IFacetService;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.search.IFacetedSearchService;
import de.julielab.semedico.search.ILabelCacheService;

/**
 * Used for dependency injection of a session based SearchConfiguration. The
 * usage of this ApplicationStateCreator is determined in AppModule.
 * 
 * @author landefeld
 * 
 */
public class FacetConfigurationsStateCreator implements
		ApplicationStateCreator<SearchSessionState> {

	private IFacetService facetService;
	private final ILabelCacheService labelCacheService;
	private final IFacetedSearchService searchService;
	private final ITermService termService;
	private final LoggerSource loggerSource;

	public FacetConfigurationsStateCreator(IFacetService facetService, ILabelCacheService labelCacheService, IFacetedSearchService searchService, ITermService termService, LoggerSource loggerSource) {
		super();
		this.facetService = facetService;
		this.labelCacheService = labelCacheService;
		this.searchService = searchService;
		this.termService = termService;
		this.loggerSource = loggerSource;
	}

	public SearchSessionState create() {

		// Create and organize this session's facetConfigurations.
		List<FacetGroup<FacetConfiguration>> facetConfigurationGroups = new ArrayList<FacetGroup<FacetConfiguration>>();
		Map<Facet, FacetConfiguration> configurationsByFacet = new HashMap<Facet, FacetConfiguration>();
		for (FacetGroup<Facet> facetGroup : facetService.getFacetGroups()) {
			FacetGroup<FacetConfiguration> facetConfigurationGroup = facetGroup.copyFacetGroup();
			for (Facet facet : facetGroup) {
				Collection<IFacetTerm> roots = termService.getFacetRoots(facet);
				FacetConfiguration facetConfiguration = new FacetConfiguration(loggerSource.getLogger(FacetConfiguration.class), facet, roots);
				facetConfigurationGroup.add(facetConfiguration);
				configurationsByFacet.put(facet, facetConfiguration);
			}
			facetConfigurationGroups.add(facetConfigurationGroup);
		}

		FacetHit facetHit = new FacetHit(loggerSource.getLogger(FacetHit.class), labelCacheService, termService);
		SearchSessionState searchConfiguration = new SearchSessionState(configurationsByFacet, facetConfigurationGroups, facetHit, termService, searchService);
		return searchConfiguration;
	}

	public IFacetService getFacetService() {
		return facetService;
	}

	public void setFacetService(IFacetService facetService) {
		this.facetService = facetService;
	}

}
