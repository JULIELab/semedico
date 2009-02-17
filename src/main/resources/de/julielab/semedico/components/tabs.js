
function Tabs(selectedTab, url){
	this.url = url;
	this.selectedTab = selectedTab;
	this.options = {};
	this.options.asynchronous = true;
    this.options.method="get";
    
    this.options.encoding = "UTF-8";
	this.options.onComplete =  this.onComplete.bind(this);
	
	this.refreshListeners();
}

Tabs.prototype.onComplete = function(response){
	var content = response.responseText.evalJSON();
	if( content == "" ){
	}
	else{	
		this.facetBar.replace(content.content);
		var script = "<script>" + content.script + "</script>";
		script.evalScripts();
	}
	
	this.refreshListeners();
}

Tabs.prototype.refreshListeners = function(){
    this.firstTabHeader = $("firstTabHeader");
	this.firstTabLink = this.firstTabHeader.getElementsByTagName("a")[0];
	this.firstTabLink.onclick = this.activateFirstTab.bind(this);
	
    this.secondTabHeader = $("secondTabHeader");
    this.secondTabLink = this.secondTabHeader.getElementsByTagName("a")[0];
	this.secondTabLink.onclick = this.activateSecondTab.bind(this);            	

    this.thirdTabHeader = $("thirdTabHeader");
    this.thirdTabLink = this.thirdTabHeader.getElementsByTagName("a")[0];
	this.thirdTabLink.onclick = this.activateThirdTab.bind(this);    	
	this.facetBar = $("facetBar");
}

Tabs.prototype.activateFirstTab = function(){	
	if( this.selectedTab == "secondTab" ){
		this.secondTabHeader.className = "secondTabInActive";	
	}
	else if( this.selectedTab == "thirdTab" ){
		this.thirdTabHeader.className = "thirdTabInActive";			
	}
	 
	this.selectedTab = "firstTab";
		
	this.firstTabHeader.className = "firstTabActive";
	
	this.options.parameters = "selectedTab="+this.selectedTab;
	new Ajax.Request(this.url, this.options);	
}

Tabs.prototype.activateSecondTab = function(){	
	if( this.selectedTab == "firstTab" ){
		this.firstTabHeader.className = "firstTabInActive";	
	}
	else if( this.selectedTab == "thirdTab" ){
		this.thirdTabHeader.className = "thirdTabInActive";			
	}
	 
	this.selectedTab = "secondTab";
		
	this.secondTabHeader.className = "secondTabActive";
	
	this.options.parameters = "selectedTab="+this.selectedTab;
	new Ajax.Request(this.url, this.options);	
}

Tabs.prototype.activateThirdTab = function(){	
	if( this.selectedTab == "firstTab" ){
		this.firstTabHeader.className = "firstTabInActive";	
	}
	else if( this.selectedTab == "secondTab" ){
		this.secondTabHeader.className = "secondTabInActive";			
	}
	 
	this.selectedTab = "thirdTab";
		
	this.thirdTabHeader.className = "thirdTabActive";
	this.options.parameters = "selectedTab="+this.selectedTab;
	new Ajax.Request(this.url, this.options);	
}

