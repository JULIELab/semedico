package de.julielab.semedico.elasticsearch.index.setup.filter;

import de.julielab.semedico.elasticsearch.index.setup.Filter;

public class SnowballFilter extends Filter {
	public String language;

	public SnowballFilter(String language) {
		super("snowball");
		this.language = language;
	}
}
