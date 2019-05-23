package de.julielab.scicopia.core.elasticsearch.legacy;

public interface ISignificantTermsAggregationUnit {
	String getTerm();
	long getDocCount();
}
