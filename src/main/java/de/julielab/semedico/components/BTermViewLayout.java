package de.julielab.semedico.components;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.apache.tapestry5.annotations.CleanupRender;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import de.julielab.semedico.base.Search;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.FacetedSearchResult;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.pages.Index;
import de.julielab.semedico.pages.ResultList;
import de.julielab.semedico.query.IQueryDisambiguationService;
import de.julielab.semedico.query.IQueryTranslationService;
import de.julielab.semedico.search.interfaces.IFacetedSearchService;
import de.julielab.semedico.util.LazyDisplayGroup;

/**
 * Central starting point of the whole of Semedico. While the index page may be
 * the entry point, all searching logic, facet configuration, facet expanding
 * etc. has its origin in this page.
 * 
 * @author faessler
 * 
 */
@Import(stylesheet = { "context:css/facets.css",
		"context:css/layout_hitlist.css" }, library = { "context:js/jquery-1.7.1.min.js" })
public class BTermViewLayout extends Search {

	@InjectPage
	private ResultList resultList;

	@Inject
	private IFacetService facetService;

	@Inject
	private ITermService termService;

	@Inject
	private IFacetedSearchService searchService;

	@Inject
	private IQueryDisambiguationService queryDisambiguationService;

	@Inject
	private IQueryTranslationService queryTanslationService;

	@Inject
	private Logger logger;

	@SessionState
	private SearchState searchState;

	// Just taken from the embedding page and passed further to the Tabs
	// component. This
	// way, the Tabs component "knows" which user interface to render, e.g.
	// search interface or B-Term-Viewing interface.
	@SuppressWarnings("unused")
	@Parameter
	@Property
	private UserInterfaceState uiState;

	@Persist
	@Property
	private int selectedFacetType;

	@Property
	@Persist
	private LabelStore currentFacetHit;

	@Persist
	@Property
	private LazyDisplayGroup<DocumentHit> displayGroup;

	private static int MAX_DOCS_PER_PAGE = 10;
	private static int MAX_BATCHES = 5;

	@Persist
	@Property
	private String originalQueryString;

	@Property
	@Parameter
	int indexOfFirstArticle;
	@Property
	@Parameter
	int indexOfLastArticle;
	@Property
	@Parameter
	long elapsedTime;
	@Property
	@Parameter
	long totalHits;

	public Object onTermSelect() {
		return null;
	}

	public ResultList performSubSearch() {
		FacetedSearchResult searchResult = searchService.search(searchState
				.getQueryTerms());
		resultList.setSearchResult(searchResult);
		setQuery(null);
		setTermId(null);
		setFacetId(null);
		return resultList;
	}

	public void resetConfigurations(
			Collection<FacetConfiguration> configurations) {
		for (FacetConfiguration configuration : configurations)
			configuration.reset();
	}

	/**
	 * Uses {@link FacetConfiguration#getCurrentPath()} to add all ancestors of
	 * the terms in <code>terms</code> to the current paths of the corresponding
	 * facet configurations. If a term in <code>terms</code> has no parent term,
	 * i.e. it is a root, the term itself is added to the current path of its
	 * facet configuration.
	 * <p>
	 * The {@link FacetBox} component associated with a particular facet
	 * configuration will then show the facet category drilled down to children
	 * of the last element of a path. The path itself is reflected on the
	 * {@link QueryPanel} component.
	 * </p>
	 * <p>
	 * If there are several terms of the same facet category in
	 * <code>terms</code>, the first term encountered will determine the set
	 * path. Following terms will not be reflected. (This is my understanding at
	 * least - EF).
	 * </p>
	 * <p>
	 * The facet configurations in <code>facetConfigurations</code> should be
	 * resetted before calling this method.
	 * </p>
	 * 
	 * @param terms
	 *            The term to which the different facet categories are currently
	 *            drilled down to.
	 * @param facetConfigurations
	 *            The facet configurations to set the current path to the
	 *            associated term in <code>terms</code.>
	 */
	protected void drillDownFacetConfigurations(Collection<IFacetTerm> terms,
			Map<Facet, FacetConfiguration> facetConfigurations) {

		for (IFacetTerm searchTerm : terms) {
			if (!searchTerm.hasChildren())
				continue;

			FacetConfiguration configuration = facetConfigurations
					.get(searchTerm.getFirstFacet());

			if (configuration.isHierarchical()
					&& configuration.getCurrentPathLength() == 0) {
				configuration.setCurrentPath(termService.getPathFromRoot(
						searchTerm).copyPath());
			}
		}
	}

	/*
	 * <p> Triggered when the autocomplete form is submitted. May return null
	 * when nothing has been typed into the text field. </p> <p> Note: As soon
	 * as anything has been typed into it, at least the last first character
	 * will always be remembered; more precisely, the autocompleter can be
	 * configured to trigger only when at least n characters have been typed in
	 * (see FacetSuggestionHitAutocomplete). Thus, when typing and then erasing
	 * everything, every erasure that leads to less then n characters will be
	 * ignored. </p>
	 */
	@Log
	public ResultList onSuccessFromSearch() throws IOException {
		return performNewSearch();
	}

	/*
	 * <p>Triggered when a suggestion is selected rather than hitting return or
	 * clicking on the search button. </p>
	 */
	@Log
	public ResultList onActionFromSearchInputField() throws IOException {
		return performNewSearch();
	}

	// public int getIndexOfFirstArticle() {
	// if (displayGroup.getDisplayedObjects().size() == 0)
	// return 0;
	// return displayGroup.getIndexOfFirstDisplayedObject() + 1;
	// }

	@Log
	public void onDownloadPmids() {
		// try {
		// searchResult = searchService.search(originalQueryString,
		// searchState.getQueryTerms().size(),
		// searchState.getSortCriterium(),
		// searchState.isReviewsFiltered());
		// Collection<String> pmids = searchService.getPmidsForSearch(
		// originalQueryString, searchState);
		// for (String pmid : pmids)
		// System.out.println(pmid);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	@CleanupRender
	public void cleanUpRender() {
		searchState.setNewSearch(false);
	}
}
