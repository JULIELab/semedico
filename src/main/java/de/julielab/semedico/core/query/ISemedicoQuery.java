package de.julielab.semedico.core.query;

import java.util.Collection;
import java.util.Set;

import de.julielab.semedico.core.query.translation.SearchTask;

public interface ISemedicoQuery {
	<T> T getQuery();

	Set<String> getSearchedFields();

	/**
	 * The search task to perform, e.g. fact search.
	 * 
	 * @return The task to perform.
	 */
	SearchTask getTask();

	/**
	 * The index to perform the query on.
	 * @return The index to search.
	 */
	String getIndex();
	
	/**
	 * The index types (e.g. medline in contrast to fulltext) to perform the
	 * search task on.
	 * 
	 * @return The index types to search on.
	 */
	Collection<String> getIndexTypes();

	void setIndexTypes(Collection<String> indexTypes);

}
