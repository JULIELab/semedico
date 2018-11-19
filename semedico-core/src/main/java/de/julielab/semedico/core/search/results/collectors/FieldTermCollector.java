package de.julielab.semedico.core.search.results.collectors;

import de.julielab.elastic.query.components.data.FieldTermItem;
import de.julielab.elastic.query.components.data.FieldTermItem.ValueType;
import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.elastic.query.components.data.aggregation.ITermsAggregationUnit;
import de.julielab.elastic.query.components.data.aggregation.MaxAggregationResult;
import de.julielab.elastic.query.components.data.aggregation.TermsAggregationResult;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import de.julielab.semedico.core.search.results.FieldTermsRetrievalResult;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.searchresponse.IAggregationSearchResponse;
import org.apache.commons.collections4.map.Flat3Map;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

public class FieldTermCollector extends SearchResultCollector<SemedicoESSearchCarrier, FieldTermsRetrievalResult> {

	private Collection<String> aggregationNames;

	public FieldTermCollector(Object collectorName, Collection<String> aggregationNames) {
		super(collectorName);
		this.aggregationNames = aggregationNames;
	}

	@Override
	public FieldTermsRetrievalResult collectResult(SemedicoESSearchCarrier semCarrier, int responseIndex) {
		Supplier<ISearchServerResponse> s1 = () -> semCarrier.getSearchResponse(0);
		Supplier<Map<String, AggregationRequest>> s2 = () -> semCarrier.getServerRequest(responseIndex).aggregationRequests;
		checkNotNull(s1, "Search Server Response", s2, "Aggregation Requests");
		stopIfError();
		Map<String, AggregationRequest> aggregationRequests = s2.get();
		aggregationNames.forEach(n -> checkNotNull((Supplier<AggregationRequest>) () -> aggregationRequests.get(n),
				"Aggregation with name " + n));
		stopIfError();

		FieldTermsRetrievalResult result = new FieldTermsRetrievalResult(
				aggregationNames.size() <= 3 ? new Flat3Map<>() : new HashMap<>(aggregationNames.size()));
		ISearchServerResponse serverResponse = s1.get();
		result.setNumDocumentsFound(serverResponse.getNumFound());
		for (String requestName : aggregationNames) {
			AggregationRequest fieldTermsAgg = aggregationRequests.get(requestName);

			TermsAggregationResult fieldTermsAggResult = (TermsAggregationResult) ((IAggregationSearchResponse)serverResponse)
					.getAggregationResult(fieldTermsAgg);
			List<ITermsAggregationUnit> terms = fieldTermsAggResult.getAggregationUnits();
			Builder<FieldTermItem> fieldTermStreamBuilder = Stream.builder();
			for (ITermsAggregationUnit termUnit : terms) {
				FieldTermItem fieldTermItem = new FieldTermItem();
				// Might be null depending on the sorting defined for the aggregation request
				// in AggregationRequests
				MaxAggregationResult docScore = (MaxAggregationResult) termUnit
						.getSubaggregationResult(ValueType.MAX_DOC_SCORE.name());
				fieldTermItem.term = termUnit.getTerm();
				fieldTermItem.setValue(FieldTermItem.ValueType.COUNT, termUnit.getCount());
				if (docScore != null)
					fieldTermItem.setValue(FieldTermItem.ValueType.MAX_DOC_SCORE, docScore.getValue());
				fieldTermStreamBuilder.accept(fieldTermItem);
			}
			result.put(requestName, fieldTermStreamBuilder.build());
			result.setSearchCarrier(semCarrier);
		}
		return result;
	}

	public Collection<String> getAggregationNames() {
		return aggregationNames;
	}
}
