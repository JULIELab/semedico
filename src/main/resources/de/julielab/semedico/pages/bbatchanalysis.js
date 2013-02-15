function filterNonTermCheckbox(url) {
	this.url = url;
	var box = document.getElementById('filterNonTerms');
	box.onclick = function() {
		var checked = box.checked;
		$j.post(url + '/' + checked, function(response) {
			$j('#textarea').html(response.content);
		});
	};
}