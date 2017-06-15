package de.julielab.semedico.core.query;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.translation.SearchTask;
import de.julielab.semedico.core.services.SearchService.SearchMode;

/**
 * A basic query implementation using a {@link ParseTree} as query
 * representation and may be set an index and index types. Only the task of the
 * query must be given by extending classes.
 * 
 * @author faessler
 *
 */
public class ParseTreeQueryBase implements ISemedicoQuery {

	protected ParseTree query;
	protected String index;
	protected Collection<String> indexTypes;
	protected SearchTask task;
	private SearchMode searchMode;

	public ParseTreeQueryBase(SearchTask searchTask) {
		this(null, searchTask);
	}

	public ParseTreeQueryBase(ParseTree query, SearchTask searchTask) {
		this.query = query;
		task = searchTask;
	}

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
	public Collection<String> getIndexTypes() {
		return indexTypes;
	}

	@Override
	public void setIndexTypes(Collection<String> indexTypes) {
		this.indexTypes = indexTypes;
	}

	public void setQuery(ParseTree query) {
		assert query != null;
		this.query = query;

	}

	public void setIndex(String index) {
		this.index = index;
	}

	@Override
	public String getIndex() {
		return index;
	}

	@Override
	public SearchTask getTask() {
		return task;
	}

	@Override
	public String toString() {
		return "ParseTreeQueryBase [query=" + query + ", index=" + index + ", indexTypes=" + indexTypes + ", task="
				+ task + "]";
	}

	@Override
	public SearchMode getSearchMode() {
		return searchMode;
	}

	@Override
	public void setSearchMode(SearchMode searchMode) {
		this.searchMode = searchMode;
		
	}

}
