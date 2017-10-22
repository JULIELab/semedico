package de.julielab.semedico.core.query;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import de.julielab.semedico.core.query.translation.SearchTask;
import de.julielab.semedico.core.services.SearchService.SearchOption;

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
	 * @deprecated Index types will be removed from ElasticSearch and we don't use them any more anyway.
	 */
	@Deprecated
	Collection<String> getIndexTypes();

	/**
	 * @param indexTypes
	 * @deprecated Index types will be removed from ElasticSearch and we don't use them any more anyway.
	 */
	@Deprecated
	void setIndexTypes(Collection<String> indexTypes);

	void setSearchOptions(EnumSet<SearchOption> searchOptions);

	EnumSet<SearchOption> getSearchOptions();
	
}
