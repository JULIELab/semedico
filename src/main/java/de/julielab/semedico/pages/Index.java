package de.julielab.semedico.pages;

import java.io.IOException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;

import de.julielab.semedico.base.Search;

/**
 * Start page of application semedico-frontend.
 */
@Import(stylesheet = {"context:css/layout_start.css"})
public class Index extends Search
{

	@InjectPage
	private Main mainPage;

	public Main onAction() throws IOException{

		mainPage.initialize();
		if( getQuery() == null || getQuery().equals("") ) {
			String autocompletionQuery = getAutocompletionQuery();
			if (autocompletionQuery == null || autocompletionQuery.equals(""))
				return null;
			setQuery(autocompletionQuery);
		}
		
		mainPage.doNewSearch(getQuery(), new ImmutablePair<String, String>(getTermId(), getFacetId()));
		
		setQuery(null);
		setTermId(null);
		setFacetId(null);
		
		return mainPage;
	}
}