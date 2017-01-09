package de.julielab.semedico.core.query;

import java.util.Set;

import de.julielab.semedico.core.parsing.ParseTree;

public class DocumentQuery implements ISemedicoQuery {

	private ParseTree query;
	private Set<String> searchFieldFilter;

	public DocumentQuery(ParseTree query, Set<String> searchFieldFilter) {
		this.query = query;
		this.searchFieldFilter = searchFieldFilter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getQuery() {
		return (T) query;
	}

	@Override
	public Set<String> getSearchFieldFilter() {
		return searchFieldFilter;
	}

}
