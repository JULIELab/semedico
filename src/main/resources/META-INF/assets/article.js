function getFulltextLinks(url, loaderPath) {
	renderLoaderImage($j("#fulltextLinksZone"), loaderPath);
	$j.post(url, function(response) {
		$j("#fulltextLinksZone").hide().html(response.content).slideDown("fast");
	});
}

function getRelatedArticles(url, loaderPath) {
	renderLoaderImage($j("#relatedLinksZone"), loaderPath);
	$j.post(url, function(response) {
		$j("#relatedLinksZone").hide().html(response.content).slideDown("fast");
	});
}

function renderLoaderImage(jQueryElement, loaderPath) {
	jQueryElement.html("<img src="+loaderPath+" />");
}