package de.julielab.scicopia.core.elasticsearch.legacy;

import org.elasticsearch.search.sort.SortOrder;

public class SortCommand {
	
	public SortCommand(String field, SortOrder order) {
		this.field = field;
		this.order = order;
	}
	
	public String field;
	@Override
	public String toString() {
		return "SortCommand [field=" + field + ", order=" + order + "]";
	}
	public SortOrder order;
}
