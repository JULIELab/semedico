package de.julielab.semedico.core.search.components.data;

import java.util.List;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.entities.documents.SemedicoDocument;

public class HighlightedStatement {
	private SemedicoDocument parentDocument;
	private Highlight statementHighlight;
	/**
	 * The choice of the type {@link IConcept} allows us to use actual concepts
	 * to describe the predicate but also simple strings in the form of keyword
	 * terms.
	 */
	private IConcept predicate;
	/**
	 * Similarly to the predicate, arguments may also be actual concepts or
	 * keywords.
	 */
	private List<IConcept> arguments;

	public SemedicoDocument getParentDocument() {
		return parentDocument;
	}

	public void setParentDocument(SemedicoDocument parentDocument) {
		this.parentDocument = parentDocument;
	}

	public Highlight getStatementHighlight() {
		return statementHighlight;
	}

	public void setStatementHighlight(Highlight statementHighlight) {
		this.statementHighlight = statementHighlight;
	}

	public IConcept getPredicate() {
		return predicate;
	}

	public void setPredicate(IConcept predicate) {
		this.predicate = predicate;
	}

	public List<IConcept> getArguments() {
		return arguments;
	}

	public void setArguments(List<IConcept> arguments) {
		this.arguments = arguments;
	}
}
