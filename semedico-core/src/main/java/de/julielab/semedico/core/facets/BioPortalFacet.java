package de.julielab.semedico.core.facets;

import java.util.Collection;
import java.util.Set;

import de.julielab.semedico.core.facets.FacetLabels.General;
import de.julielab.semedico.core.facets.FacetLabels.Unique;
import de.julielab.semedico.core.services.interfaces.ITermService;

public class BioPortalFacet extends Facet {
	private String acronym;
	private String iri;

	public BioPortalFacet(String id, String name, Collection<String> searchFieldNames,
			Collection<String> filterFieldName, Set<General> generalLabels, Set<Unique> uniqueLabels, int position,
			String cssId, Source source, ITermService termService, String acronym, String iri) {
		super(id, name, searchFieldNames, filterFieldName, generalLabels, uniqueLabels, position, cssId, source,
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
