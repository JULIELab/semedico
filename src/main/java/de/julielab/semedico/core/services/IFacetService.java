package de.julielab.semedico.core.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.Facet.FieldSource;
import de.julielab.semedico.core.Facet.Source;
import de.julielab.semedico.core.Facet.SourceType;
import de.julielab.semedico.core.FacetGroup;

public interface IFacetService {

	public final static int BIO_MED = 0;
	public final static int IMMUNOLOGY = 1;
	public final static int BIBLIOGRAPHY = 3;
	public final static int AGEING = 2;
	public final static int FILTER = 4;

	public static final int FIRST_AUTHOR_FACET_ID = 18;
	public static final int LAST_AUTHOR_FACET_ID = 19;
	public static final int PROTEIN_FACET_ID = 1;
	public static final int CONCEPT_FACET_ID = 22;
	
	public List<Facet> getFacets();
	public Facet getFacetWithId(Integer id);

//	public List<Facet> getFacetsWithType(int type);
	
	public Facet getFacetForIndex(String indexName);
	public Facet getKeywordFacet();
	public Facet getFacetWithName(String facetName);
	
	public List<FacetGroup<Facet>> getFacetGroups();
}
