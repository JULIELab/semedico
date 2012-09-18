package de.julielab.semedico.base;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.FacetedSearchResult;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.pages.ResultList;
import de.julielab.semedico.search.interfaces.IFacetedSearchService;
import de.julielab.semedico.suggestions.ITermSuggestionService;

@Import(stylesheet = {"context:css/autocomplete.css"})
public class Search {

	@InjectPage
	private ResultList resultList;
	
	@Inject
	private IFacetedSearchService searchService;

	@Inject
	private Logger logger;
	
	@Persist
	private String query;

	@Persist
	private String termId;

	@Persist
	private String facetId;
	
	/**
	 * @return the facetId
	 */
	public String getFacetId() {
		return facetId;
	}

	/**
	 * @param facetId the facetId to set
	 */
	public void setFacetId(String facetId) {
		this.facetId = facetId;
	}

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
	
	protected ResultList performNewSearch() {
		if (getQuery() == null || getQuery().equals(""))
			setQuery(getAutocompletionQuery());
		
		logger.info("Starting search with query \"{}\"{}.", getQuery(), getTermId() == null ? "" : " (term ID)");
		FacetedSearchResult searchResult = searchService.search(getQuery(), new ImmutablePair<String, String>(getTermId(), getFacetId()), IFacetedSearchService.DO_FACET);
		resultList.setSearchResult(searchResult);
		setQuery(null);
		setTermId(null);
		setFacetId(null);
		return resultList;
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
