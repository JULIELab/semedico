package de.julielab.semedico.core.services;

import de.julielab.semedico.core.entities.state.AbstractUserInterfaceState;
import de.julielab.semedico.core.entities.state.UserInterfaceState;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.facets.UIFacetGroup;
import de.julielab.semedico.core.search.components.data.LabelStore;
import de.julielab.semedico.core.search.interfaces.ILabelCacheService;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import de.julielab.semedico.core.services.interfaces.IFacetDeterminerManager;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import org.apache.tapestry5.ioc.LoggerSource;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author faessler
 * 
 */
public abstract class AbstractUserInterfaceStateCreator<T extends AbstractUserInterfaceState>  {

	protected IFacetService facetService;
	protected final ILabelCacheService labelCacheService;
	protected final IConceptService termService;
	protected final LoggerSource loggerSource;
	private IFacetDeterminerManager determinerManager;

	public AbstractUserInterfaceStateCreator(IFacetService facetService, ILabelCacheService labelCacheService,
                                             IConceptService termService, LoggerSource loggerSource,
                                             IFacetDeterminerManager determinerManager) {
		super();
		this.facetService = facetService;
		this.labelCacheService = labelCacheService;
		this.termService = termService;
		this.loggerSource = loggerSource;
		this.determinerManager = determinerManager;
	}

	public T create() {

		// Create and organize this session's facetConfigurations.
		List<UIFacetGroup> uiFacetGroups = new ArrayList<>();
		Map<Facet, UIFacet> uiFacetByFacet = new HashMap<Facet, UIFacet>();
		List<FacetGroup<Facet>> facetGroupToDisplay = getFacetGroupToDisplay();
		createUIFacets(facetGroupToDisplay, uiFacetGroups, uiFacetByFacet);

		LabelStore labelStore = new LabelStore(labelCacheService);

//		UserInterfaceState uiState = new UserInterfaceState(loggerSource.getLogger(UserInterfaceState.class),
//				uiFacetByFacet, uiFacetGroups, labelStore);
		
		T uiState = createUserInterfaceState(loggerSource.getLogger(UserInterfaceState.class), uiFacetByFacet, uiFacetGroups, labelStore);

		return uiState;
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
				UIFacet uiFacet = facet.getUiFacetCopy(loggerSource.getLogger(UIFacet.class));
				uiFacetGroup.add(uiFacet);
				uiFacetsByFacetSearch.put(facet, uiFacet);
			}
			addFacetSections(uiFacetGroup);
			uiFacetGroupsSearch.add(uiFacetGroup);
		}
	}

	private void addFacetSections(UIFacetGroup uiFacetGroup) {
		uiFacetGroup.addDefaultSection();
//		if (uiFacetGroup.getName().equals("BioPortal")) {
//			UIFacetGroupSection section = uiFacetGroup.new UIFacetGroupSection("Query Facets", true);
//			section.setDescription("The facets in this section have been determined automatically depending on your query.");
//			IFacetDeterminer facetDeterminer = determinerManager
//					.getFacetDeterminer(BioPortalFacetsFromQueryDeterminer.class);
//			facetDeterminer.setFacetGroup(uiFacetGroup);
//			section.setFacetDeterminer(facetDeterminer);
//			uiFacetGroup.addSection(section);
//		}

	}
}
