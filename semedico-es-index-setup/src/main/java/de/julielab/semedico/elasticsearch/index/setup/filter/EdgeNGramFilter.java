package de.julielab.semedico.elasticsearch.index.setup.filter;

import de.julielab.semedico.elasticsearch.index.setup.Filter;

public class EdgeNGramFilter extends Filter {
	public int min_gram;
	public int max_gram;
	public String side;

	public EdgeNGramFilter(int min_gram, int max_gram, String side) {
		super("edgeNGram");
		this.min_gram = min_gram;
		this.max_gram = max_gram;
		this.side = side;
	}
}
