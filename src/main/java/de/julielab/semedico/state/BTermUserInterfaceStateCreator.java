package de.julielab.semedico.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.services.ApplicationStateManager;

import de.julielab.semedico.core.BTermUserInterfaceState;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.search.interfaces.IFacetedSearchService;
import de.julielab.semedico.search.interfaces.ILabelCacheService;

/**
 * 
 * @author faessler
 * 
 */
public class BTermUserInterfaceStateCreator extends UserInterfaceStateCreator {

	public BTermUserInterfaceStateCreator(IFacetService facetService,
			ILabelCacheService labelCacheService,
			IFacetedSearchService searchService, ITermService termService,
			LoggerSource loggerSource, ApplicationStateManager asm) {
		super(facetService, labelCacheService, searchService, termService, loggerSource, asm);
	}

	public BTermUserInterfaceState create() {

		// Create and organize this session's facetConfigurations.
		List<FacetGroup<FacetConfiguration>> facetConfigurationGroups = new ArrayList<FacetGroup<FacetConfiguration>>();
		Map<Facet, FacetConfiguration> configurationsByFacet = new HashMap<Facet, FacetConfiguration>();
		createFacetConfigurations(facetService.getFacetGroupsBTerms(),
				facetConfigurationGroups, configurationsByFacet);

		LabelStore labelStore = new LabelStore(
				loggerSource.getLogger(LabelStore.class), labelCacheService,
				termService);

		BTermUserInterfaceState uiState = new BTermUserInterfaceState(searchService,
				configurationsByFacet, facetConfigurationGroups,
				labelStore, asm.get(SearchState.class));

		return uiState;
	}

}
