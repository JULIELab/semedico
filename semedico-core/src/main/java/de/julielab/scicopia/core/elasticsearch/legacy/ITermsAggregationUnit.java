package de.julielab.scicopia.core.elasticsearch.legacy;

import java.util.Map;

public interface ITermsAggregationUnit {
	Object getTerm();
	long getCount();
	Map<String, IAggregationResult> getSubaggregationResults();
	IAggregationResult getSubaggregationResult(String name);
}
