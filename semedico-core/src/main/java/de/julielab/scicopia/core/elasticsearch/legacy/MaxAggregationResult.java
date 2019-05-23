package de.julielab.scicopia.core.elasticsearch.legacy;

import de.julielab.scicopia.core.elasticsearch.legacy.IMaxAggregationResult;

public class MaxAggregationResult implements IMaxAggregationResult {
	private String name;
	private Double value;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Double getValue() {
		return value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(Double value) {
		this.value = value;
	}

}
