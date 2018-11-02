package de.julielab.semedico.core.search.services;

import java.util.Arrays;
import java.util.stream.Stream;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.search.results.collectors.FieldTermCollector;

public class ResultCollectors {

	public static FieldTermCollector getFieldTermsCollector(String collectorName, String... aggregationNames) {
		return new FieldTermCollector(collectorName, Arrays.asList(aggregationNames));
	}

	public static FieldTermCollector getFieldTermsCollector(String collectorName, AggregationRequest... requests) {
		return getFieldTermsCollector(collectorName, Stream.of(requests).map(r -> r.name).toArray(String[]::new));
	}

}
