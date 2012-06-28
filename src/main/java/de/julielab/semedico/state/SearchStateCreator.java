package de.julielab.semedico.state;

import org.apache.tapestry5.services.ApplicationStateCreator;

import de.julielab.semedico.core.SearchState;

/**
 * 
 * @author faessler
 * 
 */
public class SearchStateCreator implements
		ApplicationStateCreator<SearchState> {

	// Global ID counter for search sessions.
	private static int idCounter = 0;

	public SearchState create() {
		return new SearchState(idCounter++);
	}
}
