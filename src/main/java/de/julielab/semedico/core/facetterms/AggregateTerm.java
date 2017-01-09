package de.julielab.semedico.core.facetterms;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.julielab.semedico.core.TermLabels;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.interfaces.IFacetTermRelation;

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

	@Override
	public boolean isKeyword() {
		return false;
	}

	@Override
	public boolean isAggregate() {
		return true;
	}

	@Override
	public EventType getEventType() {
		EventType aggregateEventType = EventType.NONE;
		for (Concept element : getElements()) {
			switch (element.getEventType()) {
			case BINARY:
				if (aggregateEventType == EventType.UNARY)
					aggregateEventType = EventType.BOTH;
				else
					aggregateEventType = EventType.BINARY;
				break;
			case BOTH:
				aggregateEventType = EventType.BOTH;
				break;
			case NONE:
				break;
			case UNARY:
				if (aggregateEventType == EventType.BINARY)
					aggregateEventType = EventType.BOTH;
				else
					aggregateEventType = EventType.UNARY;
				break;
			}
		}
		return aggregateEventType;
	}

	/**
	 * Returns the elements that are represented by this aggregate node.
	 * 
	 * @return
	 */
	public List<Concept> getElements() {
		loadChildren();
		List<Concept> ret = new ArrayList<>();
		for (IFacetTermRelation rel : outgoingRelationships.get(IFacetTermRelation.Type.HAS_ELEMENT.name())) {
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
		if ((isAggregate() || childrenInFacets.size() > 0) && outgoingRelationships.size() == 0
				&& !childrenHaveBeenLoaded) {
			childrenHaveBeenLoaded = true;
			termService.loadChildrenOfTerm(this, TermLabels.GeneralLabel.MAPPING_AGGREGATE.name());
		}
	}

	@Override
	public ConceptType getConceptType() {
		return ConceptType.AGGREGATE_TERM;
	}

}
