package de.julielab.semedico.core.facetterms;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.interfaces.IFacetTerm;
import de.julielab.semedico.core.services.interfaces.ITermService;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FacetTerm extends Concept implements IFacetTerm {

	private List<String> sourceIds;
	private String originalId;

	public FacetTerm() {
		super();
		this.sourceIds = Collections.emptyList();
	}

	public FacetTerm(String stringTermId, String termName) {
		super(stringTermId, termName);
	}

	/**
	 * For unit tests.
	 * 
	 * @param id
	 * @param termService
	 */
	public FacetTerm(String id, ITermService termService) {
		this.id = id;
		this.termService = termService;
	}

	/**
	 * For unit tests.
	 * 
	 * @param id
	 */
	public FacetTerm(String id) {
		this.id = id;
	}

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

	@Override
	public String getOriginalId() {
		return originalId;
	}

	@Override
	public List<String> getSourceIds() {
		return sourceIds;
	}

	@Override
	public void setOriginalId(String originalId) {
		this.originalId = originalId;

	}

	@Override
	public void setSourceIds(List<String> sourceIds) {
		this.sourceIds = sourceIds;

	}

	@Override
	public String toString() {
		return "FacetTerm [id=" + id + ", preferredName=" + preferredName + "]";
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public ConceptType getConceptType() {
		return ConceptType.TERM;
	}

}
