package de.julielab.semedico.core.services.interfaces;

import java.util.List;
import java.util.Set;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetGroup;

public interface IFacetService {

	public final static int BIO_MED = 0;
	public final static int IMMUNOLOGY = 1;
	public final static int BIBLIOGRAPHY = 3;
	public final static int AGEING = 2;
	public final static int FILTER = 4;
	public final static int BTERMS = 5;

	public static final int FACET_ID_FIRST_AUTHORS = 18;
	public static final int FACET_ID_LAST_AUTHORS = 19;
	public static final int FACET_ID_AUTHORS = 39;
	public static final int PROTEIN_FACET_ID = 1;
	public static final int CONCEPT_FACET_ID = 22;
	
	public static final int BTERMS_FACET = 40;
	
	public List<Facet> getFacets();
	public Facet getFacetById(Integer id);

//	public List<Facet> getFacetsWithType(int type);
	
	public Facet getFacetByIndexFieldName(String indexName);
	public Facet getKeywordFacet();
	public Facet getFacetByName(String facetName);
	
	public List<FacetGroup<Facet>> getFacetGroupsSearch();
	
	/**
	 * @return
	 */
	List<FacetGroup<Facet>> getFacetGroupsBTerms();
	
	public Set<Facet> getStringTermFacets();
	
	public Facet getAuthorFacet();
	public Facet getFirstAuthorFacet();
	public Facet getLastAuthorFacet();
	/**
	 * @param id
	 * @return
	 */
	public boolean isAnyAuthorFacetId(Integer id);
	/**
	 * @param facet
	 * @return
	 */
	public boolean isAnyAuthorFacet(Facet facet);
}
