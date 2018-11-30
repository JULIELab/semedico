package de.julielab.semedico.core.services;

import de.julielab.semedico.core.entities.state.SearchState;
import de.julielab.semedico.core.services.interfaces.IDocumentRetrievalSearchStateCreator;

public class DocumentRetrievalSearchStateCreator implements IDocumentRetrievalSearchStateCreator {

	// Global ID counter for search sessions.
	private static long idCounter = 0;

	@Override
	public SearchState create() {
		return new SearchState();
	}
}
