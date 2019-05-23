package de.julielab.semedico.core.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.services.interfaces.IFacetService;

/**
 * A facet service super class to assemble the elements that just each facet
 * service in Semedico should have.
 * 
 * @author faessler
 * 
 */
public abstract class CoreFacetService implements IFacetService {

	protected List<Facet> facets;
	protected Map<String, Facet> facetsById;
	protected List<FacetGroup<Facet>> facetGroupsSearch;
	protected Logger log;

	public CoreFacetService(Logger log) {
		this.log = log;
		facetsById = new HashMap<>();
		facets = new ArrayList<>();
		facetGroupsSearch = new ArrayList<>();
		initializeDefaultFacets();
	}

	private void initializeDefaultFacets() {
		facets.add(Facet.KEYWORD_FACET);
		facets.add(Facet.CORE_TERMS_FACET);
		facets.add(Facet.BOOLEAN_OPERATORS_FACET);
		facets.add(Facet.MOST_INFORMATIVE_CONCEPTS_FACET);
		facets.add(Facet.MOST_FREQUENT_CONCEPTS_FACET);
		
		for (Facet facet : facets)
			facetsById.put(facet.getId(), facet);
		
		
		FacetGroup<Facet> searchFg = new FacetGroup<>("Related Concepts", 0);
		facetGroupsSearch.add(searchFg);
		
		searchFg.add(Facet.MOST_INFORMATIVE_CONCEPTS_FACET);
		searchFg.add(Facet.MOST_FREQUENT_CONCEPTS_FACET);
	}

	public List<Facet> getFacets() {
		return facets;
	}

	@Override
	public Facet getFacetByName(String facetName) {
		if (facets == null || facets.isEmpty()) {
			getFacets();
		}

		for (Facet facet : facets) {
			if (facet.getName().equals(facetName)) {
				return facet;
			}
		}

		return null;
	}

	
	@Override
	public Facet getFacetById(String id) {
		Facet facet = facetsById.get(id);
		if (null == facet)
			log.warn("Unknown facet was requested (ID: {}).", id);
		return facet;
	}
	
	@Override
	public List<Facet> getFacetsById(List<String> facetIds) {
		List<Facet> facets = new ArrayList<>(facetIds.size());
		for (String facetId : facetIds) {
			Facet facet = facetsById.get(facetId);
			if (null == facet) {
				log.warn("Facet with ID {} not found.", facetId);
				continue;
			}
			facets.add(facet);
		}
		return facets;
	}

	@Override
	public List<FacetGroup<Facet>> getFacetGroupsSearch() {
		return facetGroupsSearch;
	}
}
