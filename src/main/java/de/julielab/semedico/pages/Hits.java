package de.julielab.semedico.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.annotations.ApplicationState;
import org.apache.tapestry5.annotations.CleanupRender;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.base.Search;
import de.julielab.semedico.components.FacetBox;
import de.julielab.semedico.components.QueryPanel;
import de.julielab.semedico.core.Author;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.FacettedSearchResult;
import de.julielab.semedico.core.SearchConfiguration;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.core.SortCriterium;
import de.julielab.semedico.core.services.FacetService;
import de.julielab.semedico.core.services.IFacetService;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.query.IQueryDisambiguationService;
import de.julielab.semedico.search.IFacettedSearchService;
import de.julielab.semedico.spelling.ISpellCheckerService;
import de.julielab.semedico.util.LazyDisplayGroup;

/**
 * Central starting point of the whole of Semedico. While the index page may be
 * the entry point, all searching logic, facet configuration, facet expanding
 * etc. has its origin in this page.
 * 
 * @author landefeld/faessler
 * 
 */
public class Hits extends Search {

	@Inject
	private IFacetService facetService;

	@Inject
	private IFacettedSearchService searchService;

	@Inject
	private IQueryDisambiguationService queryDisambiguationService;

	@Inject
	private ISpellCheckerService spellCheckerService;

	@Inject
	private ITermService termService;

	@Property
	@Persist
	private FacettedSearchResult searchResult;

	@Property
	@Persist
	private FacetHit currentFacetHit;

	@Property
	@ApplicationState
	private SearchConfiguration searchConfiguration;

	@Persist
	private LazyDisplayGroup<DocumentHit> displayGroup;

	@Persist
	private long elapsedTime;

	@Property
	@Persist
	private FacetTerm selectedTerm;

	private static int MAX_DOCS_PER_PAGE = 10;
	private static int MAX_BATCHES = 5;

	@Property
	@Persist
	private Multimap<String, FacetTerm> spellingCorrectedQueryTerms;

	@Property
	private DocumentHit hitItem;

	@Property
	private int hitIndex;

	@Property
	private int authorIndex;

	@Property
	private Author authorItem;

	@Property
	private String kwicItem;

	@Property
	private int pagerItem;

	@Persist
	@Property
	private int selectedFacetType;

	/**
	 * Used to
	 */
	@Persist
	@Property
	private boolean newSearch;

	@Persist
	private Collection<FacetConfiguration> biomedFacetConfigurations;
	@Persist
	private Collection<FacetConfiguration> immunologyFacetConfigurations;
	@Persist
	private Collection<FacetConfiguration> bibliographyFacetConfigurations;
	@Persist
	private Collection<FacetConfiguration> agingFacetConfigurations;
	@Persist
	private Collection<FacetConfiguration> filterFacetConfigurations;

	@Property
	@Persist
	private Multimap<String, String> spellingCorrections;

	@Inject
	private Logger logger;

	// Notloesung solange die Facetten nicht gecounted werden; vllt. aber
	// ueberhaupt gar keine so schlechte Idee, wenn dann mal Facetten ohne
	// Treffer angezeigt werden. Dann aber in die Searchconfig einbauen evtl.
	// Wuerde das Explorieren der Facettenzweige ermöglichen, ohne dass es
	// Treffer für sie gibt.
	// NothingFound: Gibt an, ob nichts gefunden wurde. Wird an die QueryPanel
	// Komponente weitergegeben, damit die eine entsprechende Meldung rendern
	// kann.
	// removedParentTerm: Falls ein Term naeher bestimmt wurde, wurden dessen
	// Eltern
	// aus der TermQuery entfernt. Falls nun aber festgestellt wird, dass diese
	// Aktion
	// zu keinen Treffern fuehrt, wird der neue Term entfernt und der alte muss
	// wieder eingefuegt werden. Deshalb wird sich der alte Term hier gemerkt.
	// searchByTermSelect: Die Meldung ueber einen neuen Term, der die
	// Dokumenten-
	// Menge auf 0 reduziert macht nur Sinn, wenn wir nicht gerade eine
	// Suchquery
	// im Textfeld eingegeben haben. Deshalb checken wir noch, ob ueberhaupt
	// ein Term ausgewaehlt wurde.
	@Property
	@Persist
	private FacetTerm noHitTerm;
	@Persist
	private Object[] removedParentTerm;
	@Persist
	private boolean searchByTermSelect;

	public void initialize() {
		noHitTerm = null;
		searchByTermSelect = false;
		removedParentTerm = new Object[2];
		newSearch = true;
		this.selectedFacetType = Facet.BIO_MED;

		biomedFacetConfigurations = new ArrayList<FacetConfiguration>();
		immunologyFacetConfigurations = new ArrayList<FacetConfiguration>();
		bibliographyFacetConfigurations = new ArrayList<FacetConfiguration>();
		agingFacetConfigurations = new ArrayList<FacetConfiguration>();
		filterFacetConfigurations = new ArrayList<FacetConfiguration>();

		Map<Facet, FacetConfiguration> facetConfigurations = searchConfiguration
				.getFacetConfigurations();

		for (FacetConfiguration facetConfiguration : facetConfigurations
				.values()) {
			Facet facet = facetConfiguration.getFacet();
			if (facet.getType() == Facet.BIO_MED) {
				biomedFacetConfigurations.add(facetConfiguration);
			} else if (facet.getType() == Facet.IMMUNOLOGY) {
				immunologyFacetConfigurations.add(facetConfiguration);
			} else if (facet.getType() == Facet.BIBLIOGRAPHY) {
				bibliographyFacetConfigurations.add(facetConfiguration);
			} else if (facet.getType() == Facet.AGING) {
				agingFacetConfigurations.add(facetConfiguration);
			} else if (facet.getType() == Facet.FILTER) {
				filterFacetConfigurations.add(facetConfiguration);
			}
		}
	}

	public void onTermSelect(String termIndexFacetIdPathLength)
			throws IOException {
		setQuery(null);
		Multimap<String, FacetTerm> queryTerms = searchConfiguration
				.getQueryTerms();
		if (selectedTerm == null) {
			throw new IllegalStateException(
					"The FacetTerm object reflecting the newly selected term is null.");
		}
		logger.debug("Name of newly selected term: {} (ID: {})",
				selectedTerm.getName(), selectedTerm.getId());
		// Get the FacetConfiguration associated with the selected term.
		String[] facetIdPathLength = termIndexFacetIdPathLength.split("_");
		int selectedFacetId = Integer.parseInt(facetIdPathLength[1]);
		Facet selectedFacet = facetService.getFacetWithId(selectedFacetId);
		logger.debug("Searching for ancestors of {} in the query for refinement...",
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
		Multimap<String, FacetTerm> newQueryTerms = HashMultimap.create();
		List<FacetTerm> rootPath = termService.getPathFromRoot(selectedTerm);
		String refinedQueryStr = null;
		// Build a new queryTerms map with all not-refined terms.
		// The copying is done because in rare cases writing on the
		// queryTokens map while iterating over it can lead to a
		// ConcurrentModificationException.
		for (Map.Entry<String, FacetTerm> entry : queryTerms.entries()) {
			String queryToken = entry.getKey();
			FacetTerm term = entry.getValue();

			List<FacetTerm> potentialAncestorRootPath = termService
					.getPathFromRoot(term);

			if (!rootPath.contains(term)
					&& !potentialAncestorRootPath.contains(selectedTerm))
				newQueryTerms.put(queryToken, term);
			else {
				// If there IS a term in queryTerms which lies on the root
				// path, just memorize its key.
				refinedQueryStr = queryToken;
				logger.debug(
						"Found ancestor of {} in current search query: {}",
						selectedTerm.getName(), term.getName());
			}
		}
		// If there was an ancestor of the selected term in queryTerms, now
		// associate the new term with its ancestor's query string.
		if (refinedQueryStr != null) {
			logger.debug("Ancestor found, refining the query.");
			newQueryTerms.put(refinedQueryStr, selectedTerm);
		} else {
			// Otherwise, add a new mapping.
			logger.debug("No ancestor found, add the term into the current search query.");
			newQueryTerms.put(selectedTerm.getName(), selectedTerm);
		}
		searchConfiguration.setQueryTerms(newQueryTerms);
		searchConfiguration.getQueryTermFacetMap().put(selectedTerm,
				selectedFacet);

		doSearch(searchConfiguration.getQueryTerms(),
				searchConfiguration.getSortCriterium(),
				searchConfiguration.isReviewsFiltered());
	}

	@Log
	public void onDisambiguateTerm() throws IOException {
		Multimap<String, FacetTerm> queryTerms = searchConfiguration
				.getQueryTerms();
		logger.debug("Selected term from disambiguation panel: " + selectedTerm);
		String currentEntryKey = null;
		for (Map.Entry<String, FacetTerm> queryTermEntry : queryTerms.entries()) {
			if (queryTermEntry.getValue().equals(selectedTerm)) {
				currentEntryKey = queryTermEntry.getKey();
			}
			logger.debug("Term in queryTerms: "
					+ queryTermEntry.getValue().getName());
		}
		queryTerms.removeAll(currentEntryKey);
		queryTerms.put(currentEntryKey, selectedTerm);
		doSearch(queryTerms, searchConfiguration.getSortCriterium(),
				searchConfiguration.isReviewsFiltered());
	}

	public void onDrillUp() throws IOException {
		doSearch(searchConfiguration.getQueryTerms(),
				searchConfiguration.getSortCriterium(),
				searchConfiguration.isReviewsFiltered());
	}

	public Object onActionFromQueryPanel() throws IOException {
		return doSearch(searchConfiguration.getQueryTerms(),
				searchConfiguration.getSortCriterium(),
				searchConfiguration.isReviewsFiltered());
	}

	public void onDisableReviewFilter() throws IOException {
		doSearch(searchConfiguration.getQueryTerms(),
				searchConfiguration.getSortCriterium(),
				searchConfiguration.isReviewsFiltered());
	}

	public void onEnableReviewFilter() throws IOException {
		doSearch(searchConfiguration.getQueryTerms(),
				searchConfiguration.getSortCriterium(),
				searchConfiguration.isReviewsFiltered());
	}

	// called by the Index page
	public Object doNewSearch(String query, String termId) throws IOException {
		newSearch = true;
		Multimap<String, FacetTerm> queryTerms = queryDisambiguationService
				.disambiguateQuery(query, termId);
		setQuery(query);

		this.selectedFacetType = Facet.BIO_MED;
		searchConfiguration.setQueryTerms(queryTerms);
		Map<FacetTerm, Facet> queryTermFacetMap = new HashMap<FacetTerm, Facet>();
		for (FacetTerm queryTerm : queryTerms.values())
			queryTermFacetMap.put(queryTerm, queryTerm.getFirstFacet());
		searchConfiguration.setQueryTermFacetMap(queryTermFacetMap);

		if (queryTerms.size() == 0)
			return Index.class;

		Map<Facet, FacetConfiguration> facetConfigurations = searchConfiguration
				.getFacetConfigurations();
		resetConfigurations(facetConfigurations.values());
		drillDownFacetConfigurations(queryTerms.values(), facetConfigurations);
		doSearch(queryTerms, searchConfiguration.getSortCriterium(),
				searchConfiguration.isReviewsFiltered());

		return this;
	}

	public Object doSearch(Multimap<String, FacetTerm> queryTerms,
			SortCriterium sortCriterium, boolean reviewsFiltered)
			throws IOException {
		if (queryTerms.size() == 0)
			return Index.class;

		long time = System.currentTimeMillis();
		// Release the used LabelHierarchy for re-use.
		if (searchResult != null)
			searchResult.getFacetHit().clear();

		Collection<FacetConfiguration> facetConfigurations = getConfigurationsForFacetType(selectedFacetType);

		FacettedSearchResult newResult = searchService
				.search(facetConfigurations, queryTerms, sortCriterium,
						reviewsFiltered);

		if (newResult.getTotalHits() == 0 && searchByTermSelect) {
			noHitTerm = selectedTerm;
			for (String key : queryTerms.keySet()) {
				Collection<FacetTerm> values = queryTerms.get(key);
				if (values.contains(selectedTerm))
					queryTerms.remove(key, selectedTerm);
			}
			if (removedParentTerm[1] != null
					&& !queryTerms.values().contains(removedParentTerm[1])) {
				queryTerms.put((String) removedParentTerm[0],
						(FacetTerm) removedParentTerm[1]);
				removedParentTerm[1] = null;
			}
			return this;
		}
		noHitTerm = null;

		searchResult = newResult;

		// If we found nothing, let's check whether there could have been a
		// spelling error.
		if (searchResult.getTotalHits() == 0) {
			spellingCorrections = createSpellingCorrections(queryTerms);
			logger.info("adding spelling corrections: " + spellingCorrections);
			if (spellingCorrections.size() != 0) {
				spellingCorrectedQueryTerms = createSpellingCorrectedQueryTerms(
						queryTerms, spellingCorrections);
				logger.info("spelling corrected query"
						+ spellingCorrectedQueryTerms);
				searchResult = searchService.search(facetConfigurations,
						spellingCorrectedQueryTerms, sortCriterium,
						reviewsFiltered);
				searchConfiguration
						.setSpellingCorrectedQueryTerms(spellingCorrectedQueryTerms);
				searchConfiguration.setSpellingCorrections(spellingCorrections);
			}
		}
		displayGroup = new LazyDisplayGroup<DocumentHit>(
				searchResult.getTotalHits(), MAX_DOCS_PER_PAGE, MAX_BATCHES,
				searchResult.getDocumentHits());
		// TODO REMOVE this block !!!!!
		// {
		// SemedicoDocument semdoc = new SemedicoDocument();
		// semdoc.setAbstractText("Testabstract");
		// semdoc.setTitle("TestTitle");
		// semdoc.setPubMedId(4711);
		// semdoc.setAuthors(Lists.newArrayList(new Author("Tim", "Graf",
		// "FSU"), new Author("Erik", "Faessler", "Julielab")));
		// semdoc.setPublication(new Publication("TestPub", "Vol1.1",
		// "TestIssue", "1-100", new Date()));
		// semdoc.setType(SemedicoDocument.TYPE_ABSTRACT);
		// DocumentHit testHit = new DocumentHit(semdoc);
		// Collection<DocumentHit> testhits = Lists.newArrayList(testHit);
		// displayGroup = new LazyDisplayGroup<DocumentHit>(1, 10, 2, testhits);
		// }

		currentFacetHit = searchResult.getFacetHit();

		// FacetHit facetHit = currentFacetHits.get(0);
		// ILabelCacheService labelCacheService =
		// facetHit.getLabelCacheService();
		// System.out.println("Hits, latestSearch: " +
		// labelCacheService.getLastSearchTimestamp());
		// for (Label l : labelCacheService.getNodes())
		// if (l.getHits() != null && l.getHits() > 0)
		// System.out.println("Hits: " + l);

		elapsedTime = System.currentTimeMillis() - time;

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
	protected void drillDownFacetConfigurations(Collection<FacetTerm> terms,
			Map<Facet, FacetConfiguration> facetConfigurations) {

		for (FacetTerm searchTerm : terms) {
			if (!searchTerm.hasChildren())
				continue;

			FacetConfiguration configuration = facetConfigurations
					.get(searchTerm.getFirstFacet());

			if (configuration.isHierarchicMode()
					&& configuration.getCurrentPath().size() == 0) {
				configuration.setCurrentPath(termService
						.getPathFromRoot(searchTerm));
			}
		}
	}

	protected Multimap<String, String> createSpellingCorrections(
			Multimap<String, FacetTerm> queryTerms) throws IOException {
		Multimap<String, String> spellingCorrections = HashMultimap.create();
		for (String queryTerm : queryTerms.keySet()) {
			Collection<FacetTerm> mappedTerms = queryTerms.get(queryTerm);

			if (mappedTerms.size() == 1) {
				FacetTerm mappedTerm = mappedTerms.iterator().next();
				if (mappedTerm.getFirstFacet() == FacetService.KEYWORD_FACET) {
					String[] suggestions = spellCheckerService
							.suggestSimilar(queryTerm);
					for (String suggestion : suggestions)
						spellingCorrections.put(queryTerm, suggestion);
				}
			}
		}
		return spellingCorrections;
	}

	public Multimap<String, FacetTerm> createSpellingCorrectedQueryTerms(
			Multimap<String, FacetTerm> queryTerms,
			Multimap<String, String> spellingCorrections) throws IOException {
		Multimap<String, FacetTerm> spellingCorrectedTerms = HashMultimap
				.create(queryTerms);
		for (String queryTerm : spellingCorrections.keySet()) {
			for (String correction : spellingCorrections.get(queryTerm)) {
				Collection<FacetTerm> mappedTerms = queryDisambiguationService
						.mapQueryTerm(correction);
				spellingCorrectedTerms.putAll(queryTerm, mappedTerms);
				spellingCorrectedTerms.putAll(correction, mappedTerms);
			}
		}
		return spellingCorrectedTerms;
	}

	public Object onRemoveTerm() throws IOException {
		setQuery(null);
		return doSearch(searchConfiguration.getQueryTerms(),
				searchConfiguration.getSortCriterium(),
				searchConfiguration.isReviewsFiltered());
	}

	private Collection<FacetConfiguration> getConfigurationsForFacetType(
			int facetType) {
		if (facetType == Facet.BIO_MED)
			return biomedFacetConfigurations;
		else if (facetType == Facet.IMMUNOLOGY)
			return immunologyFacetConfigurations;
		else if (facetType == Facet.BIBLIOGRAPHY)
			return bibliographyFacetConfigurations;
		else
			return filterFacetConfigurations;
	}

	public void onSuccessFromSearch() throws IOException {
		if (getQuery() == null || getQuery().equals(""))
			setQuery(getAutocompletionQuery());
		doNewSearch(getQuery(), getTermId());
	}

	public void onActionFromSearchInputField() throws IOException {
		if (getQuery() == null || getQuery().equals(""))
			setQuery(getAutocompletionQuery());

		doNewSearch(getQuery(), getTermId());
	}

	public void onActionFromPagerLink(int page) throws IOException {
		displayGroup.setCurrentBatchIndex(page);
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		Collection<DocumentHit> documentHits = searchService
				.constructDocumentPage(startPosition);
		displayGroup.setDisplayedObjects(documentHits);
	}

	public void onActionFromPreviousBatchLink() throws IOException {
		displayGroup.displayPreviousBatch();
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		Collection<DocumentHit> documentHits = searchService
				.constructDocumentPage(startPosition);
		displayGroup.setDisplayedObjects(documentHits);
	}

	public void onActionFromNextBatchLink() throws IOException {
		displayGroup.displayNextBatch();
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		Collection<DocumentHit> documentHits = searchService
				.constructDocumentPage(startPosition);
		displayGroup.setDisplayedObjects(documentHits);
	}

	@CleanupRender
	public void cleanUpRender() {
		newSearch = false;
	}

	// public void onActionFromPagerLink(int page) throws IOException {
	// displayGroup.setCurrentBatchIndex(page);
	// int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
	// Collection<DocumentHit> documentHits = searchService
	// .createDocumentHitsForPositions(
	// searchConfiguration.getQueryTerms(),
	// searchResult.getScoreDocs(), startPosition);
	// displayGroup.setDisplayedObjects(documentHits);
	// }
	//
	// public void onActionFromPreviousBatchLink() throws IOException {
	// displayGroup.displayPreviousBatch();
	// int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
	// Collection<DocumentHit> documentHits = searchService
	// .createDocumentHitsForPositions(
	// searchConfiguration.getQueryTerms(),
	// searchResult.getScoreDocs(), startPosition);
	// displayGroup.setDisplayedObjects(documentHits);
	// }
	//
	// public void onActionFromNextBatchLink() throws IOException {
	// displayGroup.displayNextBatch();
	// int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
	// Collection<DocumentHit> documentHits = searchService
	// .createDocumentHitsForPositions(
	// searchConfiguration.getQueryTerms(),
	// searchResult.getScoreDocs(), startPosition);
	// displayGroup.setDisplayedObjects(documentHits);
	// }

	public boolean isCurrentPage() {
		return pagerItem == displayGroup.getCurrentBatchIndex();
	}

	public String getCurrentHitClass() {
		return hitIndex % 2 == 0 ? "evenHit" : "oddHit";
	}

	public String getCurrentArticleTypeClass() {
		if (hitItem.getDocument().getType() == SemedicoDocument.TYPE_ABSTRACT)
			return "hitIconAbstract";
		else if (hitItem.getDocument().getType() == SemedicoDocument.TYPE_TITLE)
			return "hitIconTitle";
		else if (hitItem.getDocument().getType() == SemedicoDocument.TYPE_FULL_TEXT)
			return "hitIconFull";
		else
			return null;
	}

	public int getIndexOfFirstArticle() {
		return displayGroup.getIndexOfFirstDisplayedObject() + 1;
	}

	public boolean isNotLastAuthor() {
		return authorIndex < hitItem.getDocument().getAuthors().size() - 1;
	}

	public LazyDisplayGroup<DocumentHit> getDisplayGroup() {
		return displayGroup;
	}

	public void setDisplayGroup(LazyDisplayGroup<DocumentHit> displayGroup) {
		this.displayGroup = displayGroup;
	}

	public FacettedSearchResult getFacettedSearchResult() {
		return searchResult;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}
}
