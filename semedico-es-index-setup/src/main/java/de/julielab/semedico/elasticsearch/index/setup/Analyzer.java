package de.julielab.semedico.elasticsearch.index.setup;

public class Analyzer {
	public String type;
	public String tokenizer;
	public String[] filter;

	public Analyzer(String type, String tokenizer, String... filter) {
		super();
		this.type = type;
		this.tokenizer = tokenizer;
		this.filter = filter;
	}

}
