package de.julielab.semedico.state;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.services.ApplicationStateCreator;

import com.google.common.collect.HashMultimap;

import de.julielab.stemnet.core.Facet;
import de.julielab.stemnet.core.FacetConfiguration;
import de.julielab.stemnet.core.SearchConfiguration;
import de.julielab.stemnet.core.SortCriterium;
import de.julielab.stemnet.core.Term;
import de.julielab.stemnet.core.services.IFacetService;

public class FacetConfigurationsStateCreator implements ApplicationStateCreator<SearchConfiguration>{
	
	private IFacetService facetService;
	
	public FacetConfigurationsStateCreator(IFacetService facetService) {
		super();
		this.facetService = facetService;
	}

	public SearchConfiguration create() {
		
		Map<Facet, FacetConfiguration> configurations = new HashMap<Facet, FacetConfiguration>();
		try {
			Collection<Facet> facets = facetService.getFacets();
			for( Facet facet: facets ){
				configurations.put(facet, new FacetConfiguration(facet));
			}
		} catch (SQLException exc) {
			throw new IllegalStateException(exc);
		}

		return new SearchConfiguration(SortCriterium.DATE_AND_RELEVANCE, 
									   false, 
									   new HashMultimap<String, Term>(), 
									   configurations);
	}

	public IFacetService getFacetService() {
		return facetService;
	}

	public void setFacetService(IFacetService facetService) {
		this.facetService = facetService;
	}

}
