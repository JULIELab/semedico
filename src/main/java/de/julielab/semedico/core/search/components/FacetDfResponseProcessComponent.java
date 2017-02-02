package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.IFacetField;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.elastic.query.util.TermCountCursor;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchResult;
import de.julielab.semedico.core.util.MergingTfDfTripleStream;

public class FacetDfResponseProcessComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FacetDfResponseProcess {
		//
	}

	private final Logger log;

	public FacetDfResponseProcessComponent(Logger log) {
		this.log = log;

	}

	@Override
	protected boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier)searchCarrier;
		ISearchServerResponse serverRsp = semCarrier.getSingleSearchServerResponse();
		if (null == serverRsp)
			throw new IllegalArgumentException("The search server response must not be null, but it is.");
		List<TermCountCursor> termDfLists = new ArrayList<>();
		List<IFacetField> facetFields = serverRsp.getFacetFields();

		int numTermsReturned = 0;
		for (IFacetField ff : facetFields) {
			TermCountCursor facetValues = ff.getFacetValues();
			termDfLists.add(facetValues);
			numTermsReturned += facetValues.size();
		}
		log.debug("{} terms returned.", numTermsReturned);

		
		MergingTfDfTripleStream mergedFieldValues = new MergingTfDfTripleStream(termDfLists);
		LegacySemedicoSearchResult semedicoSearchResult = new LegacySemedicoSearchResult(semCarrier.searchCmd.semedicoQuery);
		semedicoSearchResult.termDocumentFrequencies = mergedFieldValues;
		semCarrier.result = semedicoSearchResult;

		return false;
	}
}
