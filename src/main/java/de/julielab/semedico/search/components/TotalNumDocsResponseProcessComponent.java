package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.solr.client.solrj.response.QueryResponse;

public class TotalNumDocsResponseProcessComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TotalNumDocsResponseProcess {
	}
	
	@Override
	public boolean process(SearchCarrier searchCarrier) {
		QueryResponse solrResponse = searchCarrier.solrResponse;
		if (null == solrResponse)
			throw new IllegalArgumentException(
					"The solr response must not be null, but it is.");
		SemedicoSearchResult searchResult = searchCarrier.searchResult;
		if (null == searchResult) {
			searchResult = new SemedicoSearchResult();
			searchCarrier.searchResult = searchResult;
		}
		searchResult.totalNumDocs = solrResponse.getResults().getNumFound();
		return false;
	}

}
