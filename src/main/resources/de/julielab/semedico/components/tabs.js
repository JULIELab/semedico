/* 
 * The class Tabs provides dhtml handlers
 * for the selection of the facet tabs.
 * It implements both the prototype and
 * dojo javascript/DHTML frameworks:
 * 
 * 	- http://prototypejs.org
 *  - http://dojotoolkit.org
 */
function Tabs(selectedTab, url) {

	this.url = url;

	// Determine the number of tabs which have been rendered by Tapestry. Each
	// Tab has exactly one link intended for its selection, so identify the tabs
	// by their links.
	var nrOfTabs = document.getElementById("tabHeader").getElementsByTagName(
			"a").length;
	
	// For each tab we build one JSON structure to store the information
	// needed to be able to dynamically select the tabs and render them
	// appropriately. This information is added in the function
	// refreshListeners() below.
	this.tabs = {};
	for ( var nr = 0; nr < nrOfTabs; nr++) {
		this.tabs[nr] = {};
	}

	this.options = {};
	this.options.asynchronous = true;
	this.options.method = "get";

	this.options.encoding = "UTF-8";
	this.options.onComplete = this.onComplete.bind(this);

	this.refreshListeners();

	for ( var ordinal in this.tabs) {
		var className = "tabInActive";
		if (ordinal == selectedTab) {
			className = "tabActive";
			if (ordinal == 0)
				className = "firstTabActive";
		}
		this.tabs[ordinal].header.className = className;

	}
}
/*
 * The onComplete event handler is called whenever a page is completely rendered
 * by the browser
 * 
 * Updates the facet box with the JSON code supplied as argument (see
 * http://www.json.org/)
 */
Tabs.prototype.onComplete = function(response) {
	// alert(response.responseText);

	var content = response.responseText.evalJSON();
	if (content == "") {
	} else {
		this.facetBar.replace(content.content);
		// alert(content.inits[0].evalScript);
		var script = "<script>" + content.inits[0].evalScript.join('')
				+ "</script>";
		// alert(script);
		script.evalScripts();
	}

	// this.refreshListeners();
};
/*
 * Sets (refreshes?) the onClick event handlers for the tabs. Makes them super
 * duper clickable
 */
Tabs.prototype.refreshListeners = function() {
	for ( var ordinal in this.tabs) {
		// e.g. ordinal="0" -> "tab_0", which is the id of the first tab in Tabs.tml.
		this.tabs[ordinal].header = $("tab_" + ordinal);
		// Get the link of the corresponding tab, which is only '#' in Tabs.tml.
		this.tabs[ordinal].link = this.tabs[ordinal].header
				.getElementsByTagName("a")[0];
		// Bind the function activateTab (found below) to the current tab, e.g. "0".
	    // In this function, a new request is send to the server informing about which tab
	    // has been selected in case of an onclick-event.
		this.tabs[ordinal].link.onclick = this.activateTab.bind(this, ordinal);
	}

	this.facetBar = $("facetBar");

};
/*
 * This function sends the actual request to change the tab.
 * The selected tab will be set to the selected one, determined
 * by "selected" which is just the index of the selected tab (0, 1, 2, ...).
 * This index has been passed by the binding in "refreshListeners" above.
 */
Tabs.prototype.activateTab = function(selected) {
	this.options.parameters = "selectedTab=" + selected;
	// alert(this.options.parameters);
	new Ajax.Request(this.url, this.options);
};
