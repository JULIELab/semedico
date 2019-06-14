package de.julielab.semedico.commons.concepts;

import static de.julielab.semedico.commons.concepts.constants.SemedicFacetConstants.PROP_CSS_ID;
import static de.julielab.semedico.commons.concepts.constants.SemedicFacetConstants.PROP_FILTER_FIELD_NAMES;
import static de.julielab.semedico.commons.concepts.constants.SemedicFacetConstants.PROP_POSITION;
import static de.julielab.semedico.commons.concepts.constants.SemedicFacetConstants.PROP_SEARCH_FIELD_NAMES;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.julielab.neo4j.plugins.datarepresentation.ImportFacetGroup;
import de.julielab.semedico.commons.concepts.constants.SemedicFacetConstants;

public class SemedicoImportFacet extends de.julielab.neo4j.plugins.datarepresentation.ImportFacet {
	public SemedicoImportFacet(String name, String cssId, String sourceType, List<String> searchFieldNames,
			List<String> filterFieldNames, int position, List<String> labels, ImportFacetGroup facetGroup) {
		super(facetGroup, null, name, null, sourceType);
		this.name = name;
		this.cssId = cssId;
		this.sourceType = sourceType;
		this.searchFieldNames = searchFieldNames;
		this.filterFieldNames = filterFieldNames;
		this.position = position;
		this.labels = labels;
		this.facetGroup = facetGroup;
	}

	public SemedicoImportFacet(String id) {
		super(id);
	}

	@JsonProperty(PROP_CSS_ID)
	public String cssId;
	@JsonProperty(PROP_SEARCH_FIELD_NAMES)
	public List<String> searchFieldNames;
	@JsonProperty(PROP_FILTER_FIELD_NAMES)
	public List<String> filterFieldNames;
	@JsonProperty(PROP_POSITION)
	int position;
	@JsonProperty(SemedicFacetConstants.PROP_UNIQUE_LABELS)
	public List<String> uniqueLabels;
	/**
	 * @see SemedicFacetConstants#AGGREGATION_LABELS
	 */
	@JsonProperty(SemedicFacetConstants.AGGREGATION_LABELS)
	public List<String> aggregationLabels;
	/**
	 * @see SemedicFacetConstants#PROP_AGGREGATION_FIELDS
	 */
	@JsonProperty(SemedicFacetConstants.PROP_AGGREGATION_FIELDS)
	public List<String> aggregationFields;
	@JsonProperty(SemedicFacetConstants.PROP_INDUCING_TERM)
	public String incucingTerm;

	public void addUniqueLabel(String label) {
		if (uniqueLabels == null)
			uniqueLabels = new ArrayList<>();
		uniqueLabels.add(label);
	}

}
