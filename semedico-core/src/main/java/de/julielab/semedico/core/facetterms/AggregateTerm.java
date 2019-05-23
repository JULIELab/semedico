package de.julielab.semedico.core.facetterms;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.julielab.semedico.core.concepts.ConceptType;

/**
 * Aggregate concepts are representational nodes in the resource graph(s) that have not been defined themselves in one
 * of the data sources, which have been imported into the database, but are computed by building groups of terms that
 * should be assembled for some particular reasons. For instance, term mappings define sets of terms that are supposedly
 * equivalent to each other. With aggregates we define a single representation for each of such sets.
 * 
 * @author faessler
 * 
 */
public class AggregateTerm extends SyncFacetTerm {

	@JsonProperty("mappingType")
	private List<String> aggregationType;

	public List<String> getAggregationType() {
		return aggregationType;
	}

	public void setAggregationTypes(List<String> aggregationType) {
		this.aggregationType = aggregationType;
	}

	@Override
	public ConceptType getConceptType() {
		return ConceptType.AGGREGATE_TERM;
	}

}
