package de.julielab.semedico.elasticsearch.index.setup.analyzer;

import de.julielab.semedico.elasticsearch.index.setup.Analyzer;
import de.julielab.semedico.elasticsearch.index.setup.Analyzers;

public class TextSpanAnalyzers extends Analyzers {
	public Analyzer semedico_text = new SemedicoTextAnalyzer();
}
