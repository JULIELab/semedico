package de.julielab.stemnet.search;

import java.io.IOException;

import org.apache.solr.client.solrj.response.QueryResponse;

import de.julielab.stemnet.core.SortCriterium;

public interface ISearchService {
	
		
	public QueryResponse processQuery(String query, SortCriterium sortCriterium, boolean reviewFilter);


	/**
	 * Sets the path to the index directory.
	 * @param indexPath the index path
	 */
	

	public int getIndexSize();
	
}