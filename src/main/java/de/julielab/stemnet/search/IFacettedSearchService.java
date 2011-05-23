package de.julielab.stemnet.search;

import java.io.IOException;
import java.util.Collection;

import org.apache.solr.client.solrj.response.QueryResponse;

import com.google.common.collect.Multimap;

import de.julielab.stemnet.core.DocumentHit;
import de.julielab.stemnet.core.FacetConfiguration;
import de.julielab.stemnet.core.FacetTerm;
import de.julielab.stemnet.core.FacettedSearchResult;
import de.julielab.stemnet.core.SortCriterium;

public interface IFacettedSearchService {
	
	/**
	 * Returns a {@link FacettedSearchResult} which consists of the
	 * <code>documentHits</code>, <code>facetHits</code> (facet- and
	 * term-hit-counts), the ID-BitSet of retrieved documents, Lucene ScoreDocs
	 * (DocId and Score) and the total number of document hits.
	 */
	public FacettedSearchResult search(Collection<FacetConfiguration> facetConfigurations, 
			                           Multimap<String, FacetTerm> query, 
			                           SortCriterium sortCriterium,
			                           boolean filterReviews) throws IOException;

	public Collection<DocumentHit> constructDocumentPage(int start);
	
	/**
	 * Get the number of documents in the search server's index.
	 * @return The number of all documents stored in the index.
	 */
	public int getIndexSize();
}
