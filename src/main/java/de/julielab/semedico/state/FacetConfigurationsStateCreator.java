package de.julielab.semedico.state;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.services.ApplicationStateCreator;

import de.julielab.stemnet.core.Facet;
import de.julielab.stemnet.core.FacetConfiguration;
import de.julielab.stemnet.core.services.IFacetService;

public class FacetConfigurationsStateCreator implements ApplicationStateCreator<Map<Facet, FacetConfiguration>>{
	
	private IFacetService facetService;
	
	public FacetConfigurationsStateCreator(IFacetService facetService) {
		super();
		this.facetService = facetService;
	}

	@Override
	public Map<Facet, FacetConfiguration> create() {
		
		Map<Facet, FacetConfiguration> configurations = new HashMap<Facet, FacetConfiguration>();
		try {
			Collection<Facet> facets = facetService.getFacets();
			for( Facet facet: facets ){
				configurations.put(facet, new FacetConfiguration(facet));
			}
		} catch (SQLException exc) {
			throw new IllegalStateException(exc);
		}
		return configurations;
	}

	public IFacetService getFacetService() {
		return facetService;
	}

	public void setFacetService(IFacetService facetService) {
		this.facetService = facetService;
	}

}
