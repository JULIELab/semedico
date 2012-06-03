package de.julielab.semedico.pages;

import java.io.IOException;

import org.apache.tapestry5.annotations.Import;

import de.julielab.semedico.base.Search;

/**
 * Start page of application semedico-frontend.
 */
@Import(stylesheet = { "context:css/layout_start.css" })
public class Index extends Search {

	public Object onAction() throws IOException {

		if (getQuery() == null || getQuery().equals("")) {
			String autocompletionQuery = getAutocompletionQuery();
			if (autocompletionQuery == null || autocompletionQuery.equals(""))
				return null;
			setQuery(autocompletionQuery);
		}

		ResultList resultList = performNewSearch();

		setQuery(null);
		setTermId(null);
		setFacetId(null);

		return resultList;
	}
}