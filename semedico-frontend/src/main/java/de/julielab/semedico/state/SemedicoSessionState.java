package de.julielab.semedico.state;

import de.julielab.semedico.core.entities.state.SearchState;
import de.julielab.semedico.core.entities.state.UserInterfaceState;
import de.julielab.semedico.core.services.interfaces.IDocumentRetrievalSearchStateCreator;
import de.julielab.semedico.core.services.interfaces.IDocumentRetrievalUserInterfaceCreator;
import de.julielab.semedico.state.tabs.ApplicationTab;
import de.julielab.semedico.state.tabs.ApplicationTab.TabType;
import de.julielab.semedico.state.tabs.DocumentRetrievalTab;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

import java.util.*;

public class SemedicoSessionState {

	public static final String PARAM_ACTIVE_TAB = "tab";

	private List<ApplicationTab> tabs;
	private int activeTabIndex;
	private boolean tutorialMode;
	private IDocumentRetrievalSearchStateCreator docRetrievalSSCreator;
	private IDocumentRetrievalUserInterfaceCreator docRetrievalUiStateCreator;
	private Logger log;

	public SemedicoSessionState(Logger log, IDocumentRetrievalSearchStateCreator docRetrievalSSCreator,
			IDocumentRetrievalUserInterfaceCreator docRetrievalUiStateCreator) {
		this.log = log;
		this.docRetrievalSSCreator = docRetrievalSSCreator;
		this.docRetrievalUiStateCreator = docRetrievalUiStateCreator;
		this.tabs = new ArrayList<>();
		this.persistentAttributes = new HashMap<>();
	}

	/**
	 * <p>
	 * Returns the current search state. Returns <tt>null</tt>, if the current
	 * page is no search page. The search state returned by this method
	 * corresponds to the <tt>UserInterfaceState</tt> returned by
	 * {@link #getUiState()}.
	 * <p>
	 * <p>
	 * A <tt>SearchState</tt> and a <tt>UserInterfaceState</tt> completely
	 * determine the state of a single search.
	 * </p>
	 * <p>
	 * If no tabs are available at the time of the call to this method, a
	 * document retrieval tab will be created. This should only happen upon the
	 * very first search for a session.
	 * </p>
	 * 
	 * @see #getUiState()
	 * @return
	 */
	public SearchState getDocumentRetrievalSearchState() {
		ApplicationTab activeTab = getActiveTab();
		if (null == activeTab)
			activeTab = addTab(TabType.DOC_RETRIEVAL);
		switch (activeTab.getTabType()) {
		case DOC_RETRIEVAL:
			DocumentRetrievalTab tab = (DocumentRetrievalTab) activeTab;
			return tab.getSearchState();
		default:
			return null;
		}
	}

	/**
	 * <p>
	 * Returns the current user interface state for a <em>search page</em>.
	 * Returns <tt>null</tt>, if the current page is not a search page. The
	 * returned <tt>UserInterfaceState</tt> corresponds to the
	 * <tt>SearchState</tt> returned by {@link #getSearchState()}.
	 * </p>
	 * <p>
	 * A <tt>SearchState</tt> and a <tt>UserInterfaceState</tt> completely
	 * determine the state of a single search.
	 * </p>
	 * <p>
	 * If no tabs are available at the time of the call to this method, a
	 * document retrieval tab will be created. This should only happen upon the
	 * very first search for a session.
	 * </p>
	 * 
	 * @see #getSearchState()
	 * 
	 * @return
	 */
	public UserInterfaceState getDocumentRetrievalUiState() {
		ApplicationTab activeTab = getActiveTab();
		if (null == activeTab)
			activeTab = addTab(TabType.DOC_RETRIEVAL);
		switch (activeTab.getTabType()) {
		case DOC_RETRIEVAL:
			DocumentRetrievalTab tab = (DocumentRetrievalTab) activeTab;
			return tab.getUiState();
		default:
			return null;
		}
	}

	private UserInterfaceState createDocumentRetrievalUiState() {
		return docRetrievalUiStateCreator.create();
	}

	private SearchState createDocumentRetrievalSearchState() {
		return docRetrievalSSCreator.create();
	}

	public ApplicationTab getActiveTab() {
		if (tabs.isEmpty())
			return null;
		return tabs.get(activeTabIndex);
	}

	public ApplicationTab setActiveTab(int tabIndex) {
		activeTabIndex = tabIndex;
		return getActiveTab();
	}

	public int getActiveTabIndex() {
		return activeTabIndex;
	}

	public ApplicationTab addTab(TabType tabType) {
		ApplicationTab tab;
		int newTabIndex = tabs.size();
		switch (tabType) {
		case DOC_RETRIEVAL:
			tab = new DocumentRetrievalTab("Tab " + (newTabIndex + 1), newTabIndex,
					createDocumentRetrievalSearchState(), createDocumentRetrievalUiState());
			break;
		default:
			throw new IllegalArgumentException("Tab type \"" + tabType + "\" is not supported.");
		}
		tabs.add(tab);
		activeTabIndex = newTabIndex;
		return tab;
	}

	/**
	 * Removes the tab at index <tt>tabIndex</tt>. If this tab was the active
	 * tab, the previous tab becomes the active tab or the the first tab of the
	 * remaining tabs, if the removed tab was the first one.
	 * 
	 * @param tabIndex
	 * @return
	 */
	public ApplicationTab removeTab(int tabIndex) {
		tabs.remove(tabIndex);
		if (tabs.isEmpty())
			return null;
		if (tabIndex == activeTabIndex) {
			if (tabIndex - 1 >= 0)
				activeTabIndex = tabIndex - 1;
			else
				activeTabIndex = 0;
		}
		for (int i = tabIndex; i < tabs.size(); ++i)
			tabs.get(i).setTabIndex(i);
		return tabs.get(activeTabIndex);
	}

	/**
	 * Moves the tab at index <tt>from</tt> to index <tt>to</tt>. If the tab at
	 * index <tt>from</tt> is the active tab, the active tab is set to
	 * <tt>to</tt>.
	 * 
	 * @param from
	 * @param to
	 */
	public void moveTab(int from, int to) {
		ApplicationTab movedTab = tabs.get(from);
		// moved from left to right
		if (from < to)
			for (int i = from; i < to; ++i)
				tabs.set(i, tabs.get(i + 1));
		// moved from right to left
		else if (from > to)
			for (int i = from; i > to; --i)
				tabs.set(i, tabs.get(i - 1));
		tabs.set(to, movedTab);
		for (int i = Math.min(from, to); i < Math.max(from, to); ++i)
			tabs.get(i).setTabIndex(i);
		if (from == activeTabIndex)
			activeTabIndex = to;
	}

	/**
	 * Attempts to find a session state object (SSO) assignment compatible to
	 * <tt>cls</tt>. If there are multiple valid candidates, it is not defined
	 * which one will be returned.
	 * 
	 * @param cls
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T findSessionStateObject(Class<T> cls) {
		ApplicationTab activeTab = getActiveTab();
		switch (activeTab.getTabType()) {
		case DOC_RETRIEVAL:
			SearchState documentRetrivalSearchState = getDocumentRetrievalSearchState();
			if (cls.isAssignableFrom(documentRetrivalSearchState.getClass()))
				return (T) documentRetrivalSearchState;
			UserInterfaceState documentRetrievalUiState = getDocumentRetrievalUiState();
			if (cls.isAssignableFrom(documentRetrievalUiState.getClass()))
				return (T) documentRetrievalUiState;
			break;
		default:
			return null;

		}
		return null;
	}

	public boolean isTutorialMode() {
		return tutorialMode;
	}

	public void setTutorialMode(boolean tutorialMode) {
		this.tutorialMode = tutorialMode;
	}

	/**
	 * Returns all tabs of this session.
	 * 
	 * @return All application tabs.
	 */
	public List<ApplicationTab> getTabs() {
		return tabs;
	}

	/**
	 * Returns the tabs of type <tt>tabType</tt> present in this session.
	 * 
	 * @param tabType
	 * @return
	 */
	public List<ApplicationTab> getTabs(TabType tabType) {
		List<ApplicationTab> typeTabs = new ArrayList<>();
		for (ApplicationTab tab : tabs) {
			if (tab.getTabType() == tabType) {
				typeTabs.add(tab);
			}
		}
		return typeTabs;
	}

	private Map<String, Object> persistentAttributes;

	public Collection<String> getAttributeNames(String fullPrefix) {
		List<String> matchedKeys = new ArrayList<>();
		for (String key : persistentAttributes.keySet()) {
			if (key.startsWith(fullPrefix)) {
				matchedKeys.add(key);
			}
		}
		return matchedKeys;
	}

	public Object getAttribute(String name) {
		return persistentAttributes.get(name);
	}

	public void setAttribute(String name, Object object) {
		persistentAttributes.put(name, object);
	}

	public void removeAttribute(String name) {
		persistentAttributes.remove(name);
	}

	public ApplicationTab getTab(int index) {
		return tabs.get(index);
	}

	public ApplicationTab setActiveTabFromRequest(Request request) {
		// We want to keep the active tab index around as a query parameter. We
		// do this as to enable the browser back in
		// a useful way within the application. If we would NOT have the query
		// parameter and we would change from tab1
		// to tab2, for example, and then hit the browser 'back' button, we
		// would still be at tab2 because the session
		// state was changed to tab2 on the selection of tab2. With a query
		// parameter, the back button will return to
		// the state where tab1 was set to be active, this is read at this place
		// here as set into the session.

		if (request.isXHR()) {
			log.debug(
					"Request was XHR enabled, most probably an AJAX call, it is not tried to read the active tab from it.");
			return getActiveTab();
		}

		// Initialize the active tab with -1 which equals "not set".
		int activeTabIndex = -1;
		// Get the active tab index from the request, if it is set there.
		String activeTabParam = request.getParameter(PARAM_ACTIVE_TAB);
		log.debug("Trying to receive active tab index from request.");
		if (null != activeTabParam) {
			try {
				activeTabIndex = Integer.parseInt(activeTabParam);
				log.debug("Read active tab index {} from query parameter.", activeTabIndex);
			} catch (NumberFormatException e) {
				log.error("Active tab query parameter named {} was not an integer. Value was: {}", PARAM_ACTIVE_TAB,
						activeTabParam);
			}
		} else {
			log.debug("Request parameter {} is not set.", PARAM_ACTIVE_TAB);
		}
		if (activeTabIndex == -1) {
			if (getTabs().size() == 1) {
				log.debug("There is only one tab present in the session, setting active index to 0.");
				activeTabIndex = 0;
			} else {
				// This is the fallback the default behavior: Just use the the
				// value that is stored in the session.
				activeTabIndex = getActiveTabIndex();
				log.debug("Did not find tab index query parameter. Fallback to session state: Active tab index is {}.",
						activeTabIndex);
			}
		}
		log.debug("Active tab set to {}.", activeTabIndex);
		// Up to now we will have determined SOME active tab. Set it into the
		// session to reflected in the rendering.
		return setActiveTab(activeTabIndex);
	}

}
