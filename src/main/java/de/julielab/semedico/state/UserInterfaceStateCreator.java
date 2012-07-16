package de.julielab.semedico.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.services.ApplicationStateCreator;
import org.apache.tapestry5.services.ApplicationStateManager;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.search.interfaces.IFacetedSearchService;
import de.julielab.semedico.search.interfaces.ILabelCacheService;

/**
 * 
 * @author faessler
 * 
 */
public class UserInterfaceStateCreator implements
		ApplicationStateCreator<UserInterfaceState> {

	protected IFacetService facetService;
	protected final ILabelCacheService labelCacheService;
	protected final IFacetedSearchService searchService;
	protected final ITermService termService;
	protected final LoggerSource loggerSource;
	protected final ApplicationStateManager asm;

	public UserInterfaceStateCreator(IFacetService facetService,
			ILabelCacheService labelCacheService,
			IFacetedSearchService searchService, ITermService termService,
			LoggerSource loggerSource, ApplicationStateManager asm) {
		super();
		this.facetService = facetService;
		this.labelCacheService = labelCacheService;
		this.searchService = searchService;
		this.termService = termService;
		this.loggerSource = loggerSource;
		this.asm = asm;
	}

	public UserInterfaceState create() {

		// Create and organize this session's facetConfigurations.
		List<FacetGroup<FacetConfiguration>> facetConfigurationGroups = new ArrayList<FacetGroup<FacetConfiguration>>();
		Map<Facet, FacetConfiguration> configurationsByFacet = new HashMap<Facet, FacetConfiguration>();
		createFacetConfigurations(facetService.getFacetGroupsSearch(),
				facetConfigurationGroups, configurationsByFacet);

		LabelStore facetHit = new LabelStore(
				loggerSource.getLogger(LabelStore.class), labelCacheService,
				termService);

		UserInterfaceState uiState = new UserInterfaceState(searchService,
				configurationsByFacet, facetConfigurationGroups,
				facetHit, asm.get(SearchState.class));

		return uiState;
	}

	/**
	 * @param facetGroupsSearch
	 * @param facetConfigurationGroupsSearch
	 * @param configurationsByFacetSearch
	 */
	protected void createFacetConfigurations(
			List<FacetGroup<Facet>> facetGroupsSearch,
			List<FacetGroup<FacetConfiguration>> facetConfigurationGroupsSearch,
			Map<Facet, FacetConfiguration> configurationsByFacetSearch) {
		for (FacetGroup<Facet> facetGroup : facetGroupsSearch) {
			FacetGroup<FacetConfiguration> facetConfigurationGroup = facetGroup
					.copyFacetGroup();
			for (Facet facet : facetGroup) {
				Collection<IFacetTerm> roots = termService.getFacetRoots(facet);
				FacetConfiguration facetConfiguration = new FacetConfiguration(
						loggerSource.getLogger(FacetConfiguration.class),
						facet, roots);
				facetConfigurationGroup.add(facetConfiguration);
				configurationsByFacetSearch.put(facet, facetConfiguration);
			}
			facetConfigurationGroupsSearch.add(facetConfigurationGroup);
		}
	}
}
