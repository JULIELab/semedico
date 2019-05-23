package de.julielab.scicopia.core.elasticsearch.legacy;

import de.julielab.scicopia.core.elasticsearch.legacy.AggregationCommand.OrderCommand;

public class FieldTermsCommand {
	
	public enum OrderType {TERM, COUNT, DOC_SCORE}
	
	public String field;
	public int size;
	public OrderCommand.SortOrder[] sortOrders;
	// TODO document
	public OrderType[] orderTypes;
}
