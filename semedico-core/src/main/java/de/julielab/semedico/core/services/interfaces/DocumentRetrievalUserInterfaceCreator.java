package de.julielab.semedico.core.services.interfaces;

import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ioc.LoggerSource;
import org.slf4j.Logger;

import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.facets.UIFacetGroup;
import de.julielab.semedico.core.search.components.data.LabelStore;
import de.julielab.semedico.core.search.interfaces.ILabelCacheService;
import de.julielab.semedico.core.services.AbstractUserInterfaceStateCreator;

public class DocumentRetrievalUserInterfaceCreator extends AbstractUserInterfaceStateCreator<UserInterfaceState> implements IDocumentRetrievalUserInterfaceCreator {

	public DocumentRetrievalUserInterfaceCreator(IFacetService facetService, ILabelCacheService labelCacheService,
			ITermService termService, LoggerSource loggerSource) {
		super(facetService, labelCacheService, termService, loggerSource);
	}
	
	@Override
	public UserInterfaceState create() {
		return super.create();
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
