package de.julielab.semedico.pages;

import java.io.IOException;
import java.util.Collection;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;

import de.julielab.semedico.components.FacetedSearchLayout;
import de.julielab.semedico.core.Author;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.FacetedSearchResult;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.SemedicoDocument;
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

	@Property
	private DocumentHit hitItem;

	@Property
	private int hitIndex;

	@Property
	private int authorIndex;

	@Property
	private int pagerItem;

	@Property
	private String kwicItem;

	@Property
	private Author authorItem;

	@Property
	@Persist
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

	public Object onActionFromQueryPanel() throws IOException {
		FacetedSearchResult searchResult = searchService.search(searchState
				.getQueryTerms());
		setSearchResult(searchResult);
		return this;
	}

	@Log
	public ResultList onDisambiguateTerm() throws IOException {
		return searchLayout.performSubSearch();
	}

	@Log
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
