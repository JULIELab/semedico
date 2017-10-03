package de.julielab.semedico.elasticsearch.index.setup.property;

import de.julielab.semedico.elasticsearch.index.setup.AnalyzerTypes;
import de.julielab.semedico.elasticsearch.index.setup.MappingProperties;
import de.julielab.semedico.elasticsearch.index.setup.MappingProperty;
import de.julielab.semedico.elasticsearch.index.setup.MappingProperty.TermVector;
import de.julielab.semedico.elasticsearch.index.setup.MappingTypes;

public class RelationProperties extends MappingProperties {
	public MappingProperty arguments;
	public MappingProperty types;
	public MappingProperty likelihood;
	public MappingProperty sentence;
	
	public RelationProperties() {
		arguments = new MappingProperty(MappingTypes.keyword, true);
		types = new MappingProperty(MappingTypes.keyword, true);
		likelihood = new MappingProperty(MappingTypes.integer, true);
		sentence = new MappingProperty(MappingTypes.preanalyzed, false, true, AnalyzerTypes.semedico_text, TermVector.with_positions_offsets, false);
	}
}
