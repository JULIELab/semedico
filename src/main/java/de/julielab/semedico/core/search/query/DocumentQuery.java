package de.julielab.semedico.core.search.query;

import java.util.Set;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.translation.SearchTask;

public class DocumentQuery extends ParseTreeQueryBase {

	private Set<String> searchFieldFilter;

	public DocumentQuery(ParseTree query, Set<String> searchFieldFilter) {
		super(query, SearchTask.DOCUMENTS);
		this.searchFieldFilter = searchFieldFilter;
		this.resultSize = 10;
	}

	@Override
	public Set<String> getSearchedFields() {
		return searchFieldFilter;
	}

}
