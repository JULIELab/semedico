package de.julielab.semedico.pages;

import java.io.IOException;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;

import de.julielab.semedico.components.FacetedSearchLayout;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.FacetedSearchResult;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.search.interfaces.IFacetedSearchService;
import de.julielab.semedico.util.LazyDisplayGroup;

public class ResultList {

	private static int MAX_DOCS_PER_PAGE = 10;
	private static int MAX_BATCHES = 5;

	@InjectPage
	private Index index;

	@InjectComponent("FacetedSearchLayout")
	private FacetedSearchLayout searchLayout;

	@SessionState(create = false)
	@Property
	private SearchState searchState;

	// Only used to be passed to the FacetedSearchLayout component.
	@SuppressWarnings("unused")
	@SessionState
	@Property
	private UserInterfaceState uiState;

	@Inject
	private IFacetedSearchService searchService;

	@Property
	@Persist
	private LazyDisplayGroup<DocumentHit> displayGroup;

	@SuppressWarnings("unused")
	@Property
	@Persist
	// Used for display only.
	private long elapsedTime;

	/**
	 * <p>
	 * Event handler which is executed before beginning page rendering.
	 * </p>
	 * <p>
	 * The main page will check whether there is a search whose search results
	 * could be displayed. If not, the user is redirected to the Index page.
	 * </p>
	 * 
	 * @return The Index page if there is no search to display. Otherwise, null
	 *         will be returned to signal the page rendering.
	 * @see http://tapestry.apache.org/page-navigation.html
	 */
	public Object onActivate() {
		if (searchState == null)
			return index;
		return null;
	}


	@OnEvent(value = "switchToSearchNode")
	public Object onActionFromQueryPanel() throws IOException {
		FacetedSearchResult searchResult = searchService.search(searchState
				.getQueryTerms(), IFacetedSearchService.DO_FACET);
		setSearchResult(searchResult);
		return this;
	}

	public ResultList onDisambiguateTerm() throws IOException {
		return searchLayout.performSubSearch();
	}

	public ResultList onRemoveTerm() throws IOException {
		return searchLayout.performSubSearch();
	}

	public ResultList onDrillUp() throws IOException {
		return searchLayout.performSubSearch();
	}

	public ResultList onDisableReviewFilter() throws IOException {
		return searchLayout.performSubSearch();
	}

	public ResultList onEnableReviewFilter() throws IOException {
		return searchLayout.performSubSearch();
	}

	/**
	 * @param result
	 */
	public void setSearchResult(FacetedSearchResult searchResult) {
		elapsedTime = searchResult.getElapsedTime();
		displayGroup = new LazyDisplayGroup<DocumentHit>(
				searchResult.getTotalHits(), MAX_DOCS_PER_PAGE, MAX_BATCHES,
				searchResult.getDocumentHits());
	}
}
