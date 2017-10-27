package de.julielab.semedico.core.search.services;

import java.util.Arrays;
import java.util.stream.Stream;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.search.results.FieldTermCollector;

public class ResultCollectorService implements IResultCollectorService {

	@Override
	public FieldTermCollector getFieldTermsCollector(String name, String... aggregationNames) {
		return new FieldTermCollector(name, Arrays.asList(aggregationNames));
	}

	@Override
	public FieldTermCollector getFieldTermsCollector(String name, AggregationRequest... requests) {
		return getFieldTermsCollector(name, Stream.of(requests).map(r -> r.name).toArray(String[]::new));
	}

}
