function setupDisambiguationDialog(elementId, elementName, allocatedClientId) {
	console.log("Setting up dialog for element " + elementId);
	$j("#" + elementId).dialog({
		title : "Disambiguation of " + elementName,
		modal : true,
		draggable: false,
		autoOpen: false,
		dialogClass: 'disambiguation'
	});
	var widget = $j('#'+elementId).parent();
	widget.bind("clickoutside", function(event) {
		$j('#' + elementId).dialog("close");
	});
}

function openDisambiguationDialog(elementId) {
	$j("#" + elementId).dialog("open");
	var widget = $j('#'+elementId).parent();
	widget.focus();
}
