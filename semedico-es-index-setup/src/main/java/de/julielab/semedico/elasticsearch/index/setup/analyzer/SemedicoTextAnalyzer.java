package de.julielab.semedico.elasticsearch.index.setup.analyzer;

import de.julielab.semedico.elasticsearch.index.setup.Analyzer;

public class SemedicoTextAnalyzer extends Analyzer {

	public SemedicoTextAnalyzer() {
		super("custom", "standard", "lowercase", "snow_english");
	}

}
