package de.julielab.semedico.core.services;

import java.util.List;
import java.util.Map;

import de.julielab.semedico.core.services.interfaces.*;
import org.apache.tapestry5.ioc.LoggerSource;
import org.slf4j.Logger;

import de.julielab.semedico.core.entities.state.UserInterfaceState;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.facets.UIFacetGroup;
import de.julielab.semedico.core.search.components.data.LabelStore;
import de.julielab.semedico.core.search.interfaces.ILabelCacheService;

public class DocumentRetrievalUserInterfaceCreator extends AbstractUserInterfaceStateCreator<UserInterfaceState> implements IDocumentRetrievalUserInterfaceCreator {

	public DocumentRetrievalUserInterfaceCreator(IFacetService facetService, ILabelCacheService labelCacheService,
												 IConceptService termService,
												  LoggerSource loggerSource,
												 IFacetDeterminerManager determinerManager) {
		super(facetService, labelCacheService, termService, loggerSource, determinerManager);
	}
	
	@Override
	public UserInterfaceState create() {
		return super.create();
	}
	
	private void addFacetSections(UIFacetGroup uiFacetGroup) {
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

	@Override
	protected UserInterfaceState createUserInterfaceState(Logger log, Map<Facet, UIFacet> uiFacetByFacet,
			List<UIFacetGroup> uiFacetGroups, LabelStore labelStore) {
		return new UserInterfaceState(loggerSource.getLogger(UserInterfaceState.class),
				uiFacetByFacet, uiFacetGroups, labelStore);
	}


	@Override
	protected List<FacetGroup<Facet>> getFacetGroupToDisplay() {
		return facetService.getFacetGroupsSearch();
	}

}
