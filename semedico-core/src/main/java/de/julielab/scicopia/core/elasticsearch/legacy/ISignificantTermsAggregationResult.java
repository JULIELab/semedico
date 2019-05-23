package de.julielab.scicopia.core.elasticsearch.legacy;

import java.util.List;

public interface ISignificantTermsAggregationResult extends IAggregationResult {
	List<ISignificantTermsAggregationUnit> getAggregationUnits();
}
