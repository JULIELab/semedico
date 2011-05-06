/*
 * The class FacetBox controls all the dynamic elements
 * of the facets. It implements both the prototype and
 * dojo javascript/DHTML frameworks:
 * 
 * 	- http://prototypejs.org
 *  - http://dojotoolkit.org
 */

function FacetBox(name, url, expanded, collapsed, hierarchicMode){
	// parameters
	this.name = name;
	this.url = url;
	this.expanded = expanded;
	this.collapsed = collapsed;	
	this.hidden = false;
    this.hierarchicMode = hierarchicMode;
	this.options = {};
	this.options.asynchronous = true;
	this.options.onComplete = this.onToggleExpansion.bind(this);
    this.options.method="get";    
    //this.options.onFailure = tapestry.error;
    //this.options.onException = tapestry.error;
    this.options.encoding = "UTF-8";

    this.clear = false;
    this.imageType = "png";	

	/* facet elements
	 */
	this.boxElement = $(name);	
	this.listPanel = $(name+"Panel");
	this.innerBox = $(name+"Box");
	this.batchNumber = 1;	
	this.modeSwitchButton = null;
	this.collapseButton = null;
	this.closeButton = null;
	this.prevButton = null;
	this.nextButton = null;
	this.moreLink = null;
	this.pathLinks = null; // array!
	this.topLink;
	
	/* event listeners for facet elements
	 */
	this.modeSwitchButtonListener = null;
	this.prevButtonListener = null;
	this.nextButtonListener = null;
	this.collapseButtonListener = null;
	this.closeButtonListener = null;
	this.moreLinkListener = null;
	this.pathLinksListeners = null; // array!
	this.topLinkListener = null;
 	
    this.refreshListeners();    
}

/* The method refreshListeners is called when the content
 * of a facet has changed. It updates the event listeners accordingly.
 */
FacetBox.prototype.refreshListeners = function(){
	
	this.nextButton = $(this.name + "PagerNextLink");	
	
	if( this.nextButton && this.nextButtonListener )
		Event.stopObserving(this.nextButton, "click", this.nextButtonListener);
			
	if( this.nextButton && this.nextButton.id == this.name + "PagerNextLink"){
		this.nextButtonListener = this.showNextBatch.bindAsEventListener(this);
		Event.observe(this.nextButton, "click", this.nextButtonListener);
	}
	
	this.prevButton = $(this.name + "PagerPreviousLink");
	
	if( this.prevButton && this.prevButtonListener )
		Event.stopObserving(this.prevButton, "click", this.prevButtonListener);
			
	if( this.prevButton && this.prevButton.id == this.name + "PagerPreviousLink"){
		this.prevButtonListener = this.showPreviousBatch.bindAsEventListener(this);
		Event.observe(this.prevButton, "click", this.prevButtonListener);
	}
	
	if( this.moreLinkListener )
		Event.stopObserving(this.moreLink, "click", this.moreLinkListener);
	
	this.moreLink = $(this.name+"Link");
	if( this.moreLink && this.moreLink.id == this.name + "Link"){
		this.moreLinkListener = this.toggleExpansion.bindAsEventListener(this);
		Event.observe(this.moreLink, "click", this.moreLinkListener);
	}

	if( this.expanded ){
		this.filterField = this.boxElement.getElementsByTagName("input")[0];
		if( this.filterField ){
			Event.observe(this.filterField, "keypress", this.onKeyPress.bindAsEventListener(this));
			if( this.filterField.value == "type to filter" )
				Event.observe(this.filterField, "focus", this.filterField.clear.bindAsEventListener(this.filterField));
			else						
				this.filterField.focus();
		}		
	}												
	if( this.modeSwitchButtonListener )
		Event.stopObserving(this.modeSwitchButton, "click", this.modeSwitchButtonListener);
		
	this.modeSwitchButton = $(this.name+"ModeSwitchLink");
	if( this.modeSwitchButton ){
		this.modeSwitchButtonListener = this.toggleHierarchicMode.bindAsEventListener(this);	
		Event.observe(this.modeSwitchButton, "click", this.modeSwitchButtonListener);
	}
	if( this.collapseButtonListener )
		Event.stopObserving(this.collapseButton, "click", this.collapseButtonListener);

	this.collapseButton = $(this.name+"CollapseLink");
	if( this.collapseButton ) {
		this.collapseButtonListener = this.toggleCollapse.bindAsEventListener(this);
		Event.observe(this.collapseButton, "click", this.collapseButtonListener);
	}

	if( this.closeButtonListener )
		Event.stopObserving(this.closeButton, "click", this.closeButtonListener);
		
	//this.closeButton = $(this.name+"CloseLink");
	//this.closeButtonListener = this.hide.bindAsEventListener(this);
	//Event.observe(this.closeButton, "click", this.closeButtonListener);	
	
	if( this.topLinkListener && this.topLink )
		Event.stopObserving(this.topLink, "click", this.topLinkListener);
	
	this.topLink = $(this.name+"TopLink");
	if( this.topLink ){
		this.topLinkListener = this.drillToTop.bindAsEventListener(this);
		Event.observe(this.topLink, "click", this.topLinkListener);
	}
	
	if( this.pathLinksListeners ){
		for( var i = 0; i < this.pathLinksListeners.length; i++ ){
			Event.stopObserving(this.pathLinks[i], "click", this.pathLinksListeners[i]);
		}
	}
		
	this.pathLinks = new Array();
	this.pathLinksListeners = new Array();
		/*
	var index = 0;
	while( true ){
		var link = $(this.name + "pathLink"+index);
		if( link ){
			this.pathLinks.push(link);
			this.pathLinksListeners[index] = this.drillUp.bindAsEventListener(this);
			Event.observe(link, "click", this.pathLinksListeners[index]);
		}
		else{
			break;
		}
		index++;
	}
	*/
}

/* Shows a loading animation (gif)
 */
FacetBox.prototype.indicateProcessing = function(){
	this.collapseButton.style.backgroundImage = "url(\"images/loader.gif\")";
}

/* Updates the facet box with the JSON code supplied as argument content
 * see http://www.json.org/
 */
FacetBox.prototype.updateBox = function(content){
	alert(content);
	if( content == "" ){
		this.displayErrorDialog();
		this.collapseButton.style.backgroundImage = "url(\"images/ico_open.png\")";
	}
	else{	
		this.innerBox.replace(content.evalJSON().content);
		this.innerBox = $(this.name+'Box');
		this.listPanel = $(this.name+'Panel');	
		this.refreshListeners();
	}
}
/* Catches the KEY_ESC event (key press escape) to clear the filter
 */
FacetBox.prototype.onKeyPress = function(event){
	switch(event.keyCode) {
       case Event.KEY_ESC:
         this.clear=true;
    }
	
    if(this.observer) clearTimeout(this.observer);
    	this.observer = setTimeout(this.onObserverEvent.bind(this), 0.2 * 1000);
    
}
/* Clears the term filter
 */
FacetBox.prototype.clearFilter = function(){
	this.filterField.value = "";
    this.options.parameters = "clearFilter=true";
    	       	
    this.options.onComplete =  this.onListFiltered.bind(this);
    new Ajax.Request(this.url, this.options);	
    
    this.clear = false;		
    this.indicateProcessing();
}

FacetBox.prototype.onObserverEvent = function(event){
	if( this.clear ){
		this.clearFilter();
		return;
	}
	
    this.options.parameters = "filterToken=" +
    		encodeURIComponent(this.filterField.value);
    	       	
    this.options.onComplete =  this.onListFiltered.bind(this);
    this.indicateProcessing();
    new Ajax.Request(this.url, this.options);   
     			
}
/* Sets the event handler for the term filter 
 */
FacetBox.prototype.onListFiltered = function(request){
	this.updateBox(request.responseText);	
	this.filterField.focus();	
	if (/MSIE (\d+\.\d+);/.test(navigator.userAgent)){
            var textRange = this.filterField.createTextRange();
           	var length = this.filterField.value.length;
            textRange.move("character", length), textRange.moveEnd("character", length), textRange.select();
	}
}

/* Sets the event handler for the method below
 */
FacetBox.prototype.onDrillUp = function(request){
	this.updateBox(request.responseText);
}
/* Drills up the facet links
 */
FacetBox.prototype.drillUp = function(event){
	if( !event )
		event = window.event;
		
	Event.stop(event);
	var link = event.target;

	var index = -1;
	for( var i = 0; i < this.pathLinks.length; i++ ){
		if( this.pathLinks[i] == link )
		index = i;
	}

   	this.options.parameters = "drillUp="+index;       	
   	this.options.onComplete =  this.onDrillUp.bind(this);
	this.indicateProcessing();
   	new Ajax.Request(this.url, this.options);			
}
/* Sets the event handler for the method below
 */
FacetBox.prototype.onDrillToTop = function(response){	
	this.updateBox(response.responseText);
}
/* Drills up the facet links to the topmost level
 */
FacetBox.prototype.drillToTop = function(event){
	if( !event )
		event = window.event;
		
	Event.stop(event);

   	this.options.parameters =  "drillToTop=true";       	
   	this.options.onComplete =  this.onDrillToTop.bind(this);
	this.indicateProcessing();
   	new Ajax.Request(this.url, this.options);			
}

/* Sets the event handler for the method below
 */
FacetBox.prototype.onToggleHierarchicMode = function(request){
	if( this.hierarchicMode ){
		this.hierarchicMode = false;
	}
	else {
		this.hierarchicMode = true;
	}
	this.updateBox(request.responseText);
}
/* Toggles hierarchic view
 */
FacetBox.prototype.toggleHierarchicMode = function(event){
	if( !event )
		event = window.event;
	Event.stop(event);

	this.options.parameters = "hierarchicMode="+(this.hierarchicMode?"false":"true");       	
	this.options.onComplete = this.onToggleHierarchicMode.bind(this);
	new Ajax.Request(this.url, this.options);		

	this.indicateProcessing();	
}

FacetBox.prototype.onTogglePager = function(request){
	this.updateBox(request.responseText);
}

FacetBox.prototype.showPreviousBatch = function(event){
		if( !event )
			event = window.event;

		this.batchNumber--;
		Event.stop(event);
    	this.options.parameters = "pager=prev&batch=" + this.batchNumber;
    	this.options.onComplete =  this.onTogglePager.bind(this);      	
    	new Ajax.Request(this.url, this.options);
    	this.indicateProcessing();		
}
FacetBox.prototype.showNextBatch = function(event){
		if( !event )
			event = window.event;
		
		Event.stop(event);
		this.batchNumber++; 
    	this.options.parameters = "pager=next&batch=" + this.batchNumber;
    	this.options.onComplete =  this.onTogglePager.bind(this);      	
    	new Ajax.Request(this.url, this.options);
    	this.indicateProcessing();		
}
/* Sets the event handler for the method below
 */
FacetBox.prototype.onToggleExpansion = function(request){
	
	if( this.expanded ){
		this.expanded = false;
		this.collapsed = false;		
	}
	else{
		this.expanded = true;
		this.collapsed = false;
	}
	
	this.updateBox(request.responseText);	
}
/* Expands the facet box
 */
FacetBox.prototype.toggleExpansion= function(event){
	if( !event )
		event = window.event;
		
	Event.stop(event);
	
	if( this.expanded ){
    	this.options.parameters = "expandList=false";       	
    	this.options.onComplete =  this.onToggleExpansion.bind(this);
    	new Ajax.Request(this.url, this.options);		
	}
	else{
    	this.options.parameters = "expandList=true";
    	this.options.onComplete =  this.onToggleExpansion.bind(this);    	       	
    	new Ajax.Request(this.url, this.options);		
	}
	this.indicateProcessing();
}
/* Sets the event handler for the method below
 */
FacetBox.prototype.onCollapse= function(request){

	if( this.collapsed ) {			
		this.collapsed = false;
		this.expanded = false;
 	}
	else{
		this.collapsed = true;
		this.expanded = false;
	}
	this.updateBox(request.responseText);		
}
/* Collapses the facet box
 */
FacetBox.prototype.toggleCollapse=  function(event){
		if( !event )
		event = window.event;
		
		Event.stop(event);
		if( this.collapsed ){
    		this.options.parameters = "collapse=false";		
		}
		else{
			this.options.parameters = "collapse=true";
		}

    	this.options.onComplete =  this.onCollapse.bind(this);      	
    	new Ajax.Request(this.url, this.options);
    	this.indicateProcessing();		
}
/* Sets the onHide event handler for the method below
 */
FacetBox.prototype.onHide= function(request){
	this.boxElement.style.display = "none";
}
/* Sets the onShow event handler for the method below
 */
FacetBox.prototype.onShow= function(request){
	this.boxElement.style.display = "block";
}
/* Hides the facet box
 */
FacetBox.prototype.hide= function(){
	this.options.parameters = "hide=true";		
   	this.options.onComplete =  this.onHide.bind(this);      	
   	new Ajax.Request(this.url, this.options);		
	this.hidden = true;
	this.indicateProcessing();
}	
/* Shows the facet box
 */
FacetBox.prototype.show= function(){
	this.options.parameters = "hide=false";		
   	this.options.onComplete =  this.onShow.bind(this);      	
   	new Ajax.Request(this.url, this.options);		
	this.hidden = false;
	this.indicateProcessing();
}
/* Displays a standard error message
 */
FacetBox.prototype.displayErrorDialog = function(){

	dojo.require("dojo.widget.*");
  	dojo.require("dojo.widget.Dialog");

	var dialogNode=document.createElement("div");
	dialogNode.setAttribute("id", "errorDialog");
	document.body.appendChild(dialogNode);

	var containerNode = document.createElement("div");
	dojo.html.setClass(containerNode, "errorDialogContainer");
	
	var headerNode=document.createElement("div");
	headerNode.innerHTML= "We received no response from the server. Maybe your session is expired or the system is down";
 	dojo.html.setClass(headerNode, "errorDialogHead");

 	var linkNode=document.createElement("div");
 	linkNode.setAttribute("id", "exceptionDialogHandle");
 	dojo.html.setClass(linkNode, "errorCloseLink");
 	linkNode.appendChild(document.createTextNode("Close"));
	
	containerNode.appendChild(headerNode);
 	
 	dialogNode.appendChild(containerNode);
 	dialogNode.appendChild(linkNode);		
	
 	var dialog=dojo.widget.createWidget("Dialog", {widgetId:"exception"}, dialogNode);
 	dojo.event.connect(linkNode, "onclick", dialog, "hide");
 	dojo.event.connect(dialog, "hide", dialog, "destroy");

	setTimeout(function(){ 
		dialog.show();
 	}, 100);
 
}

