package de.julielab.semedico.elasticsearch.index.setup.indexes;

import de.julielab.semedico.elasticsearch.index.setup.Index;
import de.julielab.semedico.elasticsearch.index.setup.Mappings;
import de.julielab.semedico.elasticsearch.index.setup.SemedicoTextAnalysis;
import de.julielab.semedico.elasticsearch.index.setup.Settings;
import de.julielab.semedico.elasticsearch.index.setup.mapping.SemedicoDefaultMapping;
import de.julielab.semedico.elasticsearch.index.setup.property.DocumentProperties;

public class Documents extends Index {

	public Documents() {
		super(new Settings(new SemedicoTextAnalysis()),
				new Mappings(new SemedicoDefaultMapping(), new DocumentProperties()));
	}

}
