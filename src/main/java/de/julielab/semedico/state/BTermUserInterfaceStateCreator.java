package de.julielab.semedico.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.services.ApplicationStateManager;

import de.julielab.semedico.core.BTermUserInterfaceState;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.search.interfaces.ILabelCacheService;

/**
 * 
 * @author faessler
 * 
 */
public class BTermUserInterfaceStateCreator extends UserInterfaceStateCreator {

	public BTermUserInterfaceStateCreator(IFacetService facetService,
			ILabelCacheService labelCacheService, ITermService termService,
			LoggerSource loggerSource, ApplicationStateManager asm) {
		super(facetService, labelCacheService, termService,
				loggerSource, asm);
	}

	public BTermUserInterfaceState create() {

		// Create and organize this session's facetConfigurations.
		List<FacetGroup<UIFacet>> facetConfigurationGroups = new ArrayList<FacetGroup<UIFacet>>();
		Map<Facet, UIFacet> configurationsByFacet = new HashMap<Facet, UIFacet>();
		createUIFacets(facetService.getFacetGroupsBTerms(),
				facetConfigurationGroups, configurationsByFacet);

		LabelStore labelStore = new LabelStore(labelCacheService);

		BTermUserInterfaceState uiState = new BTermUserInterfaceState(
				loggerSource.getLogger(BTermUserInterfaceState.class),
				configurationsByFacet, facetConfigurationGroups,
				labelStore, asm.get(SearchState.class));

		return uiState;
	}

}
