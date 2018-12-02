package de.julielab.semedico.state.tabs;

import de.julielab.semedico.core.entities.state.SearchState;
import de.julielab.semedico.core.entities.state.UserInterfaceState;

/**
 * A <em>search context</em> denotes a document retrieval process where the user is exploring the literature by means of
 * terms and facts.
 * 
 * @see BTermRetrievalTab
 * @author faessler
 * 
 */
public class DocumentRetrievalTab extends ApplicationTab {
	private SearchState searchState;
	private UserInterfaceState uiState;

	public DocumentRetrievalTab(String name, int tabIndex, SearchState searchState, UserInterfaceState uiState) {
		super(name, tabIndex, TabType.DOC_RETRIEVAL);
		this.searchState = searchState;
		this.uiState = uiState;
	}

	public SearchState getSearchState() {
		return searchState;
	}

	public UserInterfaceState getUiState() {
		return uiState;
	}

	@Override
	public String getStartPageName() {
		return "Index";
	}

}
