package de.julielab.semedico.elasticsearch.index.setup.filter;

import de.julielab.semedico.elasticsearch.index.setup.Filter;
import de.julielab.semedico.elasticsearch.index.setup.Filters;

public class TextSpanFilters extends Filters {
	public Filter snow_english;

	public TextSpanFilters() {
		this.snow_english = new SnowballFilter("english");
	}
}
