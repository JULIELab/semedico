/* 
 * The class Tabs provides dhtml handlers
 * for the selection of the facet tabs.
 * It implements both the prototype and
 * dojo javascript/DHTML frameworks:
 * 
 * 	- http://prototypejs.org
 *  - http://dojotoolkit.org
 */
function Tabs(selectedTab, url){
	
	this.url = url;
	this.selectedTab = selectedTab;
	this.tabs = {
		first: {},
		second: {},
		third: {},
		fourth: {},
		fifth: {}
	};
	this.options = {};
	this.options.asynchronous = true;
    this.options.method="get";
    
    this.options.encoding = "UTF-8";
	this.options.onComplete = this.onComplete.bind(this);
	
	this.refreshListeners();
}
/* The onComplete event handler is called whenever
 * a page is completely rendered by the browser
 * 
 * Updates the facet box with the JSON code supplied as argument
 * (see http://www.json.org/)
 */
Tabs.prototype.onComplete = function(response){
	//alert(response.responseText);
	
	var content = response.responseText.evalJSON();
	if( content == "" ){
	}
	else{
		this.facetBar.replace(content.content);
		//alert(content.inits[0].evalScript);
		var script = "<script>" + content.inits[0].evalScript.join('') + "</script>";
		//alert(script);
		script.evalScripts();
	}
	
	this.refreshListeners();
}
/* Sets (refreshes?) the onClick event handlers for the tabs.
 * Makes them super duper clickable
 */
Tabs.prototype.refreshListeners = function(){
	for ( var ordinal in this.tabs ) {
	    this.tabs[ordinal].header = $(ordinal+"TabHeader");
	    this.tabs[ordinal].link = this.tabs[ordinal].header.getElementsByTagName("a")[0];
	    this.tabs[ordinal].link.onclick = this.activateTab.bind(this, ordinal);
	}
	//alert("aha");

	/*
    this.firstTabHeader = $("firstTabHeader");
	this.firstTabLink = this.firstTabHeader.getElementsByTagName("a")[0];
	this.firstTabLink.onclick = this.activateFirstTab.bind(this);
	
    this.secondTabHeader = $("secondTabHeader");
    this.secondTabLink = this.secondTabHeader.getElementsByTagName("a")[0];
	this.secondTabLink.onclick = this.activateSecondTab.bind(this);            	

    this.thirdTabHeader = $("thirdTabHeader");
    this.thirdTabLink = this.thirdTabHeader.getElementsByTagName("a")[0];
	this.thirdTabLink.onclick = this.activateThirdTab.bind(this);
	
    this.fourthTabHeader = $("fourthTabHeader");
    this.fourthTabLink = this.fourthTabHeader.getElementsByTagName("a")[0];
	this.fourthTabLink.onclick = this.activateFourthTab.bind(this);
	*/
	
	this.facetBar = $("facetBar");
}
/* General method called by the event handler methods below
 */
Tabs.prototype.activateTab = function(selected) {
	for(var ordinal in this.tabs) {
		this.tabs[ordinal].header.className = ordinal + (ordinal == selected ? "TabActive" : "TabInActive");
	}
	/*
	this.firstTabHeader.className = "firstTabInActive";
	this.secondTabHeader.className = "secondTabInActive";
	this.thirdTabHeader.className = "thirdTabInActive";
	this.fourthTabHeader.className = "fourthTabInActive";	
	switch(active) {
		case "first": this.firstTabHeader.className = "firstTabActive"; break;
		case "second": this.secondTabHeader.className = "secondTabActive"; break;
		case "third": this.thirdTabHeader.className = "thirdTabActive"; break;
		case "fourth": this.fourthTabHeader.className = "fourthTabActive"; break;
	}
	*/
	this.selectedTab = selected + "Tab";
	this.options.parameters = "selectedTab="+this.selectedTab;
	//alert(this.options.parameters);
	new Ajax.Request(this.url, this.options);
}

