package de.julielab.semedico.state;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.services.ApplicationStateCreator;

import com.google.common.collect.HashMultimap;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.SearchConfiguration;
import de.julielab.semedico.core.SortCriterium;
import de.julielab.semedico.core.services.IFacetService;

/**
 * Used for dependency injection of a session based SearchConfiguration. The
 * usage of this ApplicationStateCreator is determined in AppModule.
 * 
 * @author landefeld
 * 
 */
public class FacetConfigurationsStateCreator implements
		ApplicationStateCreator<SearchConfiguration> {

	private IFacetService facetService;

	public FacetConfigurationsStateCreator(IFacetService facetService) {
		super();
		this.facetService = facetService;
	}

	public SearchConfiguration create() {

		Map<Facet, FacetConfiguration> configurations = new HashMap<Facet, FacetConfiguration>();
		try {
			Collection<Facet> facets = facetService.getFacets();
			for (Facet facet : facets) {
				configurations.put(facet, new FacetConfiguration(facet));
			}
		} catch (SQLException exc) {
			throw new IllegalStateException(exc);
		}

		HashMultimap<String, FacetTerm> map = HashMultimap.create();
		return new SearchConfiguration(SortCriterium.DATE_AND_RELEVANCE, false, map, configurations);
	}

	public IFacetService getFacetService() {
		return facetService;
	}

	public void setFacetService(IFacetService facetService) {
		this.facetService = facetService;
	}

}
