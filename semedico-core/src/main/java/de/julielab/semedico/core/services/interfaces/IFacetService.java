package de.julielab.semedico.core.services.interfaces;

import de.julielab.semedico.commons.concepts.FacetLabels;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IFacetService {

	public final static int BIO_MED = 0;
	public final static int IMMUNOLOGY = 1;
	public final static int BIBLIOGRAPHY = 3;
	public final static int AGEING = 2;
	public final static int FILTER = 4;
	public final static int BTERMS = 5;
	public final static int BIOPORTAL = 6;

	// public static final int FACET_ID_CONCEPTS = 0;
	// public static final int FACET_ID_FIRST_AUTHORS = 18;
	// public static final int FACET_ID_LAST_AUTHORS = 19;
	// public static final int FACET_ID_AUTHORS = 39;
	// public static final int PROTEIN_FACET_ID = 1;
	//
	// public static final int FACET_ID_BTERMS = 40;

	/**
	 * Returns the facet with label <tt>facetLabel</tt> (optional) that has been
	 * induced by the term with ID <tt>termId</tt>.
	 * 
	 * @param termId
	 * @param facetLabel
	 * @return
	 */
	public Facet getInducedFacet(String termId, FacetLabels.General facetLabel);

	public Facet getAuthorFacet();

	public Facet getFacetById(String id);

	public Facet getFacetByIndexFieldName(String indexName);

	// public List<Facet> getFacetsWithType(int type);

	public Facet getFacetByLabel(FacetLabels.Unique label);

	public Facet getFacetByName(String facetName);

	/**
	 * @return
	 */
	List<FacetGroup<Facet>> getFacetGroupsBTerms();

	public List<FacetGroup<Facet>> getFacetGroupsSearch();

	public List<Facet> getFacets();

	public List<Facet> getFacetsById(List<String> facetIds);

	public Facet getFirstAuthorFacet();

	public Facet getKeywordFacet();

	public Facet getLastAuthorFacet();

	public Set<Facet> getStringTermFacets();

	public List<Facet> getSuggestionFacets();

	public Collection<Facet> getTermSourceFacets();

	/**
	 * @param facetFieldName
	 * @return
	 */
	public boolean isTotalFacetCountField(String facetFieldName);

	public List<Facet> getFacetsByLabel(FacetLabels.General label);

	public List<Facet> getFacetsByLabels(Set<FacetLabels.General> labels);

}
