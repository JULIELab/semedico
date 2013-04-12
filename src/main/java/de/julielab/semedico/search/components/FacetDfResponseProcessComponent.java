package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;

import de.julielab.util.SolrTfDfTripleStream;

public class FacetDfResponseProcessComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FacetDfResponseProcess {}
	
	private final Logger log;

	public FacetDfResponseProcessComponent(Logger log) {
		this.log = log;

	}

	@Override
	public boolean process(SearchCarrier searchCarrier) {
		QueryResponse solrResponse = searchCarrier.solrResponse;
		if (null == solrResponse)
			throw new IllegalArgumentException(
					"The solr response must not be null, but it is.");
		if (null == solrResponse.getResponse().get("facet_df_counts")) {
			log.warn("Terminating chain. Reason: The Solr response does not contain facet counts with document frequencies for any fields.");
			return true;
		}

		final List<Iterator<Entry<String, NamedList<Integer>>>> bTermCountLists = new ArrayList<Iterator<Entry<String, NamedList<Integer>>>>();
		// Extract the facet-document-frequency information from the response;
		// since the facetdf component is a custom component
		// (julie-solr-facet-df-component), the response cannot be retrieved by
		// a SolrJ API call.
		@SuppressWarnings("unchecked")
		NamedList<NamedList<NamedList<NamedList<Integer>>>> facetDfCounts = (NamedList<NamedList<NamedList<NamedList<Integer>>>>) solrResponse
				.getResponse().get("facet_df_counts");
		NamedList<NamedList<NamedList<Integer>>> fieldDfCounts = facetDfCounts
				.get("facet_field_df_counts");

		int numTermsReturned = 0;
		for (FacetCommand fc : searchCarrier.solrCmd.facetCmds) {
			// Just global facet settings
			if (fc.fields.size() == 0)
				continue;
			for (String field : fc.fields) {
				NamedList<NamedList<Integer>> bTermsDfCounts = fieldDfCounts
						.get(field);
				bTermCountLists.add(bTermsDfCounts.iterator());
				numTermsReturned += bTermsDfCounts.size();
			}
		}

		SolrTfDfTripleStream potentialBTermStream = new SolrTfDfTripleStream(
				bTermCountLists);
		log.debug("{} terms returned.", numTermsReturned);
		
		SemedicoSearchResult searchResult = searchCarrier.searchResult;
		if (null == searchResult){
			searchResult = new SemedicoSearchResult();
			searchCarrier.searchResult = searchResult;
		}
		searchResult.addSearchNodeTermCounts(potentialBTermStream);
		
		return false;
	}

}
