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
	private Hits hits;

	public Hits onAction() throws IOException{

		hits.initialize();
		if( getQuery() == null || getQuery().equals("") ) {
			setQuery(getAutocompletionQuery());
		}
		
		hits.doNewSearch(getQuery(), getTermId());
		
		setQuery(null);
		setTermId(null);
		
		return hits;
	}
}