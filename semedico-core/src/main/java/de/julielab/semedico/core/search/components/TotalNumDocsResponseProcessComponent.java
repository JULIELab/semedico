package de.julielab.semedico.core.search.components;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import org.slf4j.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TotalNumDocsResponseProcessComponent extends AbstractSearchComponent {

	public TotalNumDocsResponseProcessComponent(Logger log) {
		super(log);
		// TODO Auto-generated constructor stub
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TotalNumDocsResponseProcess {
	}
	
	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoESSearchCarrier semCarrier = (SemedicoESSearchCarrier) searchCarrier;
		ISearchServerResponse solrResponse = semCarrier.getSingleSearchServerResponse();
		if (null == solrResponse)
			throw new IllegalArgumentException(
					"The solr response must not be null, but it is.");
		// TODO adapt
		LegacySemedicoSearchResult searchResult = null;//(LegacySemedicoSearchResult) semCarrier.result;
		if (null == searchResult) {
			searchResult = new LegacySemedicoSearchResult(semCarrier.searchCmd.semedicoQuery);
			// TODO adapt
//			semCarrier.result = searchResult;
		}
		searchResult.totalNumDocs = solrResponse.getNumFound();
		return false;
	}

}
