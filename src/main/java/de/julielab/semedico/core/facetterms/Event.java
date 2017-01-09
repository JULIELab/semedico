package de.julielab.semedico.core.facetterms;

import java.util.List;

import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.interfaces.IFacetTerm;

/**
 * Objects of this class are used to represent reificated events. We say 'reification' because an events is foremost
 * something 'happening' to an entity, perhaps explicitly caused by another entity. For a specific set of interacting
 * entities with a specific type of interaction - or event - an instance of this class is used to represent this very
 * interaction.
 * 
 * @author faessler
 * 
 */
public class Event extends Concept {

	/**
	 * A term describing the semantics of this event, e.g. regulation, phosphorylation etc.
	 */
	private IFacetTerm eventTerm;
	/**
	 * The entities this event refers to, e.g. genes, miRNA etc.
	 */
	private List<Concept> arguments;
	/**
	 * The likelihood that has been expressed within the respective text passage concerning this event. Can - and
	 * actually will in most cases - be null and thus does not specify a particular likelihood. This means 'all
	 * likelihoods', then.
	 */
	private String likelihood;

	public Event(String id, IFacetTerm eventTerm, List<Concept> arguments, String likelihood) {
		this.id = id;
		this.eventTerm = eventTerm;
		this.arguments = arguments;
		this.likelihood = likelihood;
		setNonDatabaseTerm(true);
	}

	@Override
	public boolean isKeyword() {
		return false;
	}

	@Override
	public EventType getEventType() {
		return eventTerm.getEventType();
	}

	public IFacetTerm getEventTerm() {
		return eventTerm;
	}

	public String getLikelihood() {
		return likelihood;
	}

	public void setLikelihood(String likelihood) {
		this.likelihood = likelihood;
	}

	public void setEventTerm(IFacetTerm eventTerm) {
		this.eventTerm = eventTerm;
	}

	public List<Concept> getArguments() {
		return arguments;
	}

	public void setArguments(List<Concept> arguments) {
		this.arguments = arguments;
	}

	@Override
	public boolean isAggregate() {
		return false;
	}

	@Override
	public ConceptType getConceptType() {
		return ConceptType.EVENT;
	}

	@Override
	public String toString() {
		return "Event [eventTerm=" + eventTerm + ", arguments=" + arguments + ", likelihood=" + likelihood + "]";
	}

	@Override
	public boolean isCoreTerm() {
		return false;
	}

	/**
	 * An event itself does not count as having a 'functional' role for event queries. This is because an event may
	 * currently not be the argument of another event.
	 */
	@Override
	public boolean isEventFunctional() {
		return false;
	}

}
