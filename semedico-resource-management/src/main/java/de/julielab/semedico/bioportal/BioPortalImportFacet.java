package de.julielab.semedico.bioportal;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import de.julielab.neo4j.plugins.datarepresentation.ImportFacet;
import de.julielab.neo4j.plugins.datarepresentation.ImportFacetGroup;
import de.julielab.semedico.core.facets.FacetProperties.BioPortal;

public class BioPortalImportFacet extends ImportFacet {

	@SerializedName(BioPortal.acronym)
	public String acronym;
	@SerializedName(BioPortal.IRI)
	public String iri;

	public BioPortalImportFacet(String name, String cssId, String sourceType, List<String> searchFieldNames,
			List<String> filterFieldNames, int position, List<String> generalLabels, ImportFacetGroup facetGroup) {
		super(name, cssId, sourceType, searchFieldNames, filterFieldNames, position, generalLabels, facetGroup);
	}

}
