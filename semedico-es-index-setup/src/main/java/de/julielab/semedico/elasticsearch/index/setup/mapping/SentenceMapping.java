package de.julielab.semedico.elasticsearch.index.setup.mapping;

import de.julielab.semedico.elasticsearch.index.setup.Mappings;
import de.julielab.semedico.elasticsearch.index.setup.property.LikelihoodTextSpanProperties;

public class SentenceMapping extends Mappings {

	public SentenceMapping() {
		super(new SemedicoDefaultMapping(), new LikelihoodTextSpanProperties());
	}

}
