package de.julielab.semedico.core.entities;

import de.julielab.semedico.core.services.ConceptNeo4jService;

/**
 * Used as key for cache loaders that require a term ID and a facet ID, for example the {@link ConceptNeo4jService.ShortestRootPathInFacetCacheLoader}
 * 
 * @author faessler
 * 
 */
public class TermFacetKey {
	private String termId;
	private String facetId;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((facetId == null) ? 0 : facetId.hashCode());
		result = prime * result + ((termId == null) ? 0 : termId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TermFacetKey other = (TermFacetKey) obj;
		if (facetId == null) {
			if (other.facetId != null)
				return false;
		} else if (!facetId.equals(other.facetId))
			return false;
		if (termId == null) {
			if (other.termId != null)
				return false;
		} else if (!termId.equals(other.termId))
			return false;
		return true;
	}

	public TermFacetKey(String termId, String facetId) {
		super();
		this.termId = termId;
		this.facetId = facetId;
	}

	public String getTermId() {
		return termId;
	}

	public void setTermId(String termId) {
		this.termId = termId;
	}

	public String getFacetId() {
		return facetId;
	}

	public void setFacetId(String facetId) {
		this.facetId = facetId;
	}
}
