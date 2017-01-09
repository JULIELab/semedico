/**
 * Change the tids everytime you create new indices on the database!
 */

var myRegulation = "tid1852"; // Regulation of Gene Expression

/**
 * on afterRender() in ResultList.java
 */
function disableClicksAfterAjax(tutorialStep, incrementTutorialStepLink) {
	$j(document).ajaxStop(function() {
		if(hideWait()) {
			var listOfNotDisabledLinks = "a:not(#skipTutorial, #tid1848 a, .filterBoxContainer .filterRight a)";
			$j("a:not(#skipTutorial, #nextTutorialStep)").css("pointer-events", "none");
			showTutorialStep(tutorialStep, incrementTutorialStepLink);
		}
	});
	
}

function showWait() {
	$j('body').append("<div id='waitDiv'></div")
}


function hideWait() {
	if($j('#waitDiv').length > 0) {
		$j('#waitDiv').remove();
		return true;
	}
	return false;
}
/**
 * The event handler
 */


function disableAllClicks() {
	var listOfNotDisabledLinks = "a:not(#skipTutorial, #tid1848 a, .filterBoxContainer .filterRight a)";
	$j("a:not(#skipTutorial, #nextTutorialStep)").css("pointer-events", "none");
}

function disableSearchButton() {
	var searchButton = $j('#searchButton');
	searchButton.clone().attr('id','searchButton2').insertAfter(searchButton);
	searchButton.css("display","none");
	$j('#searchButton2').click(function(e){
		e.preventDefault();
		return false;
	});
};

function enableSearchButtonCheck() {
	$j('#searchButton2').click(function(e){
		var validInput = checkInputBeforeSubmit();
		if(validInput == true) {
			$j('#search').submit();
			return true;
		}
		else {
			e.preventDefault();
			return false;
		}
	});
}

function enableSearchButton() { 
	$j('#searchButton2').remove();
	$j('#searchButton').css("display","visible");
};

function disableKeypress() {
	$j('#token-input-searchInputField').keypress(function(e) {
		if(e.keyCode == 13 || e.which == 13) {
			e.preventDefault();
			return false;
		}
	});
};

function enableKeypressCheck() {
	$j('#token-input-searchInputField').keypress(function(e) {
		if(e.keyCode == 13 || e.which == 13) {
			var validInput = checkInputBeforeSubmit();
			if(validInput == true) {
				$j('#search').submit();
				return true;
			}
			else {
				e.preventDefault();
				return false;
			}
		}
	});
}

function enableKeypress() {
	$j('#token-input-searchInputField').keypress(function(e) {
		if(e.keyCode == 13 || e.which == 13) {
			return true;
		}
	});
}

function checkInputBeforeSubmit() {
	var input = $j('#token-input-searchInputField').val();
	if(input == "regulates")
		return true;
	else {
		var tutorialDialog = $j('#tutorialDialog');
		var tutorialDialogHeader = $j('#tutorialDialogContent h1');
		var tutorialDialogContent = $j('#tutorialDialogContent p');
		
		var overviewKeywords2 = 'Ooops. This is not the word we ask you to look up. If you like to skip the tutorial, you can do so ';
		overviewKeywords2 += 'by clicking on "Skip Tutorial". Otherwise type in "regulates" to continue.';
		
		tutorialDialog.css("display","block");
		tutorialDialog.removeClass();
		tutorialDialog.addClass("tutorial-overviewKeywords2-div");
		tutorialDialog.addClass("triangleRight");
		tutorialDialogHeader.text("The Search for Keywords");
		tutorialDialogContent.text(overviewKeywords2);
		tutorialDialog.position({my: "right center", at: "left-20 center", of: "#token-input-searchInputField"});
		return false;
	}
}

/**
 * Tutorial steps
 */

Tapestry.Initializer.startTutorial = function(step, incrementTutorialStepLink) {

	var tutorialStep = 0;
	if(step >= 0)
		tutorialStep = step;
	
	showWait();
	
	disableSearchButton();
	disableKeypress();
	disableClicksAfterAjax(tutorialStep, incrementTutorialStepLink);

};
//
function endTutorial() {
	var tutorialDialog = $j('#tutorialDialog');
	tutorialDialog.css("display", "none");
};

function showTutorialStep(tutorialStep, incrementTutorialStepLink) {
	var tutorialDialog = $j('#tutorialDialog');
	var tutorialStepDiv = getTutorialScriptArray(tutorialStep,incrementTutorialStepLink);
	var tutorialArraySize = getTutorialScriptArray().length;
	var tutorialDialogHeader = $j('#tutorialDialogContent h1');
	var tutorialDialogContent = $j('#tutorialDialogContent p');
	tutorialDialog.css("display","block");
	tutorialDialog.removeClass();
	if(tutorialStepDiv['modal'] == true) {
		$j('#modalDialog').addClass("modal");
	}
	else {
		$j('#modalDialog').removeClass("modal");
		$j('#modalDialog').addClass("nonmodal");
	}
	if(tutorialStepDiv['prepareStep']) {
		tutorialStepDiv.prepareStep();
	}
	/*
	 * For highlighting the area, which is described
	 * Not working in the moment, because we need to wait for AJAX to be ready 
	 */
	$j('#demoDiv').css("display", "none");
//	if(tutorialStepDiv['demo'] == true) {
//		var demoDiv = $j('#demoDiv');
//		demoDiv.css("display", "block;");
//		demoDiv.position(tutorialStepDiv['demoPosition']);
//		demoDiv.css("height", tutorialStepDiv['demoHeight']+20);
//		demoDiv.css("width", tutorialStepDiv['demoWidth']+20);
//	}
//	else {
//		$j('#demoDiv').css("display","none");
//	}
	tutorialDialog.addClass("tutorial-"+tutorialStepDiv["name"]+"-div");
	tutorialDialog.addClass(tutorialStepDiv["class"]);
	tutorialDialogHeader.text(tutorialStepDiv["h1"]);
	tutorialDialogContent.text(tutorialStepDiv["content"]);
	tutorialDialog.position(tutorialStepDiv["position"]);
	// Hide "Next"-Button at the end of the tutorial
	if(tutorialStep >= tutorialArraySize-1)
		$j('#nextTutorialStep').css("display", "none");
	else {
		$j('#nextTutorialStep').off("click");
		$j('#nextTutorialStep').click(function(){
			var validStep = tutorialStepDiv.validateStep(tutorialDialog, tutorialDialogHeader, tutorialDialogContent, tutorialStep);
			if(validStep == true) {
				var response = $j.post(incrementTutorialStepLink, function(data) { 
					showTutorialStep(data.tutorialStep, incrementTutorialStepLink);
				});
			}
		});
	}
}

/* Create the JSON Object containing the script */
function getTutorialScriptArray(step, incrementTutorialStepLink) {
	var resultListWelcome = 'After you hit the "find"-Button or the "Enter"-key on the index page you are directed to this page, which ';
	resultListWelcome += 'is called the "Result List". It consists of different areas which will be explained in the following. ';
	
	var overviewTabs = 'On top of the page, the tab bar is located. Here you can save different queries for later use.';
	
	var overviewSearch = 'On the top, you see the "Search Panel". Here you can start a new search or add a new term to the existing one. ';
	overviewSearch += 'The funcionality of this panel is similar to the one on the index page. The only difference here is the missing ';
	overviewSearch += 'event panel.';
	
	var overviewResult = 'The Search Panel is isolated by the Result Bar. Here you can see the number of documents found for query.';
	
	var overviewFacet = 'On the left hand side, the so-called Facet Panel is located. Our data is organized in different facets (displayed bold), which may ';
	overviewFacet += 'be interpreted as some kind of categories. The lists under the facet labels contain the terms with which you can restrain your search. ';
	overviewFacet += 'After each facet or term, the number of documents is displayed, in which results to your query have been found.';
	
	var overviewQuery = 'In the middle column, the query panel is located at the top of the column, just under the result bar. It contains the terms you ';
	overviewQuery += 'looked up via the search input field and/or through the facet panel.';
	
	var overviewDocuments = 'In the center of the page, you can see the document list. All the results for your search are listed here.';
	
	var overviewFilter = 'On the right hand side, the filter panel is located. ';
	
	var ambiguousTerm = "Let's have a closer look on the query panel. You searched for the term 'regulates', which Semedico ";
	ambiguousTerm += "recognizes as ambiguous. Just click on the word 'Ambiguous' and a new window will appear.";
	
	var disambiguation = "In this window, you can see the terms corresponding to your search. It is recommended to chose one of the ";
	disambiguation += "suggestions to refine the query. However, if you don't know the exact term you are looking for, you can just ";
	disambiguation += "press ESC to leave the disambiguation panel. Now, please choose 'regulation of gene expression' to continue.";
	
	var congratulation = 'You have reached the end of our interactive tutorial. We have that you have now a better inside on how to use ';
	congratulation += 'Semedico and its amazing features.';
	
	/**
	 * Every tutorial step is built like this (items with an * are mandatory and cannot be left out!):
	 * 
	 * 		name*: 			The name for the div
	 * 		position*: 		The position, where the tutorial div should be placed
	 * 		class*: 		The class, which should be added to the div, e.g. for the triangles. Possible triangle-Classes: noTriangle, triangleTop, triangleBottom, triangleLeft, triangleRight
	 * 		h1*: 			The headline for the div
	 * 		content*: 		The content of the div (the description)
	 * 		modal: 			true/false, determines whether the div is a modal div or not
	 * 		validateStep*: 	a function, which is called when clicking the next-button (if no check is necessary, simply put in: function () { return true;}
	 * 		prepareStep:	a function, which is called on the beginning of the step (e.g. to alter behaviour of links etc.)
	 * 		demo: 			true/false (not working at the moment), determines if another div is put in to highlight the described one
	 * 		demoPosition: 	the position on which the new div is added
	 * 		demoHeight: 	the height of the new div
	 * 		demoWidth: 		the width of the new div
	 */	
		
	var tutorialScriptArray = [
	{"name" : "welcome", "position" : {my: "center center", at: "center center", of: window}, "class": "noTriangle", 
		"h1": "The Result List", "content" : resultListWelcome, "modal": true, "validateStep" : function () { return true;}},
	{"name" : "overviewTabs", "position" : {my: "left top", at: "left bottom+20", of: '#pageTabsPanel'}, "class": "triangleTop", 
		"h1": "The Tab Bar", "content" : overviewTabs, "demo": true, demoPosition: {my: "left top", at: "left-10 top-10", of: "#pageTabsPanel"},
		"demoHeight" : $j('#pageTabsPanel').height(), "demoWidth": $j('#pageTabsPanel').width(), "validateStep" : function () { return true;}},
	{"name" : "overviewSearch", "position" : {my: "center top", at: "center bottom+20", of: '#searchInputDiv'}, "class": "triangleTop", 
		"h1": "The Search Panel", "content" : overviewSearch, "demo": true, demoPosition: {my: "left top", at: "left-10 top-10", of: "#searchPanelDiv"},
		"demoHeight" : $j('#searchPanelDiv').height(), "demoWidth": $j('#searchPanelDiv').width(), "validateStep" : function () { return true;}},
	{"name" : "overviewResult", "position" : {my: "center bottom", at: "center top-20", of: '#resultBar'}, "class": "triangleBottom", 
		"h1": "The Result Bar", "content" : overviewResult, "demo": true, demoPosition: {my: "left top", at: "left-10 top-10", of: "#resultBar"},
		"demoHeight" : $j('#resultBar').height(), "demoWidth": $j('#resultBar').width(), "validateStep" : function () { return true;}},
	{"name" : "overviewFacet", "position" : {my: "left top", at: "right+20 top+20", of: '#facetBar'}, "class": "triangleLeft", 
		"h1": "The Facet Panel", "content" : overviewFacet, "demo": true, demoPosition: {my: "left top", at: "left top-10", of: "#facetBar"},
		"demoHeight" : $j('#facetBar').height(), "demoWidth": $j('#facetBar').width(), "validateStep" : function () { return true;}},
	{"name" : "overviewQuery", "position" : {my: "center top", at: "center bottom+20", of: '#queryPanel'}, "class": "triangleTop", 
		"h1": "The Query Panel", "content" : overviewQuery, "demo": true, demoPosition: {my: "left top", at: "left-10 top-10", of: "#queryPanel"},
		"demoHeight" : $j('#queryPanel').height(), "demoWidth": $j('#queryPanel').width(), "validateStep" : function () { return true;}},
	{"name" : "overviewDocuments", "position" : {my: "center bottom", at: "center top-20", of: '#resultContainer'}, "class": "triangleBottom", 
		"h1": "The Document List", "content" : overviewDocuments, "demo": true, demoPosition: {my: "left top", at: "left-10 top-10", of: "#resultContainer"},
		"demoHeight" : $j('#resultContainer').height(), "demoWidth": $j('#resultContainer').width(), "validateStep" : function () { return true;}},
	{"name" : "overviewFilter", "position" : {my: "right top", at: "left-25 top+20", of: '#rightColumn'}, "class": "triangleRight", 
		"h1": "The Filter Panel", "content" : overviewFilter, "demo": true, demoPosition: {my: "left top", at: "left-10 top-10", of: "#rightColumn"},
		"demoHeight" : $j('#rightColumn').height(), "demoWidth": $j('#rightColumn').width(), "validateStep" : function () { return true; }},
	{"name" : "ambiguousTerm", "position" : {my: "center top", at: "center bottom+20", of: '#ambiguousqueryunit'}, "class": "triangleTop", 
		"h1": "The Disambiguation", "content" : ambiguousTerm, "validateStep" : function () { return true;},"prepareStep": function() {
			$j('#ambiguousqueryunit .filterBox a').css("pointer-events", "auto");
			$j('#ambiguousqueryunit .filterBox a').click(function(e) {
				var response = $j.post(incrementTutorialStepLink, function(data) { 
					showTutorialStep(data.tutorialStep, incrementTutorialStepLink);
				});
			});
			return true;
		}},
	{"name" : "disambiguation", "position" : {my: "left top", at: "left+10 top", of: '#facetBar'}, "class": "noTriangle", 
		"h1": "The Disambiguation", "content" : disambiguation, "validateStep" : function () { return true;},"prepareStep": function() {
			$j('#ambiguousqueryunit_disambiguationDialog  a').css("pointer-events", "none");
			$j('#tid1852').css("pointer-events", "auto");
			return true;
		}},
	{"name" : "congratulations", "position" : {my: "center center", at: "center center", of: window}, "class": "noTriangle", 
		"h1": "Congratulations!", "content" : congratulation, "modal" : true, "validateStep" : function () { return true;}}
//	{"name" : "overviewSearch3", "position" : {my: "right center", at: "left-20 center", of: "#searchInputDiv .token-input-list-suggestions"}, "class": "triangleRight",
//		"h1": "The Autocompletion of Semedico", "content" : overviewSearchContent3, 
//		"validateStep" : function (tutorialDialog, tutorialDialogHeader, tutorialDialogContent, tutorialStep) {
//			var searchInput = $j('#searchInputField').tokenInput("get");
//			if(searchInput.length == 1 && searchInput[0].termid == myP38)
//				return true;
//			else {
//				tutorialDialog.css("display","block");
//				tutorialDialog.removeClass();
//				tutorialDialog.addClass("tutorial-overviewSearch2-div");
//				tutorialDialog.addClass("triangleRight");
//				tutorialDialogHeader.text("The Autocompletion of Semedico");
//				tutorialDialogContent.text(overviewSearchContent2);
//				tutorialDialog.position({my: "right center", at: "left-20 center", of: "#searchInputDiv .token-input-list-suggestions"});
//				$j('#nextTutorialStep').click(function(){
//						showTutorialStep(tutorialStep);
//				});
//			}
//		}
//	},
	];
	if(step >= 0) {
		return tutorialScriptArray[step];
	}
	else
		return tutorialScriptArray;
	
}

/* Get the GET-Parameters from URL */
function getQueryVariable(variable) {
  var query = window.location.search.substring(1);
  var vars = query.split("&");
  for (var i=0;i<vars.length;i++) {
    var pair = vars[i].split("=");
    if (pair[0] == variable) {
      return pair[1];
    }
  } 
}