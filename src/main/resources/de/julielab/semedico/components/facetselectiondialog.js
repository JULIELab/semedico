function facetSelectionDialog() {
	$j(function() {
		$j("#facetSelectionList").bind("mousedown", function(e) {
			  e.metaKey = true;
		}).selectable({
			// The following combination of 'no cancel' but 'yes filter'
			// is the only combination capable of avoiding accidental 
			// label-selection, but still allows for selecting li elements by 
			// clicking on the respective label. I'm leaving the 'no cancel'
			// here because it might be needed in the future (e.g. when using
			// something other than labels).
			// cancel: 'li label',
			filter: 'li:not(label)',
			selected: function(event, ui) {
			},
			unselected: function(event, ui) {
			},
			stop: function() {
				var selectionResult = $j("#selectionResult").empty();
				$j(".ui-selected", this).each(function() {
					var index = $j("#facetSelectionList li").index(this);
					selectionResult.append(index + 1);
					// console.log(selectionResult); // DEBUG
				});
				
			},
		});
	});
	 	
	$j(function() {
		$j("#facetSelectionList > li").each(function(index) {
			// Notice the !. Whenever an element of the list does NOT contain
			// the checked-property (i.e. in our case IS to be displayed) we
			// mark it as selected.
			if(!$j("#facetSelectionList > li > input:eq("+index+")").is(":checked")) {
				$j("#facetSelectionList > li:eq("+index+")").addClass("ui-selected");
			}
		});
	});
};

// Called from FormDialog.java. We iterate over the list of selected elements 
// (i.e. everything with the id 'ui-selected') and, depending on whether the 
// element does have the appropriate id, check or uncheck the respective checkbox.
function checkBoxHelper() {
	$j(function() {
		$j("#facetSelectionList > li").each(function(index) {
			if($j("#facetSelectionList > li:eq("+index+")").hasClass("ui-selected")) {
				$j("#facetSelectionList > li > input:eq("+index+")").prop("checked", false);
			} else if(!$j("#facetSelectionList > li:eq("+index+")").hasClass("ui-selected")) {
				$j("#facetSelectionList > li > input:eq("+index+")").prop("checked", true);
			}
		});
	});
};

// Called directly from FormDialog.tml for filtering facets.
function filterHelper(filterText) {
	// The following snippet is used to make the contains-selector case-insensitive.
	// The original can be found here: http://goo.gl/lnRtl.
	$j.expr[":"].contains = $j.expr.createPseudo(function(arg) {
	    return function(elem) {
	        return $j(elem).text().toUpperCase().indexOf(arg.toUpperCase()) >= 0;
	    };
	});
	
	// Child-selector is faster than descendant-selector for this purpose.
	if($j(filterText).val().empty()) {
		$j("#facetSelectionList > li > label").parent().show();
	} else {
		$j("#facetSelectionList > li > label:not(:contains(" + $j(filterText).val() + "))").parent().hide();
	    $j("#facetSelectionList > li > label:contains(" + $j(filterText).val() + ")").parent().show();
	}
};

function facetFilterCheckAll() {
	$j(function() {
		$j("#facetSelectionList > li").each(function(index) {
			$j("#facetSelectionList > li:eq("+index+")").addClass("ui-selected");
		});
	});
};

function facetFilterUncheckAll() {
	$j(function() {
		$j("#facetSelectionList > li").each(function(index) {
			$j("#facetSelectionList > li:eq("+index+")").removeClass("ui-selected");
		});
	});
};

function focusHelper() {
};