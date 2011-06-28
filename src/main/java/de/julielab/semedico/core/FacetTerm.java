package de.julielab.semedico.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.julielab.semedico.core.MultiHierarchy.MultiHierarchyNode;
import de.julielab.semedico.core.services.TermService;

public class FacetTerm extends MultiHierarchyNode implements Comparable<FacetTerm> {
	// The Term's name, e.g. "Hematopoiesis", "Cell Aggregation" etc.
//	private String label;

	// How this particular term is uniquely identified in the database and thus
	// for retrieval within the TermService.
//	private String internalIdentifier;

	// The Lucene index field names in whose this Term may occur.
	private Collection<String> indexNames;

	// The Facet this term belongs to.
	private List<Facet> facets;

	// The unique database ID of this term. As this ID depends on the order of
	// database import it may change when the database is set up from scratch
	// which makes the internalIdentifier more suitable as a global identifier.
	private Integer databaseId;

	/**
	 *  The Term which is the parent of this Term in the Term hierarchy.
	 *  Set in {@link TermService#readTermsWithSelectString} while reading
	 *  Terms from the database.
	 */
	private FacetTerm parent;

	/**
	 *  The child Terms of this Term in the Term hierarchy.
	 *  Set in {@link TermService#readTermsWithSelectString} while reading
	 *  Terms from the database.
	 */
	private List<FacetTerm> subTerms;

	// Short description of this term.
	private String shortDescription;

	// (Long) description of this term.
	private String description;
	private String kwicQuery;
	
	/**
	 * This Term's position in the list of Terms associated with this Term's facet.
	 * Set in {@link TermService#registerTerm(FacetTerm)}.
	 */
	private int facetIndex;

	private List<FacetTerm> parents;

//	public FacetTerm(Integer id) {
//		super();
//		this.databaseId = id;
//		subTerms = new ArrayList<FacetTerm>();
//	}

//	public FacetTerm() {
//		subTerms = new ArrayList<FacetTerm>();
//		this.databaseId = -1;
//	}

	public FacetTerm(String internalIdentifier, String name) {
		super(internalIdentifier, name);
		subTerms = new ArrayList<FacetTerm>();
		this.databaseId = -1;
		facets = new ArrayList<Facet>();
	}

//	public String getLabel() {
//		return label;
//	}
//
//	public void setLabel(String label) {
//		this.label = label;
//	}

	public Facet getFirstFacet() {
		return facets.get(0);
	}
	
	public List<Facet> getFacets() {
		return facets;
	}

	public void addFacet(Facet facet) {
		this.facets.add(facet);
	}

	public Integer getDatabaseId() {
		return databaseId;
	}

	public void setDatabaseId(Integer id) {
		this.databaseId = id;
	}

//	public FacetTerm getParent() {
//		return parent;
//	}
//
//	public void setParent(FacetTerm parent) {
//		this.parent = parent;
//	}
//
//	public List<FacetTerm> getSubTerms() {
//		return subTerms;
//	}
//
//	public void setSubTerms(List<FacetTerm> subTerms) {
//		this.subTerms = subTerms;
//		for (FacetTerm subTerm : subTerms)
//			subTerm.setParent(this);
//	}
//
//	/**
//	 * Adds all ancestors of this term.
//	 * @return
//	 */
//	public synchronized List<FacetTerm> getAllParents() {
//		if (parent == null)
//			return Collections.EMPTY_LIST;
//
//		if (parents == null)
//			parents = new ArrayList<FacetTerm>();
//		else
//			return parents;
//
//		FacetTerm parent = getParent();
//		while (parent != null) {
//			parents.add(parent);
//			parent = parent.getParent();
//		}
//
//		Collections.reverse(parents);
//		return parents;
//	}

	@Override
	public String toString() {
		String string = "{ internalIdentifier:" + id
				+ "; name: " + name + "; indexes: " + indexNames + "; facet:"
				+ facets + "; " + " facetIndex: " + facetIndex + "; kwicQuery: "
				+ kwicQuery + "; " + super.toString() + ";}";
		return string;
	}

//	public boolean isOnPath(List<FacetTerm> path) {
//		int i = 0;
//		boolean allHit = true;
//		List<FacetTerm> parents = getAllParents();
//
//		for (FacetTerm term : path) {
//			if (i < parents.size())
//				allHit &= parents.get(i).equals(term);
//
//			i++;
//		}
//
//		return allHit;
//	}

	public String getShortDescription() {
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

//	public boolean isChildTerm(FacetTerm term) {
//		FacetTerm parent = this.parent;
//		while (parent != null) {
//			if (term.equals(parent))
//				return true;
//			parent = parent.getParent();
//		}
//		return false;
//	}
//
//	public boolean isParentTerm(FacetTerm term) {
//		FacetTerm parent = this.parent;
//		while (parent != null) {
//			if (parent.equals(term))
//				return true;
//			parent = parent.getParent();
//		}
//
//		return false;
//	}

	public Collection<String> getIndexNames() {
		return indexNames;
	}

	public void setIndexNames(Collection<String> indexNames) {
		this.indexNames = indexNames;
	}

	@Override
	public int compareTo(FacetTerm term) {

		return databaseId - term.getDatabaseId();
	}
}
