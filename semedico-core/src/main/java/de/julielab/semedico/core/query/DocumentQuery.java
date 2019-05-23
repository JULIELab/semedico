package de.julielab.semedico.core.query;

import java.util.Collection;
import java.util.Set;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.translation.SearchTask;

public class DocumentQuery implements ISemedicoQuery {

	private ParseTree query;
	private Set<String> searchFieldFilter;
	private Collection<String> indexTypes;
	private SearchTask task;

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
	public Set<String> getSearchedFields() {
		return searchFieldFilter;
	}

	@Override
	public SearchTask getTask() {
		return task;
	}

	@Override
	public Collection<String> getIndexTypes() {
		return indexTypes;
	}

	@Override
	public void setIndexTypes(Collection<String> indexTypes) {
		this.indexTypes = indexTypes;
		
	}

	@Override
	public void setTask(SearchTask task) {
		this.task = task;
	}

}
