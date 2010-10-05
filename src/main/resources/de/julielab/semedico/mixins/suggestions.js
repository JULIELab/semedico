/* This function is called from the suggestion list entries onClick
 * event. It generates a query URL and sets the search field value
 * to the chosen suggestion.
 */
 function selectSuggestion(element, selectedElement){
	 var suggestURL = $T("searchInputField").suggestURL;
	 var newTerm = Element.collectTextNodesIgnoreClass(selectedElement, 'informal').strip();
     var id = selectedElement.id;
     var url = suggestURL + "?query=" + newTerm + "&id="+id;
     
     if( newTerm != null && newTerm != "" ){
    	 element.value = newTerm;
     }
     window.location.href= url;
 }