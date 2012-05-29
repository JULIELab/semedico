package de.julielab.semedico.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tapestry5.annotations.CleanupRender;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.base.Search;
import de.julielab.semedico.components.FacetBox;
import de.julielab.semedico.components.QueryPanel;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.FacettedSearchResult;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.SearchSessionState;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.SortCriterium;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.Taxonomy.IPath;
import de.julielab.semedico.core.services.IFacetService;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.query.IQueryDisambiguationService;
import de.julielab.semedico.query.IQueryTranslationService;
import de.julielab.semedico.search.IFacetedSearchService;
import de.julielab.semedico.util.LazyDisplayGroup;

/**
 * Central starting point of the whole of Semedico. While the index page may be
 * the entry point, all searching logic, facet configuration, facet expanding
 * etc. has its origin in this page.
 * 
 * @author landefeld/faessler
 * 
 */
@Import(stylesheet = { "context:css/facets.css",
		"context:css/layout_hitlist.css" }, library = { "context:js/jquery-1.7.1.min.js" })
public class Main extends Search {

	@InjectPage
	private Index index;

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

	@Property
	@SessionState
	private SearchSessionState searchConfiguration;

	@Persist
	@Property
	private int selectedFacetType;

	@Property
	@Persist
	private FacetHit currentFacetHit;

	@Property
	@Persist
	private FacettedSearchResult searchResult;

	@Persist
	@Property
	private LazyDisplayGroup<DocumentHit> displayGroup;

	@Persist
	@Property
	private long elapsedTime;

	private static int MAX_DOCS_PER_PAGE = 10;
	private static int MAX_BATCHES = 5;

	@Persist
	private UserInterfaceState uiState;

	@Persist
	@Property
	private SearchState searchState;

	@Persist
	@Property
	private int pubMedId;

	@Persist
	@Property
	private String originalQueryString;

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
		if (searchConfiguration.getSearchState().getRawQuery() == null)
			return index;
		return null;
	}

	@SetupRender
	public void initialize() {
		uiState = searchConfiguration.getUiState();
		searchState = searchConfiguration.getSearchState();

		// newSearch = true;
	}

	public void onShowArticle(int pmid) throws IOException {
		pubMedId = pmid;
	}

	@Log
	public void onDisambiguateTerm() throws IOException {
		Multimap<String, IFacetTerm> queryTerms = searchConfiguration
				.getSearchState().getQueryTerms();
		IFacetTerm selectedTerm = searchConfiguration.getSearchState()
				.getDisambiguatedTerm();
		logger.debug("Selected term from disambiguation panel: " + selectedTerm);
		String currentEntryKey = null;
		for (Map.Entry<String, IFacetTerm> queryTermEntry : queryTerms
				.entries()) {
			if (queryTermEntry.getValue().equals(selectedTerm)) {
				currentEntryKey = queryTermEntry.getKey();
			}
			logger.debug("Term in queryTerms: "
					+ queryTermEntry.getValue().getName());
		}
		queryTerms.removeAll(currentEntryKey);
		queryTerms.put(currentEntryKey, selectedTerm);
		doSearch(queryTerms, searchConfiguration.getSearchState()
				.getSortCriterium(), searchConfiguration.getSearchState()
				.isReviewsFiltered());
	}

	public void onDrillUp() throws IOException {
		doSearch(searchConfiguration.getSearchState().getQueryTerms(),
				searchConfiguration.getSearchState().getSortCriterium(),
				searchConfiguration.getSearchState().isReviewsFiltered());
	}

	public Object onActionFromQueryPanel() throws IOException {
		return doSearch(searchConfiguration.getSearchState().getQueryTerms(),
				searchConfiguration.getSearchState().getSortCriterium(),
				searchConfiguration.getSearchState().isReviewsFiltered());
	}

	public void onDisableReviewFilter() throws IOException {
		doSearch(searchConfiguration.getSearchState().getQueryTerms(),
				searchConfiguration.getSearchState().getSortCriterium(),
				searchConfiguration.getSearchState().isReviewsFiltered());
	}

	public void onEnableReviewFilter() throws IOException {
		doSearch(searchConfiguration.getSearchState().getQueryTerms(),
				searchConfiguration.getSearchState().getSortCriterium(),
				searchConfiguration.getSearchState().isReviewsFiltered());
	}

	public void onTermSelect(String termIndexFacetIdPathLength)
			throws IOException {
		setQuery(null);
		Multimap<String, IFacetTerm> queryTerms = searchConfiguration
				.getSearchState().getQueryTerms();
		Label selectedLabel = searchConfiguration.getSearchState()
				.getSelectedTerm();
		if (selectedLabel == null) {
			throw new IllegalStateException(
					"The IFacetTerm object reflecting the newly selected term is null.");
		}
		logger.debug("Name of newly selected label: {} (ID: {})",
				selectedLabel.getName(), selectedLabel.getId());
		// Get the FacetConfiguration associated with the selected term.
		String[] facetIdPathLength = termIndexFacetIdPathLength.split("_");
		int selectedFacetId = Integer.parseInt(facetIdPathLength[1]);
		Facet selectedFacet = facetService.getFacetById(selectedFacetId);

		IFacetTerm selectedTerm;
		boolean selectedTermIsAlreadyInQuery = false;
		Multimap<String, IFacetTerm> newQueryTerms = HashMultimap.create();

		if (selectedLabel instanceof TermLabel) {
			selectedTerm = ((TermLabel) selectedLabel).getTerm();
			logger.debug(
					"Searching for ancestors of {} in the query for refinement...",
					selectedTerm.getName());
			// We have to take caution when refining a term. Only the
			// deepest term of each root-node-path in the hierarchy may be
			// included in our queryTerms map.
			// Reason 1: The root-node-path of _each_ term in queryTerms is
			// computed automatically in the QueryPanel
			// currently.
			// Reason 2: We associate refined terms with the (user) query string
			// of the original term. Multiple terms per string -> disambiguation
			// triggers.
			IPath rootPath = termService.getPathFromRoot(selectedTerm);
			String refinedQueryStr = null;
			// Build a new queryTerms map with all not-refined terms.
			// The copying is done because in rare cases writing on the
			// queryTokens map while iterating over it can lead to a
			// ConcurrentModificationException.
			for (Map.Entry<String, IFacetTerm> entry : queryTerms.entries()) {
				String queryToken = entry.getKey();
				IFacetTerm term = entry.getValue();

				IPath potentialAncestorRootPath = termService
						.getPathFromRoot(term);

				if (!rootPath.containsNode(term)
						&& !potentialAncestorRootPath
								.containsNode(selectedTerm))
					newQueryTerms.put(queryToken, term);
				else {
					// If there IS a term in queryTerms which lies on the root
					// path, just memorize its key. Except its the exact term
					// which
					// has been selected. This can happen when a facet has been
					// drilled up and the same term is selected again.
					if (term.equals(selectedTerm))
						selectedTermIsAlreadyInQuery = true;
					refinedQueryStr = queryToken;
					logger.debug(
							"Found ancestor of {} in current search query: {}",
							selectedTerm.getName(), term.getName());
				}
			}
			if (!selectedTermIsAlreadyInQuery) {
				// If there was an ancestor of the selected term in queryTerms,
				// now
				// associate the new term with its ancestor's query string.
				if (refinedQueryStr != null) {
					logger.debug("Ancestor found, refining the query.");
					newQueryTerms.put(refinedQueryStr, selectedTerm);
				} else {
					// Otherwise, add a new mapping.
					logger.debug("No ancestor found, add the term into the current search query.");

					// Associate the new term with its ID as query string.
					newQueryTerms.put(selectedTerm.getId(), selectedTerm);
					// Append the new term to the raw query
				}
			}
		} else {
			logger.debug("String label (with no associated term) selected. Creating special FacetTerm object.");
			// What about a FacetTermFactory? It could cache these things and
			// offer proper methods for terms with a facet vs. key terms.
			// TODO: Now we have kind of a factory, what about caching? Could
			// happen right inside of StringTermService
			selectedTerm = termService.getTermObjectForStringTerm(
					selectedLabel.getName(), selectedFacet);
			// selectedTerm = new FacetTerm("\"" + selectedLabel.getId() + "\"",
			// selectedLabel.getName());
			// selectedTerm.addFacet(selectedFacet);
			// selectedTerm.setIndexNames(selectedFacet.getFilterFieldNames());
			if (queryTerms.values().contains(selectedTerm))
				selectedTermIsAlreadyInQuery = true;
			else
				queryTerms.put(selectedTerm.getId(), selectedTerm);
			newQueryTerms = queryTerms;
		}

		if (!selectedTermIsAlreadyInQuery) {
			List<String> allTerms = new ArrayList<String>();
			for (String name : newQueryTerms.keySet())
				for (IFacetTerm term : newQueryTerms.get(name))
					allTerms.add(name + ": " + term.getName());
			logger.info("New term added to query. Current queryTerms content: '"
					+ StringUtils.join(allTerms, "', '") + "'");

			searchConfiguration.getSearchState().setQueryTerms(newQueryTerms);
			searchConfiguration.getSearchState().getQueryTermFacetMap()
					.put(selectedTerm, selectedFacet);

			doSearch(searchConfiguration.getSearchState().getQueryTerms(),
					searchConfiguration.getSearchState().getSortCriterium(),
					searchConfiguration.getSearchState().isReviewsFiltered());
		} else {
			logger.debug("Selected term is already contained in the query. No changes made.");
			Map<Facet, FacetConfiguration> facetConfigurations = uiState
					.getFacetConfigurations();
			FacetConfiguration facetConfiguration = facetConfigurations
					.get(selectedFacet);
			uiState.createLabelsForFacet(facetConfiguration);
		}
	}

	// called by the Index page
	public Object doNewSearch(String query,
			Pair<String, String> termIdAndFacetId) throws IOException {
		// This seemingly has happened before when coming from
		// 'onSuccessFromSearch', yet I don't know why or how.
		if (searchState == null) {
			logger.warn("Search state was null!");
			return this;
		}

		// May happen when nothing is typed into the search field but the search
		// is triggered (e.g. by hitting return) anyway.
		if (query == null && termIdAndFacetId == null)
			return this;

		logger.info(
				"New search has been triggered. Entered query: \"{}\", Term-ID: \"{}\".",
				query, termIdAndFacetId.getLeft());
		searchState.setNewSearch(true);
		Multimap<String, IFacetTerm> queryTerms = queryDisambiguationService
				.disambiguateQuery(query, termIdAndFacetId);

		setQuery(query);

		searchState.setRawQuery(getQuery());
		searchState.setQueryTerms(queryTerms);
		Map<IFacetTerm, Facet> queryTermFacetMap = searchState
				.getQueryTermFacetMap();

		for (IFacetTerm queryTerm : queryTerms.values())
			queryTermFacetMap.put(queryTerm, queryTerm.getFirstFacet());

		if (queryTerms.size() == 0)
			return Index.class;

		searchConfiguration.reset();
		Map<Facet, FacetConfiguration> facetConfigurations = uiState
				.getFacetConfigurations();
		drillDownFacetConfigurations(queryTerms.values(), facetConfigurations);
		doSearch(queryTerms, searchState.getSortCriterium(),
				searchState.isReviewsFiltered());

		return this;
	}

	public Object doSearch(Multimap<String, IFacetTerm> queryTerms,
			SortCriterium sortCriterium, boolean reviewsFiltered)
			throws IOException {
		if (queryTerms.size() == 0)
			return Index.class;

		pubMedId = 0;

		long time = System.currentTimeMillis();
		// Release the used LabelHierarchy for re-use.
		uiState.getFacetHit().clear();

		logger.debug("Performing main search.");
		originalQueryString = queryTanslationService.createQueryFromTerms(
				queryTerms, searchState.getRawQuery());
		searchResult = searchService.search(originalQueryString,
				queryTerms.size(), sortCriterium, reviewsFiltered);
		logger.debug("Preparing child terms of displayed terms.");
		if (!uiState.prepareLabelsForSelectedFacetGroup())
			logger.debug("No children to prepare.");

		// If we found nothing, let's check whether there could have been a
		// spelling error.
		// TODO doesn't work currently, has to be fully replaced by Johannes'
		// work with Solr spellchecking.
		// if (searchResult.getTotalHits() == 0) {
		// spellingCorrections = createSpellingCorrections(queryTerms);
		// logger.info("adding spelling corrections: " + spellingCorrections);
		// if (spellingCorrections.size() != 0) {
		// spellingCorrectedQueryTerms = createSpellingCorrectedQueryTerms(
		// queryTerms, spellingCorrections);
		// logger.info("spelling corrected query"
		// + spellingCorrectedQueryTerms);
		//
		// searchResult = searchService.search(
		// spellingCorrectedQueryTerms, getQuery(), sortCriterium,
		// reviewsFiltered);
		// // searchConfiguration
		// // .setSpellingCorrectedQueryTerms(spellingCorrectedQueryTerms);
		// // searchConfiguration.setSpellingCorrections(spellingCorrections);
		//
		// }
		// }
		displayGroup = new LazyDisplayGroup<DocumentHit>(
				searchResult.getTotalHits(), MAX_DOCS_PER_PAGE, MAX_BATCHES,
				searchResult.getDocumentHits());

		currentFacetHit = searchResult.getFacetHit();

		elapsedTime = System.currentTimeMillis() - time;

		logger.info("Time for Solr search: " + elapsedTime + " ms");

		setQuery(null);
		setTermId(null);
		setFacetId(null);

		return this;
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
	 * configuration will then show the facet categorie drilled down to children
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
				configuration.setCurrentPath(termService
						.getPathFromRoot(searchTerm).copyPath());
			}
		}
	}

	public Object onRemoveTerm() throws IOException {
		setQuery(null);
		return doSearch(searchConfiguration.getSearchState().getQueryTerms(),
				searchConfiguration.getSearchState().getSortCriterium(),
				searchConfiguration.getSearchState().isReviewsFiltered());
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
	public void onSuccessFromSearch() throws IOException {
		if (getQuery() == null || getQuery().equals(""))
			setQuery(getAutocompletionQuery());
		doNewSearch(getQuery(), new ImmutablePair<String, String>(getTermId(),
				getFacetId()));
	}

	@Log
	public void onActionFromSearchInputField() throws IOException {
		if (getQuery() == null || getQuery().equals(""))
			setQuery(getAutocompletionQuery());

		doNewSearch(getQuery(), new ImmutablePair<String, String>(getTermId(),
				getFacetId()));
	}

	public int getIndexOfFirstArticle() {
		if (displayGroup.getDisplayedObjects().size() == 0)
			return 0;
		return displayGroup.getIndexOfFirstDisplayedObject() + 1;
	}

	public boolean isShowArticle() {
		return pubMedId > 0;
	}

	public void onDownloadPmids() {
		try {
			searchResult = searchService.search(originalQueryString,
					searchState.getQueryTerms().size(),
					searchState.getSortCriterium(),
					searchState.isReviewsFiltered());
			Collection<String> pmids = searchService.getPmidsForSearch(
					originalQueryString, searchState);
			for (String pmid : pmids)
				System.out.println(pmid);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@CleanupRender
	public void cleanUpRender() {
		searchState.setNewSearch(false);
	}
}
