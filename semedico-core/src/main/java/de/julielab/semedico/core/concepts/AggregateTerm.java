package de.julielab.semedico.core.concepts;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.julielab.semedico.core.TermLabels;
import de.julielab.semedico.core.concepts.interfaces.IConceptRelation;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate concepts are representational nodes in the resource graph(s) that have not been defined themselves in one
 * of the data sources, which have been imported into the database, but are computed by building groups of terms that
 * should be assembled for some particular reasons. For instance, term mappings define sets of terms that are supposedly
 * equivalent to each other. With aggregates we define a single representation for each of such sets.
 * 
 * @author faessler
 * 
 */
public class AggregateTerm extends SyncDbConcept {

	@JsonProperty("mappingType")
	private List<String> aggregationType;

	/**
	 * Returns the elements that are represented by this aggregate node.
	 * 
	 * @return
	 */
	public List<Concept> getElements() {
		loadChildren();
		List<Concept> ret = new ArrayList<>();
		for (IConceptRelation rel : outgoingRelationships.get(IConceptRelation.Type.HAS_ELEMENT.name())) {
			ret.add(rel.getEndNode());
		}
		return ret;
	}

	public List<String> getAggregationType() {
		return aggregationType;
	}

	public void setAggregationTypes(List<String> aggregationType) {
		this.aggregationType = aggregationType;
	}

	/**
	 * This method overrides the original loading method of Concept because we have to specify the MAPPING_AGGREGATE
	 * label to get the aggregate's children, i.e. its elements.
	 */
	@Override
	protected void loadChildren() {
		if ((getConceptType() == ConceptType.AGGREGATE_CONCEPT || childrenInFacets.size() > 0) && outgoingRelationships.size() == 0
				&& !childrenHaveBeenLoaded) {
			childrenHaveBeenLoaded = true;
			conceptService.loadChildrenOfTerm(this, TermLabels.GeneralLabel.MAPPING_AGGREGATE.name());
		}
	}

	@Override
	public ConceptType getConceptType() {
		return ConceptType.AGGREGATE_CONCEPT;
	}

}
