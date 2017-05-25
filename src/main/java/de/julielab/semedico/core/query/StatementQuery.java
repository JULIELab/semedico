package de.julielab.semedico.core.query;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.translation.SearchTask;

public class StatementQuery implements ISemedicoQuery {

	private ParseTree query;
	private String index;
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
		return SearchTask.STATEMENTS;
	}

	@Override
	public Collection<String> getIndexTypes() {
		return indexTypes;
	}

	@Override
	public void setIndexTypes(Collection<String> indexTypes) {
		this.indexTypes = indexTypes;
	}

	public void setQuery(ParseTree query) {
		this.query = query;
		
	}

	public void setIndex(String index) {
		this.index = index;
	}
	
	@Override
	public String getIndex() {
		return index;
	}

}
