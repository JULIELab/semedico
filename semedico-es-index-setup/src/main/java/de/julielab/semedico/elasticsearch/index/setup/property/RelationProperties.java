package de.julielab.semedico.elasticsearch.index.setup.property;

import de.julielab.semedico.elasticsearch.index.setup.AnalyzerTypes;
import de.julielab.semedico.elasticsearch.index.setup.MappingProperties;
import de.julielab.semedico.elasticsearch.index.setup.MappingProperty;
import de.julielab.semedico.elasticsearch.index.setup.MappingProperty.TermVector;
import de.julielab.semedico.elasticsearch.index.setup.MappingTypes;

public class RelationProperties extends MappingProperties {
	public MappingProperty arguments;
	public MappingProperty argumentwords;
	public MappingProperty types;
	public MappingProperty likelihood;
	public MappingProperty sentence;
	public MappingProperty begin;
	public MappingProperty end;
	public MappingProperty sentencebegin;
	public MappingProperty sentenceend;
	public MappingProperty numarguments;
	public MappingProperty numdistinctarguments;
	public MappingProperty source;
	
	public RelationProperties() {
		arguments = new MappingProperty(MappingTypes.keyword, false, true);
		argumentwords = new MappingProperty(MappingTypes.text, false, true);
		types = new MappingProperty(MappingTypes.keyword, false, true);
		likelihood = new MappingProperty(MappingTypes.integer, true, true);
		sentence = new MappingProperty(MappingTypes.preanalyzed, false, true, AnalyzerTypes.semedico_text, TermVector.with_positions_offsets, false);
		begin = new MappingProperty(MappingTypes.integer, true);
		end = new MappingProperty(MappingTypes.integer, true);
		sentencebegin = new MappingProperty(MappingTypes.integer, true);
		sentenceend = new MappingProperty(MappingTypes.integer, true);
		numarguments = new MappingProperty(MappingTypes.integer, false, true);
		numdistinctarguments = new MappingProperty(MappingTypes.integer, false, true);
		source = new MappingProperty(MappingTypes.text, true, true);
	}
}
