package de.julielab.semedico.elasticsearch.index.setup;

public class Analysis {
	public Analyzers analyzer;
	public Filters filter;

	public Analysis(Analyzers analyzer) {
		super();
		this.analyzer = analyzer;
	}
	
	public Analysis(Analyzers analyzer, Filters filter) {
		this(analyzer);
		this.filter = filter;
	}
}
