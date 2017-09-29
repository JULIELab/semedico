package de.julielab.semedico.elasticsearch.index.setup.indexes;

import de.julielab.semedico.elasticsearch.index.setup.Analysis;
import de.julielab.semedico.elasticsearch.index.setup.Index;
import de.julielab.semedico.elasticsearch.index.setup.Mappings;
import de.julielab.semedico.elasticsearch.index.setup.Settings;
import de.julielab.semedico.elasticsearch.index.setup.analyzer.TextSpanAnalyzers;
import de.julielab.semedico.elasticsearch.index.setup.mapping.SemedicoDefaultMapping;
import de.julielab.semedico.elasticsearch.index.setup.property.LikelihoodTextSpanProperties;

public class Sentences extends Index {

	public Sentences() {
		super(new Settings(new Analysis(new TextSpanAnalyzers())),
				new Mappings(new SemedicoDefaultMapping(), new LikelihoodTextSpanProperties()));
	}

}
