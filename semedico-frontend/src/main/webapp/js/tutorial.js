/**
 * Change the tids everytime you create new indices on the database!
 */

var myP38 = "tid1839"; // p38 in Genes and Proteins
var myMef2 = "tid1842"; // Mef2 in Genes and Proteins
var myEventType = "Positive regulation";

$j(document).ready(function(){
	
	var tutorialMode = getQueryVariable("tutorialMode");
	
	if(tutorialMode == 'true') {
		
		
		startTutorial();
	}
	else if(tutorialMode == 'false') {
		endTutorial();
	}
});

/**
 * The event handler
 */

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

function disableAddQueryButton() {
	var addQueryButton = $j('#addQueryButton');
	addQueryButton.clone().attr('id','addQueryButton2').insertAfter(addQueryButton);
	addQueryButton.css("display","none");
	$j('#addQueryButton2').off("click");
};

function enableAddQueryButton() {
	$j('#addQueryButton2').remove();
	$j('#addQueryButton').css("display", "visible");
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

function startTutorial() {
	var tutorialStep = 8;
	
	disableSearchButton();
	disableAddQueryButton();
	disableKeypress();
	
	
	showTutorialStep(tutorialStep);
};
//
function endTutorial() {
	var tutorialDialog = $j('#tutorialDialog');
	tutorialDialog.css("display", "none");
};

function showTutorialStep(tutorialStep) {
	var tutorialDialog = $j('#tutorialDialog');
	var tutorialStepDiv = getTutorialScriptArray(tutorialStep);
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
	tutorialDialog.addClass("tutorial-"+tutorialStepDiv["name"]+"-div");
	tutorialDialog.addClass(tutorialStepDiv["class"]);
	tutorialDialogHeader.text(tutorialStepDiv["h1"]);
	tutorialDialogContent.text(tutorialStepDiv["content"]);
	tutorialDialog.position(tutorialStepDiv["position"]);

	$j('#nextTutorialStep').click(function(){
		var validStep = tutorialStepDiv.validateStep(tutorialDialog, tutorialDialogHeader, tutorialDialogContent, tutorialStep);
		if(validStep == true)
			showTutorialStep(tutorialStep+1);
	});
	
}

/* Create the JSON Object containing the script */
function getTutorialScriptArray(step) {
	var welcomeContent = "We'd like to give you a brief introduction in how Semedico works by describing ";
	welcomeContent+= "the basic functions and features. You can continue to the next tutorial steps by hitting the ";
	welcomeContent+= "Next-link. If you feel like you don't need any more introduction you can cancel it at any point ";
	welcomeContent+= "with the 'skip Tutorial'-link";
	
	var overviewStartContent = "At first, we like to show you the index page of Semedico with it basic structure. ";
	overviewStartContent+= "We will demonstrate the different kinds of queries you are able to commit. ";
	
	var overviewHelpContent = "In the top right corner of the window you can see the Help / Documentation link. You will ";
	overviewHelpContent+= "be directed to our documentation page with detailed information about semantic search in general and ";
	overviewHelpContent+= "Semedico in particular.";
	
	var overviewContact = 'On the bottom of the page, we left our contact information. Feel free to use these if you have any ';
	overviewContact += 'questions or should discover any errors. In addition we ask you to give us feedback on what we can improve to make your search more ';
	overviewContact += 'convinient. Thank you!';
	
	var overviewSearchContent1 = 'This is the general input field you will see throughout Semedico. It provides you with an ';
	overviewSearchContent1+= 'autocompletion functionality so that you are able to select the accurate term you were looking for. ';
	overviewSearchContent1+= 'Try it! Type in some terms and have a look around.';
	
	var overviewSearchContent2 = 'Ooops. This is not the word we ask you to look up. If you like to skip the tutorial, you can do so ';
	overviewSearchContent2 += 'by clicking on "Skip Tutorial". Otherwise type in "p38" to continue.';
	
	var overviewSearchContent3 = 'Now clear the input field by clicking on the "x" right of the term names and type in "p38". ';
	overviewSearchContent3 += 'Look at the suggestions. Semedico offers you the ability to chose the exact term you want to look up. ';
	overviewSearchContent3 += 'If the term is ambiguous, you can chose the right one from one of the categories, which we call facets. ';
	overviewSearchContent3 += 'If Semedico cannot find you desired term or you are not sure which one of the show terms is the right one, ';
	overviewSearchContent3 += 'you can browse with the arrow keys or the mouse over the term to get a brief summary of the synonyms so you ';
	overviewSearchContent3 += 'might be able to identify the term you are looking for. ';
	overviewSearchContent3 += 'Now, chose one of the suggestions!';
	
	var overviewEvent1 = 'Very good! You can see that one term is saved in the input field and once you hit the "find" button ';
	overviewEvent1 += 'Semedico will find the corresponding documents. But there is another way to search in Semedico: ';
	overviewEvent1 += 'looking beneath the input field you can see the "Event Query Panel". With a click you can open it. Try it!';
	
	var overviewEvent2 = 'The Event Query Panel is used to make a so-called fact or event search. With the three input field you can ';
	overviewEvent2 += 'search for events with up to two arguments. The events are limited to genes and proteins and therefore the ';
	overviewEvent2 += 'auto completion shows just terms out of this facet. You can type in the terms you are looking for or you can ';
	overviewEvent2 += 'use keywords like "ANY" to select an arbitrary event argument or "NONE" to search with only one argument. ';
	overviewEvent2 += 'Let us try the event search for "p38", "Positive Regulation", "Mef2".';
	
	var overviewEvent3 = 'Ooops. This is not the word we ask you to look up. If you like to skip the tutorial, you can do so ';
	overviewEvent3 += 'by clicking on "Skip Tutorial". Otherwise search for "p38", "Positive Regulation" and "Mef2" to continue.';
	
	var overviewEvent4 = 'Very good! Now hit the "Add query" button and your chosen query will appear in the search input field ';
	overviewEvent4 += 'on the top.';
	
	var overviewKeywords1 = 'There is a third way to look for terms, which we would like to demonstrate. For your own convenience, ';
	overviewKeywords1 += 'we cleared the input field so that you can just type in the term you want to look up and hit the enter ';
	overviewKeywords1 += 'key or click the "find" button. Try typing in the word "regulates" and hit enter!';
	
	var overviewKeywords2 = 'Ooops. This is not the word we ask you to look up. If you like to skip the tutorial, you can do so ';
	overviewKeywords2 += 'by clicking on "Skip Tutorial". Otherwise type in "regulates" to continue.';
	
	
	var tutorialScriptArray = [
	{"name" : "welcome", "position" : {my: "center center", at: "center center", of: window}, "class": "noTriangle", 
		"h1": "Welcome to Semedico", "content" : welcomeContent, "modal": true, "validateStep" : function () { return true;}},
	{"name" : "overviewStart", "position" : {my: "center center", at: "center center", of: window}, "class": "noTriangle",
		"h1": "Semedico - An Overview", "content" : overviewStartContent, "modal": true,  "validateStep" : function () { return true;}},
	{"name" : "overviewHelp", "position" : {my: "right top", at: "right-10 bottom+10", of: "#linkPanel"}, "class": "triangleTop",
		"h1": "Help and Documentation", "content" : overviewHelpContent, "validateStep" : function () { return true;}},
	{"name" : "overviewContact", "position" : {my: "center bottom", at: "center top-20", of: "#infoPanel"}, "class": "triangleBottom",
		"h1": "Contact us", "content" : overviewContact, "validateStep" : function () { return true;}},
	{"name" : "overviewSearch1", "position" : {my: "right center", at: "left-20 center", of: "#searchInputDiv .token-input-list-suggestions"}, "class": "triangleRight",
		"h1": "The Autocompletion of Semedico", "content" : overviewSearchContent1, 
		"validateStep" : function () { return true; }
	},
	{"name" : "overviewSearch3", "position" : {my: "right center", at: "left-20 center", of: "#searchInputDiv .token-input-list-suggestions"}, "class": "triangleRight",
		"h1": "The Autocompletion of Semedico", "content" : overviewSearchContent3, 
		"validateStep" : function (tutorialDialog, tutorialDialogHeader, tutorialDialogContent, tutorialStep) {
			var searchInput = $j('#searchInputField').tokenInput("get");
			if(searchInput.length == 1 && searchInput[0].termid == myP38)
				return true;
			else {
				tutorialDialog.css("display","block");
				tutorialDialog.removeClass();
				tutorialDialog.addClass("tutorial-overviewSearch2-div");
				tutorialDialog.addClass("triangleRight");
				tutorialDialogHeader.text("The Autocompletion of Semedico");
				tutorialDialogContent.text(overviewSearchContent2);
				tutorialDialog.position({my: "right center", at: "left-20 center", of: "#searchInputDiv .token-input-list-suggestions"});
				$j('#nextTutorialStep').click(function(){
						showTutorialStep(tutorialStep);
				});
			}
		}
	},
	{"name" : "overviewEvent1", "position" : {my: "right center", at: "left-20 center", of: "#eventFormToggleTriangle"}, "class": "triangleRight",
		"h1": "The Event Query Panel", "content" : overviewEvent1, 
		"validateStep" : function (tutorialDialog, tutorialDialogHeader, tutorialDialogContent, tutorialStep) { 
			var eventZone = $j('#eventZone');
			if(eventZone.css("display") == "none")
			 {
				tutorialDialog.css("display","block");
				tutorialDialog.removeClass();
				tutorialDialog.addClass("tutorial-overviewEvent-div");
				tutorialDialog.addClass("triangleRight");
				tutorialDialogHeader.text("The Event Query Panel");
				tutorialDialogContent.text('Please open the event Panel by clicking on the link "Show Event Query Panel".');
				tutorialDialog.position({my: "right center", at: "left-20 center", of: "#eventFormToggleTriangle"});
				$j('#nextTutorialStep').click(function(){
						showTutorialStep(tutorialStep);
				});
			 }
			else
				return true;
		}
	},
	{"name" : "overviewEvent2", "position" : {my: "right center", at: "left-20 center", of: "#eventZone .token-input-list-suggestions"}, "class": "triangleRight",
		"h1": "The Event Query Panel", "content" : overviewEvent2, 
		"validateStep" : function (tutorialDialog, tutorialDialogHeader, tutorialDialogContent, tutorialStep) { 
			var arg1 = $j('#arg1Input').tokenInput("get");
			var arg2 = $j('#arg2Input').tokenInput("get");
			var eventType = $j('#eventTypeSelector').val();
			if(arg1.length == 1 && arg2.length == 1 && arg1[0].termid == myP38 && arg2[0].termid == myMef2 && eventType == myEventType) {
				enableAddQueryButton();
				return true;
			}
			else {
				tutorialDialog.css("display","block");
				tutorialDialog.removeClass();
				tutorialDialog.addClass("tutorial-overviewEvent3-div");
				tutorialDialog.addClass("triangleRight");
				tutorialDialogHeader.text("The Event Query Panel");
				tutorialDialogContent.text(overviewEvent3);
				tutorialDialog.position({my: "right center", at: "left-20 center", of: "#eventZone .token-input-list-suggestions"});
				$j('#nextTutorialStep').click(function(){
						showTutorialStep(tutorialStep);
				});
			 }
		}
	},
	{"name" : "overviewEvent4", "position" : {my: "left center", at: "right+20 center", of: "#addQueryButton"}, "class": "triangleLeft",
		"h1": "The Event Query Panel", "content" : overviewEvent4, 
		"validateStep" : function () { 
			$j('#searchInputField').tokenInput("remove");
			$j('#token-input-searchInputField').focus();
			enableKeypressCheck();
			enableSearchButtonCheck();
			return true;
		}
	},
	{"name" : "overviewKeywords1", "position" : {my: "center bottom", at: "center top-20", of: "#searchInputDiv"}, "class": "triangleBottom",
		"h1": "The Search for Keywords", "content" : overviewKeywords1, 
		"validateStep" : function (tutorialDialog, tutorialDialogHeader, tutorialDialogContent, tutorialStep) { 
			var searchInput = $j('#token-input-searchInputField').val();
			if(searchInput == "regulates") {
				return true;
			}
			else
			 {
				tutorialDialog.css("display","block");
				tutorialDialog.removeClass();
				tutorialDialog.addClass("tutorial-overviewKeywords2-div");
				tutorialDialog.addClass("triangleRight");
				tutorialDialogHeader.text("The Search for Keywords");
				tutorialDialogContent.text(overviewKeywords2);
				tutorialDialog.position({my: "right center", at: "left-20 center", of: "#token-input-searchInputField"});
				$j('#nextTutorialStep').click(function(){
						showTutorialStep(tutorialStep);
				});
			 }
		}
	}
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
