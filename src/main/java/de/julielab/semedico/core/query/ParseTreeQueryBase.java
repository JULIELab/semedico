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
	protected SearchMode searchMode;
	protected int resultSize;

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

	/**
	 * The maximum number of results that are actually fetched from the search
	 * server. There might be more hits but only the top-resultSize hits are
	 * loaded into Semedico.
	 * 
	 * @return The number of results fetched from the search server.
	 */
	public int getResultSize() {
		return resultSize;
	}

	/**
	 * Sets the maximum number of results that are actually fetched from the
	 * search server. There might be more hits but only the top-resultSize hits
	 * are loaded into Semedico.
	 * 
	 * @param resultSize
	 *            The number of results fetched from the search server.
	 */
	public void setResultSize(int resultSize) {
		this.resultSize = resultSize;
	}

}
