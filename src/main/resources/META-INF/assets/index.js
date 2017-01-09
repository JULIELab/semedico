$j(function(){
	$j('#token-input-searchInputField').focus();
})

// $j(function() {
//
// // setupEventForm();
//
// // Bind click-event to examples
// $j("#exampleLink").click(function() {
// if ($j('#exampleList').is(':visible')) {
// $j('#exampleList').slideUp('slow');
// } else {
// $j('#exampleList').slideDown('slow');
// }
// });
// });
//
// /**
// * Adds all functionality for the event input form. Here, the Token Input
// * autocompletion is added, the enter key is prevented from submitting the
// * search input field etc.
// */
// function setupEventForm() {
// /*
// * If you're in the Event Panel, don't use the enter key to submit the form.
// * Instead add the current event form contents to the main query.
// */
// $j('#token-input-arg1Input').keypress(function(e) {
// if (e.keyCode == 13 || e.which == 13) {
// e.preventDefault();
// addEventQuery();
// return false;
// } else if (e.keyCode == 27 || e.which == 27) {
// $j('#searchInputField').focus();
// } else {
// return true;
// }
// });
// $j('#eventTypeSelector').keypress(function(e) {
// if (e.keyCode == 13 || e.which == 13) {
// e.preventDefault();
// addEventQuery();
// return false;
// } else if (e.keyCode == 27 || e.which == 27) {
// $j('#searchInputField').focus();
// } else {
// return true;
// }
// });
// $j('#token-input-arg2Input').keypress(function(e) {
// if (e.keyCode == 13 || e.which == 13) {
// e.preventDefault();
// addEventQuery();
// return false;
// } else if (e.keyCode == 27 || e.which == 27) {
// e.preventDefault();
// $j('#searchInputField').focus();
// } else {
// return true;
// }
// });
// /** ********* End: catch enter keypress *********** */
//
// $j('#token-input-arg1Input, #token-input-arg2Input').addClass(
// "autocomplete");
//
// // Remove contents kept by the browser
// var defaultEventValue = {
// tokenid : "[None]",
// name : "[None]",
// facetid : "fid-1"
// };
// $j("#arg1Input").tokenInput("add", defaultEventValue);
// $j("#arg2Input").tokenInput("add", defaultEventValue);
//
// // This is to repair JavaScriptFunctionality after Ajax calls.
// // This is the prototype version because we only use the Tapestry Zone
// // construct to add and delete table rows for event queries.
// Ajax.Responders.register({
// onComplete : function() {
// emptyEventOnNoneValue(defaultEventValue, "arg1Input");
// emptyEventOnNoneValue(defaultEventValue, "arg2Input");
// }
// });
// emptyEventOnNoneValue(defaultEventValue, "arg1Input");
// emptyEventOnNoneValue(defaultEventValue, "arg2Input");
//
// // Add a slide effect to the event query form to able to hide and how it on
// // click.
// $j("#eventFormToggleLink,#eventFormToggleTriangle").click(
// function() {
// if ($j("#eventZone").is(":visible")) {
// $j("#eventZone").slideUp("slow");
// $j("#eventFormToggleTriangle").addClass(
// "eventFormTriangleClosed").removeClass(
// "eventFormTriangleOpened");
// } else {
// $j("#eventZone").slideDown("slow");
// $j("#eventFormToggleTriangle").addClass(
// "eventFormTriangleOpened").removeClass(
// "eventFormTriangleClosed");
// }
// });
//
// // Add the click-handler fort the "Add Query" Button
// $j("#addQueryButton").click(function() {
// addEventQuery();
// });
// }
//
// function addEventQuery() {
// var $Query = $j("#searchInputField");
// var qString = "";
// var defaultEvent = "Any molecular interaction";
// var arg1 = $j.parseJSON($j("#arg1Input").val());
// var arg2 = $j.parseJSON($j("#arg2Input").val());
// var eventTypeId = $j('#eventTypeSelector').val();
// var eventTypeLabel = $j('#eventTypeSelector option:selected').text();
// $Query.tokenInput("add", {
// termid : "(",
// name : "(",
// facetid : "fid-1"
// });
// for (i = 0; i < arg1.length; i++) {
// if (i == 0 && arg1.length > 1) // add a bracket if argument contains
// // more than one token
// $Query.tokenInput("add", {
// termid : "(",
// name : "(",
// facetid : "fid-3",
// });
// if (arg1[i]['tokenid'] != '[None]')
// $Query.tokenInput("add", {
// name : arg1[i]['name'],
// facetid : arg1[i]['facetid'],
// tokenid : arg1[i]['tokenid']
// });
// if (i == arg1.length - 1 && arg1.length > 1) // close bracket
// $Query.tokenInput("add", {
// termid : ")",
// name : ")",
// facetid : "fid-3"
// });
// }
// $Query.tokenInput("add", {
// termid : eventTypeId,
// name : eventTypeLabel,
// facetid : "fid-1"
// });
// for (i = 0; i < arg2.length; i++) {
// if (i == 0 && arg2.length > 1) // add a bracket if argument contains
// // more than one token
// $Query.tokenInput("add", {
// termid : "(",
// name : "(",
// facetid : "fid-3"
// });
// if (arg2[i]['tokenid'] != '[None]')
// $Query.tokenInput("add", {
// name : arg2[i]['name'],
// facetid : arg2[i]['facetid'],
// tokenid : arg2[i]['tokenid']
// });
// if (i == arg2.length - 1 && arg2.length > 1) // close bracket
// $Query.tokenInput("add", {
// termid : ")",
// name : ")",
// facetid : "fid-3"
// });
// }
// $Query.tokenInput("add", {
// termid : ")",
// name : ")",
// facetid : "fid-3"
// });
//
// // reset the form upon query addition
// $j("#arg1Input").tokenInput("remove");
// $j("#arg2Input").tokenInput("remove");
// // reset to the first selection option, this should be the default option
// $j('#eventTypeSelector').prop('selectedIndex', 0);
//
// $j('#token-input-searchInputField').focus();
// };
//
// function emptyEventOnNoneValue(defaultEventValue, tokenInputField) {
// var hiddenInputField = $j('#' + tokenInputField)
// $j("#token-input-" + tokenInputField).focus(
// function() {
// var currentVal = hiddenInputField.tokenInput("get");
// if (currentVal.length == 1
// && $j.inArray(defaultEventValue, currentVal)) {
// hiddenInputField.tokenInput("remove");
// }
// ;
// }).focusout(function() {
// var currentVal = hiddenInputField.tokenInput("get");
// if (currentVal.length == 0) {
// hiddenInputField.tokenInput("add", defaultEventValue);
// }
// });
// };
//
// function EventTypeSelect(url) {
// this.url = url;
// $j('#eventTypeSelector').change(function() {
// var selectedType = $j('#eventTypeSelector').val();
// var data = url + '/' + selectedType;
// $j.post(data);
// });
// }
