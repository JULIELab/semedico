package de.julielab.semedico.elasticsearch.index.setup.property;

import de.julielab.semedico.elasticsearch.index.setup.AnalyzerTypes;
import de.julielab.semedico.elasticsearch.index.setup.MappingProperty;
import de.julielab.semedico.elasticsearch.index.setup.MappingTypes;

public class TextSpanProperty extends MappingProperty {

	public TextSpanProperty() {
		super(MappingTypes.preanalyzed, false, true, AnalyzerTypes.semedico_text, TermVector.with_positions_offsets);
	}

}
