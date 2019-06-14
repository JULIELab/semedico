package de.julielab.semedico.core.facets;

import de.julielab.semedico.commons.concepts.FacetLabels;
import de.julielab.semedico.core.services.interfaces.IConceptService;

import java.util.Collection;
import java.util.Set;

public class BioPortalFacet extends Facet {
	private String acronym;
	private String iri;

	public BioPortalFacet(String id, String name, Collection<String> searchFieldNames,
                          Collection<String> filterFieldName, Set<FacetLabels.General> generalLabels, Set<FacetLabels.Unique> uniqueLabels, int position,
                          String cssId, FacetSource facetSource, IConceptService termService, String acronym, String iri) {
		super(id, name, searchFieldNames, filterFieldName, generalLabels, uniqueLabels, position, cssId, facetSource,
				termService);
		this.acronym = acronym;
		this.iri = iri;
	}

	/**
	 * For unit tests.
	 * 
	 * @param id
	 * @param acronym
	 */
	public BioPortalFacet(String id, String acronym) {
		super(id);
		this.acronym = acronym;
	}

	public String getAcronym() {
		return acronym;
	}

	public String getIri() {
		return iri;
	}

}
