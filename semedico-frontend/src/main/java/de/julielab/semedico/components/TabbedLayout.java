package de.julielab.semedico.components;

import de.julielab.semedico.pages.Index;
import de.julielab.semedico.state.SemedicoSessionState;
import de.julielab.semedico.state.tabs.ApplicationTab;
import de.julielab.semedico.state.tabs.ApplicationTab.TabType;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

@Import(stylesheet = { "context:css/tabbedlayout.css" })
public class TabbedLayout {

	@SessionState
	@Property
	private SemedicoSessionState sessionState;

	@Inject
	private ComponentResources componentResources;

	@Inject
	private PageRenderLinkSource linkSource;

	@Inject
	private Request request;

	@Inject
	private Logger log;

	@Property
	@Persist
	private ApplicationTab tabLoopItem;

	public void setupRender() {
		// For the current tab, set the name of the just activated (loaded) page. This way we can leave a tab, come back
		// later and still now at which page we have been.
		Component page = componentResources.getPage();
		String pageName = page.getComponentResources().getCompleteId();
		sessionState.getActiveTab().setActivePageName(pageName);
	}

	Link onTabSelect(int tabIndex) {
		sessionState.setActiveTab(tabIndex);
		Link tabLink = getActiveTabLink();
		return tabLink;
	}

	protected Link getActiveTabLink() {
		ApplicationTab activeTab = sessionState.getActiveTab();
		Link tabLink = linkSource.createPageRenderLink(activeTab.getActivePageName());
		tabLink.addParameter(SemedicoSessionState.PARAM_ACTIVE_TAB, activeTab.getTabIndexAsString());
		return tabLink;
	}

	void onTabMove(int from, int to) {
		sessionState.moveTab(from, to);
	}

	Object onTabRemove(int tabIndex) {
		ApplicationTab newActiveTab = sessionState.removeTab(tabIndex);
		if (null != newActiveTab)
			return getActiveTabLink();
		return Index.class.getSimpleName();
	}

	Link onAddTab(TabType tabType) {
		sessionState.addTab(tabType);
		return getActiveTabLink();
	}

	public ApplicationTab getActiveTab() {
		return sessionState.getActiveTab();
	}

	public String getTabStateClass() {
		// return activeTabIndex == tabLoopItem.getTabIndex() ? "activeTab" : "inactiveTab";
		return sessionState.getActiveTabIndex() == tabLoopItem.getTabIndex() ? "activeTab" : "inactiveTab";
	}
}