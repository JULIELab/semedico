package de.julielab.semedico.elasticsearch.index.setup.filter;

import de.julielab.semedico.elasticsearch.index.setup.Filter;

public class StopFilter extends Filter {
	public String stopwords_path;

	public StopFilter(String stopwords_path) {
		super("stop");
		this.stopwords_path = stopwords_path;
	}
}
