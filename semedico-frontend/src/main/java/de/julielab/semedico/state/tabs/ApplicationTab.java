package de.julielab.semedico.state.tabs;

/**
 * A <em>Application Tab</em> is a page dedicated to a specific task. Tasks include document retrieval, text mining or
 * simply a configuration page.
 * 
 * @author faessler
 * 
 */
public abstract class ApplicationTab {

	public enum TabType {
		DOC_RETRIEVAL, BTERM_RETRIEVAL
	}

	protected String name;
	private String activePageName;
	protected final TabType tabType;
	private int tabIndex;

	public ApplicationTab(String name, int tabIndex, TabType tabType) {
		this.name = name;
		this.tabIndex = tabIndex;
		this.tabType = tabType;
		this.activePageName = getStartPageName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getActivePageName() {
		return activePageName;
	}

	public void setActivePageName(String activePageName) {
		this.activePageName = activePageName;
	}

	public abstract String getStartPageName();

	public TabType getTabType() {
		return tabType;
	}

	public int getTabIndex() {
		return tabIndex;
	}

	public void setTabIndex(int tabIndex) {
		this.tabIndex = tabIndex;
	}

	public String getTabIndexAsString() {
		return String.valueOf(tabIndex);
	}
	
}
