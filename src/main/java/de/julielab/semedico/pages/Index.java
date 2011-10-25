package de.julielab.semedico.pages;

import java.io.IOException;

import org.apache.tapestry5.annotations.InjectPage;

import de.julielab.semedico.base.Search;

/**
 * Start page of application semedico-frontend.
 */
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
		
		
		
		mainPage.doNewSearch(getQuery(), getTermId());
		
		setQuery(null);
		setTermId(null);
		
		return mainPage;
	}
}