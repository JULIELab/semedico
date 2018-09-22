$(function(){
		$("#token-input-searchInputField").focus();
		$("input:submit").attr("disabled", false);
})

$(function() {
	$('#search').submit(function() {
		var searchInput = $("#searchInputField").val();
		var pushItem = {
			tokenid : $("#token-input-searchInputField").val(),
			name : $("#token-input-searchInputField").val(),
			facetid : "fid-1",
			tokentype : "FREETEXT"
		};
		if (searchInput.length) {
		    // We want to insert the freetext token to the position where it was actually typed.
		    // This corresponds to the position where the input field token currently is positioned.
		    var inputToken = $('#searchInputDiv ul li.token-input-input-token-suggestions');
		    var inputTokenPosition = $('#searchInputDiv ul li').index(inputToken);
			var sentValue = JSON.parse(searchInput);
			if (pushItem.name.length)
			    sentValue = sentValue.slice(0, inputTokenPosition).concat([pushItem]).concat(sentValue.slice(inputTokenPosition));
//				sentValue.push(pushItem);
		} else {
			if (pushItem.name.length)
				var sentValue = [ pushItem ];
		}
		$("#searchInputField").val(JSON.stringify(sentValue));
		//Switch off submit button
		$("input:submit").attr("disabled", true);
		$("body").css("cssText", "cursor: wait !important;");
		$(".token-input-list-suggestions").css("cursor", "wait");
		$("input").css("cursor", "wait");
//		$("#searchButton").click(function(){
//			$("#searchButton").off("click");
//			$("body").css("cursor", "progress");
		
	});
});

