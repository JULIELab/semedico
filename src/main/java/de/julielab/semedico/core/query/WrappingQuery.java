package de.julielab.semedico.core.query;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import de.julielab.semedico.core.query.translation.SearchTask;
import de.julielab.semedico.core.services.SearchService.SearchOption;

public class WrappingQuery implements ISemedicoQuery {

	private ISemedicoQuery wrappedQuery;
	private EnumSet<SearchOption> searchOptions;

	public WrappingQuery(ISemedicoQuery wrappedQuery) {
		this.wrappedQuery = wrappedQuery;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getQuery() {
		return (T) wrappedQuery;
	}

	@Override
	public Set<String> getSearchedFields() {
	return wrappedQuery.getSearchedFields();
	}

	@Override
	public SearchTask getTask() {
	return wrappedQuery.getTask();
	}

	@Override
	public String getIndex() {
	return wrappedQuery.getIndex();
	}

	@Override
	public Collection<String> getIndexTypes() {
		return wrappedQuery.getIndexTypes();
	}

	@Override
	public void setIndexTypes(Collection<String> indexTypes) {
		wrappedQuery.setIndexTypes(indexTypes);

	}

	@Override
	public void setSearchOptions(EnumSet<SearchOption> searchOptions) {
		this.searchOptions = searchOptions;
		
	}

	@Override
	public EnumSet<SearchOption> getSearchOptions() {
		return searchOptions;
	}

}
