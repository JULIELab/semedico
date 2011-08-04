package de.julielab.semedico.core.services;

import java.util.List;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetGroup;

public interface IFacetService {

	public List<Facet> getFacets();
	public Facet getFacetWithId(Integer id);

	public List<Facet> getFacetsWithType(int type);
	
	public Facet getFacetForIndex(String indexName);
	public Facet getKeywordFacet();
	public Facet getFacetWithName(String facetName);
	
	public List<FacetGroup> copyFacetGroups();
}
