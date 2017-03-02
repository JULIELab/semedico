package de.julielab.semedico.core.query;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.translation.SearchTask;

public class StatementQuery implements ISemedicoQuery {

	private SearchTask task;
	private ParseTree query;
	private Collection<String> indexTypes;

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getQuery() {
		return (T) query;
	}

	@Override
	public Set<String> getSearchedFields() {
		return Collections.emptySet();
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
	public void setTask(SearchTask task) {
		this.task = task;
		
	}

	@Override
	public void setIndexTypes(Collection<String> indexTypes) {
		this.indexTypes = indexTypes;
	}

	public void setQuery(ParseTree query) {
		this.query = query;
		
	}

}
