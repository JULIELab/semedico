package de.julielab.semedico.base;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.ioc.annotations.Inject;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.services.IFacetService;
import de.julielab.semedico.suggestions.ITermSuggestionService;

public class Search {

	@Persist
	private String query;

	@Persist
	private String termId;

	@Persist
	private String autocompletionQuery;
	
	@Inject
	private ITermSuggestionService termSuggestionService;
	
	@Inject
	private IFacetService facetService;
	
	public List<FacetTermSuggestionStream> onProvideCompletions(String query) throws IOException, SQLException{
		
		if( query == null )
			return Collections.emptyList();
		
		autocompletionQuery = query;
		List<Facet> facets = facetService.getFacets();
		return termSuggestionService.getSuggestionsForFragment(query, facets);
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getTermId() {
		return termId;
	}

	public void setTermId(String termId) {
		this.termId = termId;
	}

	public String getAutocompletionQuery() {
		return autocompletionQuery;
	}

	public void setAutocompletionQuery(String autocompletionQuery) {
		this.autocompletionQuery = autocompletionQuery;
	}
}
