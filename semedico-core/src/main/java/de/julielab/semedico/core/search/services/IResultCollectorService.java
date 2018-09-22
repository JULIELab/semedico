package de.julielab.semedico.core.search.services;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.search.results.collectors.FieldTermCollector;

public interface IResultCollectorService {
	FieldTermCollector getFieldTermsCollector(String name, String... aggregationNames);
	FieldTermCollector getFieldTermsCollector(String name, AggregationRequest... requests);
}
