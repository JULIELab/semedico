package de.julielab.semedico.elasticsearch.index.setup;

import de.julielab.semedico.elasticsearch.index.setup.analyzer.TextSpanAnalyzers;
import de.julielab.semedico.elasticsearch.index.setup.filter.TextSpanFilters;

public class SemedicoTextAnalysis extends Analysis {

	public SemedicoTextAnalysis() {
		super(new TextSpanAnalyzers(), new TextSpanFilters());
	}

}
