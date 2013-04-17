package de.julielab.semedico.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.services.ApplicationStateCreator;
import org.apache.tapestry5.services.ApplicationStateManager;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;
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
	protected final ITermService termService;
	protected final LoggerSource loggerSource;
	protected final ApplicationStateManager asm;

	public UserInterfaceStateCreator(IFacetService facetService,
			ILabelCacheService labelCacheService, ITermService termService,
			LoggerSource loggerSource, ApplicationStateManager asm) {
		super();
		this.facetService = facetService;
		this.labelCacheService = labelCacheService;
		this.termService = termService;
		this.loggerSource = loggerSource;
		this.asm = asm;
	}

	public UserInterfaceState create() {

		// Create and organize this session's facetConfigurations.
		List<FacetGroup<UIFacet>> facetConfigurationGroups = new ArrayList<FacetGroup<UIFacet>>();
		Map<Facet, UIFacet> configurationsByFacet = new HashMap<Facet, UIFacet>();
		createUIFacets(facetService.getFacetGroupsSearch(),
				facetConfigurationGroups, configurationsByFacet);

		LabelStore labelStore = new LabelStore(labelCacheService);

		UserInterfaceState uiState = new UserInterfaceState(
				loggerSource.getLogger(UserInterfaceState.class),
				configurationsByFacet, facetConfigurationGroups, labelStore,
				asm.get(SearchState.class));

		return uiState;
	}

	/**
	 * @param facetGroupsSearch
	 * @param uiFacetGroupsSearch
	 * @param uiFacetsByFacetSearch
	 */
	protected void createUIFacets(
			List<FacetGroup<Facet>> facetGroupsSearch,
			List<FacetGroup<UIFacet>> uiFacetGroupsSearch,
			Map<Facet, UIFacet> uiFacetsByFacetSearch) {
		for (FacetGroup<Facet> facetGroup : facetGroupsSearch) {
			FacetGroup<UIFacet> uiFacetGroup = facetGroup
					.copyFacetGroup();
			for (Facet facet : facetGroup) {
				UIFacet uiFacet = facet.getUiFacetCopy(loggerSource
						.getLogger(UIFacet.class));
				uiFacetGroup.add(uiFacet);
				uiFacetsByFacetSearch.put(facet, uiFacet);
			}
			uiFacetGroupsSearch.add(uiFacetGroup);
		}
	}
}
