package de.julielab.semedico.pages;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.elasticsearch.index.query.QueryBuilder;
import org.slf4j.Logger;

import de.julielab.semedico.components.FacetedSearchLayout;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.util.LazyDisplayGroup;
import de.julielab.semedico.services.IStatefulSearchService;
import de.julielab.semedico.state.SemedicoSessionState;
import de.julielab.semedico.state.tabs.ApplicationTab;

@Import(stylesheet =
	{
		"context:css/semedico-tutorial.css"
	},
	library =
	{
		"context:js/tutorial-resultlist.js"
	})

public class ResultList {
	@Persist("tab")
	@Property
	private ParseTree query;

	@Persist("tab")
	@Property
	private QueryBuilder esQuery;

	@Property
	@Persist("tab")
	private LazyDisplayGroup<HighlightedSemedicoDocument> displayGroup;

	@Property
	@Persist("tab")
	// Used for display only.
	private long elapsedTime;

	@InjectPage
	private Index index;

	@InjectComponent("FacetedSearchLayout")
	private FacetedSearchLayout searchLayout;

	@SessionState(create = false)
	@Property
	private SemedicoSessionState sessionState;

	@Property
	private SearchState searchState;

	@Inject
	private IStatefulSearchService searchService;

	@Inject
	private ITermService termService;

	@Persist
	private int tutorialStep;

	@Inject
	private Request request;

	@Environmental
	private JavaScriptSupport javascriptSupport;

	@Inject
	private ComponentResources componentResources;
	
	@Inject
	private Logger log;

	/**
	 * <p>
	 * Event handler which is executed before beginning page rendering.
	 * </p>
	 * <p>
	 * The main page will check whether there is a search whose search results could be displayed. If not, the user is
	 * redirected to the Index page.
	 * </p>
	 * 
	 * @return The Index page if there is no search to display. Otherwise, null will be returned to signal the page
	 *         rendering.
	 * @see http://tapestry.apache.org/page-navigation.html
	 */

	public Object onActivate() {
		
		// TODO solve with the already introduced RequestFilter (has to be readily implemented, however)
		if (sessionState != null) {
			sessionState.setActiveTabFromRequest(request);
			searchState = sessionState.getDocumentRetrievalSearchState();
		}
		
		if (searchState == null || searchState.getSemedicoQuery() == null || searchState.getSemedicoQuery().isEmpty()) {
			log.debug("No document retrieval search state or no query, return to index.");
			return index;
		}
		return null;
	}

	public ResultList onDisambiguateTerm() {
		return searchLayout.performSubSearch();
	}

	public ResultList onRemoveTerm() {
		return searchLayout.performSubSearch();
	}

	public ResultList onDrillUp() {
		return searchLayout.performSubSearch();
	}
	
	public JSONObject onIncrementTutorialStep() {
		tutorialStep++;
		JSONObject stepObject = new JSONObject();
		stepObject.put("tutorialStep", tutorialStep);
		return stepObject;
	}

	public void setupRender() {
		// Tutorial
		String isTutorialMode = request.getParameter("tutorialMode");
		
		if (isTutorialMode != null && isTutorialMode.equals("false")) {
			sessionState.setTutorialMode(Boolean.parseBoolean(isTutorialMode));
			tutorialStep = 0;
		}
		
		ApplicationTab activeTab = sessionState.getActiveTab();

		String tabName = "Query";
		activeTab.setName("(" + tabName + ")");
	}

	public void afterRender() {
		if (sessionState.isTutorialMode()) {
			Link eventLink = componentResources.createEventLink("incrementTutorialStep");
			JSONArray parameters = new JSONArray();
			parameters.put(tutorialStep);
			parameters.put(eventLink.toURI());
			javascriptSupport.addInitializerCall(InitializationPriority.LATE, "startTutorial", parameters);
		}
	}

	/**
	 * @param result
	 */
	public void setSearchResult(
			LegacySemedicoSearchResult searchResult) {
		elapsedTime = searchResult.getElapsedTime();
		displayGroup = searchResult.documentHits;
		query = searchResult.query;
		esQuery = searchResult.esQuery;
	}
}
