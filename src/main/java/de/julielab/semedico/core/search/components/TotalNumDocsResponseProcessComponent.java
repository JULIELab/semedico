package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;

public class TotalNumDocsResponseProcessComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TotalNumDocsResponseProcess {
	}
	
	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		ISearchServerResponse solrResponse = semCarrier.getSingleSearchServerResponse();
		if (null == solrResponse)
			throw new IllegalArgumentException(
					"The solr response must not be null, but it is.");
		LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) semCarrier.result;
		if (null == searchResult) {
			searchResult = new LegacySemedicoSearchResult(semCarrier.searchCmd.semedicoQuery);
			semCarrier.result = searchResult;
		}
		searchResult.totalNumDocs = solrResponse.getNumFound();
		return false;
	}

}
