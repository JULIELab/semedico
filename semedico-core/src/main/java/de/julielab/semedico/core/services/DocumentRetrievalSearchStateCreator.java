package de.julielab.semedico.core.services;

import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.services.interfaces.IDocumentRetrievalSearchStateCreator;

public class DocumentRetrievalSearchStateCreator implements IDocumentRetrievalSearchStateCreator {

	@Override
	public SearchState create() {
		return new SearchState();
	}
}
