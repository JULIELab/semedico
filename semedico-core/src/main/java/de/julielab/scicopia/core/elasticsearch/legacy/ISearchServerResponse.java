package de.julielab.scicopia.core.elasticsearch.legacy;

import java.util.List;

import de.julielab.scicopia.core.elasticsearch.legacy.ISearchServerDocument;
import de.julielab.scicopia.core.elasticsearch.legacy.IFacetField;
import de.julielab.scicopia.core.elasticsearch.legacy.AggregationCommand;
import de.julielab.scicopia.core.elasticsearch.legacy.IAggregationResult;

public interface ISearchServerResponse {

	List<IFacetField> getFacetFields();

	List<ISearchServerDocument> getDocumentResults();
	
	IAggregationResult getAggregationResult(AggregationCommand aggCmd);

	long getNumFound();

	long getNumSuggestions();

	List<ISearchServerDocument> getSuggestionResults();
	
	boolean isSuggestionSearchResponse();
	
	void setSuggestionSearchResponse(boolean isSuggestionSearchResponse);

}
