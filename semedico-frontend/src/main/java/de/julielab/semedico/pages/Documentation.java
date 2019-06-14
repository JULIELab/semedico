package de.julielab.semedico.pages;

import org.apache.tapestry5.annotations.Import;

@Import(
	library =
	{
		"context:js/jquery.min.js",
		"context:js/lightbox.js",
		"documentation.js"
	},
	stylesheet =
	{
		"context:css/lightbox.css",
		"context:css/documentation.css"
	})

public class Documentation
{
	// no functional content
}
