$(function() {
	$("#facetSectionList").sortable({
		cursor : "move",
		opacity : 0.75,
		placeholder : "facet-section-sortable-placeholder",
		forcePlaceholderSize : true,
		revert : true,
		handle : ".facetGroupSectionHeader",
		tolerance : "pointer"
	});
	// don't use, disables selection of this element AND ALL DESCENDING
	// $("#facetSectionList").disableSelection();

});

/**
 * currently unused because it doesn't work this way with the Ajax loading of
 * the facets
 * 
 * @param facetSectionId
 * @param url
 */
function makeFacetsSortable(facetSectionId, url) {
	var facetSortable = new FacetSortable(facetSectionId, url);
	var facetSection = document.getElementById(facetSectionId);
	facetSection.facetSortable = facetSortable;
}

/**
 * Makes the facet boxes draggable so they can be moved around in the list.
 * currently unused because it doesn't work this way with the Ajax loading of
 * the facets
 * 
 * @param facetSectionId
 * @param url
 */
function FacetSortable(facetSectionId, url) {
	this.facetSectionId = facetSectionId;
	this.url = url;

	// Apply sortable.
	this.applySortable = function() {
		// The element we get the ID of is the DIV for the whole facetSection.
		// This
		// element has a single child which is the <ul> element, listing the
		// individual facet boxes. On this element we want to apply sortable to
		// be
		// able to sort the facets.
		facets = document.getElementById(facetSectionId).childNodes[0].childNodes[0];
		Sortable.create(facets, {
			group : this.facetSectionId,
			animation : 150,
			// On end of a facet dragging, make the change known to the frontend
			// so
			// it can adjust the facet positions accordingly. Otherwise, the
			// positions would reset after each page load.
			onEnd : function(/** Event */
			evt) {
				var oldIndex = evt.oldIndex; // element's old index within
				// parent
				var newIndex = evt.newIndex; // element's new index within
				// parent
				$.post(this.url + '/' + oldIndex + '/' + newIndex);
			}
		});
	}
}

function loadSectionFacets(url, loaderPath, elementId) {
	// console.log("Loading section element " + elementId);
	var sectionElementSelector = "#" + elementId;
	renderLoaderImage($(sectionElementSelector), loaderPath);
	$.post(url, function(response) {
		var parent = $(sectionElementSelector).hide().html(response.content)
				.slideDown(500);
		// After the HTML replacement, the java script handling the facet box
		// functions like the 'more' button, drill to top, collapsing,
		// filtering, etc, is disfunctional because the old JS objects refer to
		// now-deprecated elements. However, the response from tapestry does not
		// only include the HTML elements but also the corresponding java script
		// calls that have been issued in the afterRender() method calls of the
		// facet boxes (see AbstractFacetBox.java). We have to iterate over
		// those JS calls and evaluate them.
		if (!response.inits || response.inits.length == 0){
		    return;
		}
		facetBoxJSObjects = response.inits[0].evalScript
		for (i = 0; i < facetBoxJSObjects.length; i++) {
			var evalString = facetBoxJSObjects[i];
			// this is a bit hackish: among the javascript, there is also the
			// function to load the facets asynchronously. If we would evaluate
			// that function, the facets would load all over again in an endless
			// loop. We assume that all calls to the 'FacetBox' object are the
			// correct ones (see facetbox.js).
			if (evalString.indexOf('FacetBox') != -1)
				eval(evalString);
		}
	});
}

function renderLoaderImage(jQueryObject, loaderPath) {
	jQueryObject.html("<img src=" + loaderPath + " />");
}