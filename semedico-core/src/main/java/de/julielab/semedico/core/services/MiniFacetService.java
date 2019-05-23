package de.julielab.semedico.core.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.Facet.Source;
import de.julielab.semedico.core.facets.Facet.SourceType;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.facets.FacetLabels.General;
import de.julielab.semedico.core.facets.FacetLabels.Unique;

public class MiniFacetService extends CoreFacetService {

	public MiniFacetService(Logger log) {
		super(log);

		List<Facet> searchFacets = new ArrayList<>();
		List<String> searchLocations = Arrays.asList("title", "text", "mesh");
		Facet diseases = new Facet(NodeIDPrefixConstants.FACET + 0, "Diseases");
		diseases.setSearchFieldNames(searchLocations);
		Facet chemicals = new Facet(NodeIDPrefixConstants.FACET + 1, "Chemicals");
		chemicals.setSearchFieldNames(searchLocations);
		searchFacets.add(diseases);
		searchFacets.add(chemicals);

		FacetGroup<Facet> searchFg = new FacetGroup<>("DefaultFacets", 0);
		facetGroupsSearch.add(searchFg);

		Source defaultSource = new Facet.Source(SourceType.FIELD_FLAT_TERMS, "conceptlist");
		for (Facet facet : searchFacets) {
			facet.setSource(defaultSource);
			facetsById.put(facet.getId(), facet);
			searchFg.add(facet);
		}

	}

	@Override
	public Facet getInducedFacet(String termId, General facetLabel) {
		return null;
	}

	@Override
	public Facet getAuthorFacet() {
		return null;
	}

	@Override
	public Facet getFacetByIndexFieldName(String indexName) {
		return null;
	}

	@Override
	public Facet getFacetByLabel(Unique label) {
		return null;
	}

	@Override
	public List<FacetGroup<Facet>> getFacetGroupsSearch() {
		return facetGroupsSearch;
	}

	@Override
	public Facet getFirstAuthorFacet() {
		return null;
	}

	@Override
	public Facet getKeywordFacet() {
		return null;
	}

	@Override
	public Facet getLastAuthorFacet() {
		return null;
	}

	@Override
	public Set<Facet> getStringTermFacets() {
		return null;
	}

	@Override
	public List<Facet> getSuggestionFacets() {
		return null;
	}

	@Override
	public Collection<Facet> getTermSourceFacets() {
		return null;
	}

	@Override
	public boolean isTotalFacetCountField(String facetFieldName) {
		return false;
	}

	@Override
	public List<Facet> getFacetsByLabel(General label) {
		return null;
	}

	@Override
	public List<Facet> getFacetsByLabels(Set<General> labels) {
		return null;
	}

}
