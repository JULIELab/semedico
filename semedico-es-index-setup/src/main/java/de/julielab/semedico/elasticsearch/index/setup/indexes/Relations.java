package de.julielab.semedico.elasticsearch.index.setup.indexes;

import de.julielab.semedico.elasticsearch.index.setup.Index;
import de.julielab.semedico.elasticsearch.index.setup.Mappings;
import de.julielab.semedico.elasticsearch.index.setup.SemedicoTextAnalysis;
import de.julielab.semedico.elasticsearch.index.setup.Settings;
import de.julielab.semedico.elasticsearch.index.setup.mapping.SemedicoDefaultMapping;
import de.julielab.semedico.elasticsearch.index.setup.property.RelationProperties;

public class Relations extends Index {

	public Relations() {
		super(new Settings(Settings.DEFAULT_NUM_REPLICAS, Settings.DEFAULT_NUM_SHARDS,
				new SemedicoTextAnalysis()),
				new Mappings(new SemedicoDefaultMapping(), new RelationProperties()));
	}

}
