package de.julielab.semedico.core.services.interfaces;

import de.julielab.semedico.commons.concepts.FacetLabels;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IFacetService {


    /**
     * Returns the facet with label <tt>facetLabel</tt> (optional) that has been
     * induced by the term with ID <tt>termId</tt>.
     *
     * @param termId
     * @param facetLabel
     * @return
     */
    Facet getInducedFacet(String termId, FacetLabels.General facetLabel);

    Facet getAuthorFacet();

    Facet getFacetById(String id);

    Facet getFacetByIndexFieldName(String indexName);

    //   List<Facet> getFacetsWithType(int type);

    Facet getFacetByLabel(FacetLabels.Unique label);

    Facet getFacetByName(String facetName);

    List<FacetGroup<Facet>> getFacetGroupsSearch();

    List<Facet> getFacets();

    List<Facet> getFacetsById(List<String> facetIds);

    Facet getFirstAuthorFacet();

    Facet getKeywordFacet();

    Facet getLastAuthorFacet();

    Set<Facet> getStringTermFacets();

    List<Facet> getSuggestionFacets();

    Collection<Facet> getTermSourceFacets();

    /**
     * @param facetFieldName
     * @return
     */
    boolean isTotalFacetCountField(String facetFieldName);

    List<Facet> getFacetsByLabel(FacetLabels.General label);

    List<Facet> getFacetsByLabels(Set<FacetLabels.General> labels);

}
