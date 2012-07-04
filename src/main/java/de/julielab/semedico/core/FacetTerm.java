package de.julielab.semedico.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import de.julielab.semedico.core.services.TermService;
import de.julielab.semedico.core.taxonomy.MultiHierarchyNode;

public class FacetTerm extends MultiHierarchyNode  {
	// The Lucene index field names in whose this Term may occur.
	private Collection<String> indexNames;

	// The Facets this term belongs to in an ordered manner.
	private List<Facet> facets;
	// The Facets this term belongs to for element-checks.
	private Set<Facet> facetSet;

	/**
	 * The Term which is the parent of this Term in the Term hierarchy. Set in
	 * {@link TermService#readTermsWithSelectString} while reading Terms from
	 * the database.
	 */
	private FacetTerm parent;

	/**
	 * The child Terms of this Term in the Term hierarchy. Set in
	 * {@link TermService#readTermsWithSelectString} while reading Terms from
	 * the database.
	 */
	private List<FacetTerm> subTerms;

	// Short description of this term.
	private String shortDescription;

	// (Long) description of this term.
	private String description;
	private String kwicQuery;

	/**
	 * This Term's position in the list of Terms associated with this Term's
	 * facet. Set in {@link TermService#registerTerm(FacetTerm)}.
	 */
	private int facetIndex;

	private List<FacetTerm> parents;

	// public FacetTerm(Integer id) {
	// super();
	// this.databaseId = id;
	// subTerms = new ArrayList<FacetTerm>();
	// }

	// public FacetTerm() {
	// subTerms = new ArrayList<FacetTerm>();
	// this.databaseId = -1;
	// }

	public FacetTerm(String internalIdentifier, String name) {
		super(internalIdentifier, name);
		subTerms = new ArrayList<FacetTerm>();
		facets = new ArrayList<Facet>();
		facetSet = new HashSet<Facet>();
	}

	// public String getLabel() {
	// return label;
	// }
	//
	// public void setLabel(String label) {
	// this.label = label;
	// }

	public Facet getFirstFacet() {
		for (Facet facet : facets)
			if (facet != Facet.KEYWORD_FACET)
				return facet;
		return facets.get(0);
	}

	public List<Facet> getFacets() {
		return facets;
	}

	public void addFacet(Facet facet) {
		this.facets.add(facet);
		this.facetSet.add(facet);
	}


	@Override
	public String toString() {
		List<String> facetNames = new ArrayList<String>();
		for (Facet facet : facets)
			facetNames.add(facet.getName());
		String string = "{ internalIdentifier:" + id + "; name: " + name
				+ "; indexes: " + indexNames + "; facet:"
				+ StringUtils.join(facetNames, ", ") + "; " + " facetIndex: "
				+ facetIndex + "; kwicQuery: " + kwicQuery + "; "
				+ super.toString() + ";}";
		return string;
	}

	public String getSynonyms() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getKwicQuery() {
		return kwicQuery;
	}

	public void setKwicQuery(String kwicQuery) {
		this.kwicQuery = kwicQuery;
	}

	public int getFacetIndex() {
		return facetIndex;
	}

	public void setFacetIndex(int facetIndex) {
		this.facetIndex = facetIndex;
	}

	public Collection<String> getIndexNames() {
		return indexNames;
	}

	public void setIndexNames(Collection<String> indexNames) {
		this.indexNames = indexNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.Taxonomy.IFacetTerm#isContainedInFacet(de.julielab
	 * .semedico.core.Facet)
	 */
	@Override
	public boolean isContainedInFacet(Facet otherFacet) {
		return facetSet.contains(otherFacet);
	}

	// The Term's name, e.g. "Hematopoiesis", "Cell Aggregation" etc.
	// private String label;

	// How this particular term is uniquely identified in the database and thus
	// for retrieval within the TermService.
	// private String internalIdentifier;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object otherObject) {
		if (!(otherObject instanceof FacetTerm))
			return false;
		FacetTerm otherTerm = (FacetTerm) otherObject;
		return this.id.equals(otherTerm.id);
	}
}
