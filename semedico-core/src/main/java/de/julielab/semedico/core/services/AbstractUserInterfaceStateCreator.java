package de.julielab.semedico.core.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ioc.LoggerSource;
import org.slf4j.Logger;

import de.julielab.semedico.core.AbstractUserInterfaceState;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.facets.UIFacetGroup;
import de.julielab.semedico.core.search.components.data.LabelStore;
import de.julielab.semedico.core.search.interfaces.ILabelCacheService;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;

/**
 * 
 * @author faessler
 * 
 */
public abstract class AbstractUserInterfaceStateCreator<T extends AbstractUserInterfaceState>  {

	protected IFacetService facetService;
	protected final ILabelCacheService labelCacheService;
	protected final ITermService termService;
	protected final LoggerSource loggerSource;

	public AbstractUserInterfaceStateCreator(IFacetService facetService, ILabelCacheService labelCacheService,
			ITermService termService, LoggerSource loggerSource) {
		super();
		this.facetService = facetService;
		this.labelCacheService = labelCacheService;
		this.termService = termService;
		this.loggerSource = loggerSource;
	}

	public T create() {

		// Create and organize this session's facetConfigurations.
		List<UIFacetGroup> uiFacetGroups = new ArrayList<>();
		Map<Facet, UIFacet> uiFacetByFacet = new HashMap<>();
		List<FacetGroup<Facet>> facetGroupToDisplay = getFacetGroupToDisplay();
		createUIFacets(facetGroupToDisplay, uiFacetGroups, uiFacetByFacet);

		LabelStore labelStore = new LabelStore(labelCacheService);

		return createUserInterfaceState(loggerSource.getLogger(UserInterfaceState.class), uiFacetByFacet, uiFacetGroups, labelStore);
	}
	
	protected abstract T createUserInterfaceState(Logger log, Map<Facet, UIFacet> uiFacetByFacet, List<UIFacetGroup> uiFacetGroups, LabelStore labelStore);
	protected abstract List<FacetGroup<Facet>> getFacetGroupToDisplay();

	/**
	 * @param facetGroupsSearch The facet groups that should be shown for search as defined by the database. They are the 'templates' for the user interface copies that are created in this method and put into <tt>uiFacetGroupsSearch</tt>.
	 * @param uiFacetGroupsSearch The user specific interface copies of the facets in <tt>facetGroupsSearch</tt>.
	 * @param uiFacetsByFacetSearch
	 */
	protected void createUIFacets(List<FacetGroup<Facet>> facetGroupsSearch, List<UIFacetGroup> uiFacetGroupsSearch,
			Map<Facet, UIFacet> uiFacetsByFacetSearch) {
		for (FacetGroup<Facet> facetGroup : facetGroupsSearch) {
			UIFacetGroup uiFacetGroup = facetGroup.getUiFacetGroup();
			for (Facet facet : facetGroup) {
				UIFacet uiFacet = facet.getUiFacetCopy();
				uiFacetGroup.add(uiFacet);
				uiFacetsByFacetSearch.put(facet, uiFacet);
			}
			addFacetSections(uiFacetGroup);
			uiFacetGroupsSearch.add(uiFacetGroup);
		}
	}

	private void addFacetSections(UIFacetGroup uiFacetGroup) {
		uiFacetGroup.addDefaultSection();
	}
}
